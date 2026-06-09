package com.msa.iotlab.platform

import com.msa.iotlab.payload.PayloadCodec
import com.msa.iotlab.protocol.BaseProtocolClient
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.OutgoingPayload
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolMessageFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Raw TCP socket implementation for JVM-capable targets.
 * It owns only socket lifecycle and byte transport; profile/session/history logic remains outside it.
 */
class TcpJavaProtocolClient : BaseProtocolClient("TCP") {
    private var socket: Socket? = null
    private var readJob: Job? = null

    override suspend fun connect(profile: ConnectionProfile) {
        activeProfile = profile
        updateState(ConnectionState.Connecting)
        var newSocket: Socket? = null
        runCatching {
            val createdSocket = Socket()
            newSocket = createdSocket
            createdSocket.connect(InetSocketAddress(profile.host, profile.port), profile.timeoutMillis.toSocketTimeout())
            createdSocket.tcpNoDelay = profile.tcp.tcpNoDelay
            socket = createdSocket
            updateState(ConnectionState.Connected)
            emitConnected("TCP connected to ${profile.host}:${profile.port}")
            startReadLoop(profile, createdSocket)
        }.onFailure { error ->
            runCatching { newSocket?.close() }
            socket = null
            updateState(ConnectionState.Failed(error.message ?: "TCP connection failed"))
            emitFailure("connect", error)
        }
    }

    private fun startReadLoop(profile: ConnectionProfile, socket: Socket) {
        readJob?.cancel()
        readJob = scope.launch {
            val buffer = ByteArray(profile.tcp.readBufferSize)
            while (!socket.isClosed) {
                runCatching {
                    val count = socket.getInputStream().read(buffer)
                    if (count <= 0) {
                        updateState(ConnectionState.Disconnected("Remote closed"))
                        emitDisconnected("Remote closed")
                        return@launch
                    }
                    val bytes = buffer.copyOf(count)
                    emit(ProtocolEvent.MessageReceived(ProtocolMessageFactory.fromBytes(profile, MessageDirection.IN, bytes)))
                }.onFailure { error ->
                    if (!socket.isClosed) {
                        updateState(ConnectionState.Disconnected("Read failed"))
                        emitFailure("read", error)
                        emitDisconnected("Read failed")
                    }
                    return@launch
                }
            }
        }
    }

    override suspend fun send(payload: OutgoingPayload) {
        val profile = requireActiveProfile("send") ?: return
        val connectedSocket = socket ?: return emitFailure("send", IllegalStateException("TCP socket is not connected"))
        val raw = PayloadCodec.encode(payload.text, payload.encoding)
        val bytes = raw.withLineEnding(profile.tcp.lineEnding)
        runCatching {
            connectedSocket.getOutputStream().write(bytes)
            connectedSocket.getOutputStream().flush()
            emit(ProtocolEvent.MessageSent(ProtocolMessageFactory.fromBytes(profile, MessageDirection.OUT, bytes)))
        }.onFailure { error ->
            emitFailure("send", error)
        }
    }

    override suspend fun disconnect() {
        updateState(ConnectionState.Disconnecting("User requested"))
        readJob?.cancel()
        runCatching { socket?.close() }
        socket = null
        updateState(ConnectionState.Disconnected("Closed"))
        emitDisconnected("Closed")
    }
}

private fun ByteArray.withLineEnding(lineEnding: String): ByteArray = when (lineEnding.uppercase()) {
    "LF" -> this + byteArrayOf('\n'.code.toByte())
    "CRLF" -> this + byteArrayOf('\r'.code.toByte(), '\n'.code.toByte())
    else -> this
}

private fun Long.toSocketTimeout(): Int = coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
