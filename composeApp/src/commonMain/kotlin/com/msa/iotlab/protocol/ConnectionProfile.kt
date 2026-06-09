package com.msa.iotlab.protocol

import kotlinx.serialization.Serializable

/**
 * User-defined protocol connection profile that can be persisted and reused.
 */
@Serializable
data class ConnectionProfile(
    val id: String,
    val name: String,
    val protocol: ProtocolType,
    val host: String,
    val port: Int,
    val tlsEnabled: Boolean = false,
    val timeoutMillis: Long = 10_000,
    val autoReconnect: Boolean = false,
    val payloadEncoding: PayloadEncoding = PayloadEncoding.TEXT,
    val createdAt: Long,
    val updatedAt: Long,
    val mqtt: MqttOptions = MqttOptions(),
    val websocket: WebSocketOptions = WebSocketOptions(),
    val tcp: TcpOptions = TcpOptions(),
    val udp: UdpOptions = UdpOptions()
)
