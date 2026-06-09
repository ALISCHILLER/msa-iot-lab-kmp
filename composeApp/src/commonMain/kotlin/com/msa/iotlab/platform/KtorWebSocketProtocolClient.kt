package com.msa.iotlab.platform

import com.msa.iotlab.payload.PayloadCodec
import com.msa.iotlab.protocol.BaseProtocolClient
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.HeaderJsonParser
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.OutgoingPayload
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolMessageFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Ktor-backed WebSocket client shared by Android, Desktop and iOS targets.
 * It implements only WebSocket transport details while BaseProtocolClient owns state/event plumbing.
 */
class KtorWebSocketProtocolClient(
    private val client: HttpClient
) : BaseProtocolClient("WebSocket") {
    private var session: DefaultClientWebSocketSession? = null
    private var readJob: Job? = null

    override suspend fun connect(profile: ConnectionProfile) {
        activeProfile = profile
        updateState(ConnectionState.Connecting)
        runCatching {
            val webSocketUrl = profile.toWebSocketUrl()
            val headers = HeaderJsonParser.parse(profile.websocket.headersJson)
            val ws = client.webSocketSession {
                url(webSocketUrl)
                headers.forEach { (name, value) -> header(name, value) }
            }
            session = ws
            updateState(ConnectionState.Connected)
            emitConnected("WebSocket connected to $webSocketUrl")
            startReadLoop(profile, ws)
        }.onFailure { error ->
            updateState(ConnectionState.Failed(error.message ?: "WebSocket connection failed"))
            emitFailure("connect", error)
        }
    }

    private fun startReadLoop(profile: ConnectionProfile, ws: DefaultClientWebSocketSession) {
        readJob?.cancel()
        readJob = scope.launch {
            runCatching {
                for (frame in ws.incoming) {
                    when (frame) {
                        is Frame.Text -> emitIncoming(profile, frame.readText().encodeToByteArray())
                        is Frame.Binary -> emitIncoming(profile, frame.readBytes())
                        is Frame.Close -> {
                            updateState(ConnectionState.Disconnected("Remote closed"))
                            emitDisconnected("Remote closed")
                            break
                        }
                        else -> Unit
                    }
                }
            }.onFailure { error ->
                updateState(ConnectionState.Disconnected("Read failed"))
                emitFailure("read", error)
                emitDisconnected("Read failed")
            }
        }
    }

    private suspend fun emitIncoming(profile: ConnectionProfile, bytes: ByteArray) {
        emit(
            ProtocolEvent.MessageReceived(
                ProtocolMessageFactory.fromBytes(profile, MessageDirection.IN, bytes)
            )
        )
    }

    override suspend fun send(payload: OutgoingPayload) {
        val profile = requireActiveProfile("send") ?: return
        val ws = session ?: return emitFailure("send", IllegalStateException("WebSocket is not connected"))
        val bytes = PayloadCodec.encode(payload.text, payload.encoding)
        runCatching {
            if (payload.encoding.isBinaryTransport()) ws.send(Frame.Binary(true, bytes)) else ws.send(payload.text)
            emit(ProtocolEvent.MessageSent(ProtocolMessageFactory.fromBytes(profile, MessageDirection.OUT, bytes)))
        }.onFailure { error ->
            emitFailure("send", error)
        }
    }

    override suspend fun disconnect() {
        updateState(ConnectionState.Disconnecting("User requested"))
        readJob?.cancel()
        runCatching { session?.close() }
        session = null
        updateState(ConnectionState.Disconnected("Closed"))
        emitDisconnected("Closed")
    }
}

private fun ConnectionProfile.toWebSocketUrl(): String {
    val scheme = if (tlsEnabled) "wss" else "ws"
    val normalizedPath = websocket.path.trim().let { if (it.isBlank()) "" else if (it.startsWith("/")) it else "/$it" }
    return "$scheme://$host:$port$normalizedPath"
}

private fun PayloadEncoding.isBinaryTransport(): Boolean = this == PayloadEncoding.HEX || this == PayloadEncoding.BASE64
