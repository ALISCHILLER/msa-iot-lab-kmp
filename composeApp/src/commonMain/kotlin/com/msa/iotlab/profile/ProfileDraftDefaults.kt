package com.msa.iotlab.profile

import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolType

/**
 * Produces deterministic default editor drafts for new and existing connection profiles.
 * Centralizing these defaults keeps Compose screens free from protocol-specific fallback rules.
 */
object ProfileDraftDefaults {
    fun from(initialProfile: ConnectionProfile?, initialProtocol: ProtocolType?): ProfileDraft {
        val protocol = initialProfile?.protocol ?: initialProtocol ?: ProtocolType.MQTT
        return ProfileDraft(
            initialProfile = initialProfile,
            name = initialProfile?.name ?: "New IoT Profile",
            protocol = protocol,
            host = initialProfile?.host ?: "192.168.1.10",
            port = (initialProfile?.port ?: protocol.defaultPort).toString(),
            tlsEnabled = initialProfile?.tlsEnabled ?: false,
            timeoutMillis = (initialProfile?.timeoutMillis ?: 10_000).toString(),
            autoReconnect = initialProfile?.autoReconnect ?: false,
            payloadEncoding = initialProfile?.payloadEncoding ?: PayloadEncoding.TEXT,
            mqttClientId = initialProfile?.mqtt?.clientId ?: "msa-iot-lab-client",
            mqttUsername = initialProfile?.mqtt?.username ?: "",
            mqttPassword = initialProfile?.mqtt?.password ?: "",
            mqttSubscribeTopic = initialProfile?.mqtt?.subscribeTopic ?: "#",
            mqttPublishTopic = initialProfile?.mqtt?.publishTopic ?: "device/test",
            mqttQos = (initialProfile?.mqtt?.qos ?: 0).toString(),
            mqttRetain = initialProfile?.mqtt?.retain ?: false,
            mqttCleanSession = initialProfile?.mqtt?.cleanSession ?: true,
            mqttKeepAliveSeconds = (initialProfile?.mqtt?.keepAliveSeconds ?: 60).toString(),
            wsPath = initialProfile?.websocket?.path ?: "",
            wsHeadersJson = initialProfile?.websocket?.headersJson ?: "{}",
            tcpLineEnding = initialProfile?.tcp?.lineEnding ?: "NONE",
            tcpReadBufferSize = (initialProfile?.tcp?.readBufferSize ?: 4096).toString(),
            udpLocalPort = initialProfile?.udp?.localPort?.toString() ?: "",
            udpBroadcastEnabled = initialProfile?.udp?.broadcastEnabled ?: false,
            udpListenEnabled = initialProfile?.udp?.listenEnabled ?: true,
            udpReadBufferSize = (initialProfile?.udp?.readBufferSize ?: 4096).toString()
        )
    }
}
