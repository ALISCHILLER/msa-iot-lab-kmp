package com.msa.iotlab.profile

import com.msa.iotlab.database.ProfileEntity
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.MqttOptions
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.protocol.TcpOptions
import com.msa.iotlab.protocol.UdpOptions
import com.msa.iotlab.protocol.WebSocketOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Serializable wrapper for protocol-specific options stored in one JSON column.
 * This keeps the profile table stable as protocol-specific settings grow.
 */
@Serializable
private data class ProfileOptionsPayload(
    val mqtt: MqttOptions = MqttOptions(),
    val websocket: WebSocketOptions = WebSocketOptions(),
    val tcp: TcpOptions = TcpOptions(),
    val udp: UdpOptions = UdpOptions()
)

/**
 * Converts a domain profile into its Room entity representation.
 */
fun ConnectionProfile.toEntity(json: Json): ProfileEntity {
    val options = ProfileOptionsPayload(mqtt, websocket, tcp, udp)
    return ProfileEntity(
        id = id,
        name = name,
        protocol = protocol.name,
        host = host,
        port = port,
        tlsEnabled = tlsEnabled,
        timeoutMillis = timeoutMillis,
        autoReconnect = autoReconnect,
        payloadEncoding = payloadEncoding.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        optionsJson = json.encodeToString(ProfileOptionsPayload.serializer(), options)
    )
}

/**
 * Converts a Room profile entity into the shared domain model.
 */
fun ProfileEntity.toDomain(json: Json): ConnectionProfile {
    val options = runCatching {
        json.decodeFromString(ProfileOptionsPayload.serializer(), optionsJson)
    }.getOrDefault(ProfileOptionsPayload())

    return ConnectionProfile(
        id = id,
        name = name,
        protocol = ProtocolType.valueOf(protocol),
        host = host,
        port = port,
        tlsEnabled = tlsEnabled,
        timeoutMillis = timeoutMillis,
        autoReconnect = autoReconnect,
        payloadEncoding = PayloadEncoding.valueOf(payloadEncoding),
        createdAt = createdAt,
        updatedAt = updatedAt,
        mqtt = options.mqtt,
        websocket = options.websocket,
        tcp = options.tcp,
        udp = options.udp
    )
}
