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
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP datagram implementation for JVM-capable targets.
 * It supports send, listen and broadcast modes through the shared profile model.
 */
class UdpJavaProtocolClient : BaseProtocolClient("UDP") {
    private var socket: DatagramSocket? = null
    private var readJob: Job? = null

    override suspend fun connect(profile: ConnectionProfile) {
        activeProfile = profile
        updateState(ConnectionState.Connecting)
        var createdSocket: DatagramSocket? = null
        runCatching {
            val udpSocket = profile.udp.localPort?.let(::DatagramSocket) ?: DatagramSocket()
            createdSocket = udpSocket
            udpSocket.broadcast = profile.udp.broadcastEnabled
            socket = udpSocket
            updateState(ConnectionState.Connected)
            emitConnected("UDP ready on local port ${udpSocket.localPort}")
            if (profile.udp.listenEnabled) startReadLoop(profile, udpSocket)
        }.onFailure { error ->
            runCatching { createdSocket?.close() }
            socket = null
            updateState(ConnectionState.Failed(error.message ?: "UDP bind failed"))
            emitFailure("connect/bind", error)
        }
    }

    private fun startReadLoop(profile: ConnectionProfile, socket: DatagramSocket) {
        readJob?.cancel()
        readJob = scope.launch {
            val buffer = ByteArray(profile.udp.readBufferSize)
            while (!socket.isClosed) {
                runCatching {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val bytes = packet.data.copyOf(packet.length)
                    emit(
                        ProtocolEvent.MessageReceived(
                            ProtocolMessageFactory.fromBytes(
                                profile = profile,
                                direction = MessageDirection.IN,
                                bytes = bytes,
                                metadata = mapOf("remote" to "${packet.address.hostAddress}:${packet.port}")
                            )
                        )
                    )
                }.onFailure { error ->
                    if (!socket.isClosed) emitFailure("read", error)
                    return@launch
                }
            }
        }
    }

    override suspend fun send(payload: OutgoingPayload) {
        val profile = requireActiveProfile("send") ?: return
        val udpSocket = socket ?: return emitFailure("send", IllegalStateException("UDP socket is not ready"))
        val bytes = PayloadCodec.encode(payload.text, payload.encoding)
        runCatching {
            val address = InetAddress.getByName(profile.host)
            val packet = DatagramPacket(bytes, bytes.size, address, profile.port)
            udpSocket.send(packet)
            emit(
                ProtocolEvent.MessageSent(
                    ProtocolMessageFactory.fromBytes(
                        profile = profile,
                        direction = MessageDirection.OUT,
                        bytes = bytes,
                        metadata = mapOf("target" to "${profile.host}:${profile.port}")
                    )
                )
            )
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
