package com.msa.iotlab.profile

import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType

/**
 * Editable profile form state collected from UI before it becomes a validated ConnectionProfile.
 * Keeping form text in a draft object avoids huge parameter lists and makes the editor easier to evolve.
 */
data class ProfileDraft(
    val initialProfile: ConnectionProfile? = null,
    val name: String,
    val protocol: ProtocolType,
    val host: String,
    val port: String,
    val tlsEnabled: Boolean,
    val timeoutMillis: String,
    val autoReconnect: Boolean,
    val payloadEncoding: PayloadEncoding,
    val mqttClientId: String,
    val mqttUsername: String,
    val mqttPassword: String,
    val mqttSubscribeTopic: String,
    val mqttPublishTopic: String,
    val mqttQos: String,
    val mqttRetain: Boolean,
    val mqttCleanSession: Boolean,
    val mqttKeepAliveSeconds: String,
    val wsPath: String,
    val wsHeadersJson: String,
    val tcpLineEnding: String,
    val tcpReadBufferSize: String,
    val udpLocalPort: String,
    val udpBroadcastEnabled: Boolean,
    val udpListenEnabled: Boolean,
    val udpReadBufferSize: String
)
