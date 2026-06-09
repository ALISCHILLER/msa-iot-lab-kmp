package com.msa.iotlab.protocol

import kotlinx.serialization.Serializable

/**
 * MQTT-specific connection, publish and subscribe options stored inside ConnectionProfile.
 */
@Serializable
data class MqttOptions(
    val clientId: String = "msa-iot-lab-client",
    val username: String? = null,
    val password: String? = null,
    val cleanSession: Boolean = true,
    val keepAliveSeconds: Int = 60,
    val qos: Int = 0,
    val retain: Boolean = false,
    val subscribeTopic: String? = null,
    val publishTopic: String? = null,
    val willTopic: String? = null,
    val willPayload: String? = null
)

/**
 * WebSocket-specific path, header and keep-alive options.
 */
@Serializable
data class WebSocketOptions(
    val path: String = "",
    val headersJson: String = "{}",
    val pingIntervalMillis: Long = 15_000
)

/**
 * TCP-specific read and line-ending options for raw socket communication.
 */
@Serializable
data class TcpOptions(
    val lineEnding: String = "NONE",
    val readBufferSize: Int = 4096,
    val tcpNoDelay: Boolean = true
)

/**
 * UDP-specific socket binding, broadcast and receive options.
 */
@Serializable
data class UdpOptions(
    val localPort: Int? = null,
    val broadcastEnabled: Boolean = false,
    val listenEnabled: Boolean = true,
    val readBufferSize: Int = 4096
)
