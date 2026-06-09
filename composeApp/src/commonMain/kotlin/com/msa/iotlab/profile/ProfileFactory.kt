package com.msa.iotlab.profile

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.core.emptyToNull
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.MqttOptions
import com.msa.iotlab.protocol.TcpOptions
import com.msa.iotlab.protocol.UdpOptions
import com.msa.iotlab.protocol.WebSocketOptions

/**
 * Builds ConnectionProfile instances from editable form values and keeps
 * defaulting/coercion rules out of the Compose screen.
 */
object ProfileFactory {
    fun create(
        draft: ProfileDraft,
        timeProvider: TimeProvider = AppClock,
        idProvider: IdProvider = IdGenerator
    ): ConnectionProfile {
        val now = timeProvider.nowMillis()
        return ConnectionProfile(
            id = draft.initialProfile?.id ?: idProvider.newId(),
            name = draft.name.ifBlank { "Unnamed Profile" }.trim(),
            protocol = draft.protocol,
            host = draft.host.ifBlank { "localhost" }.trim(),
            port = draft.port.toIntOrNull()?.coerceIn(1, 65_535) ?: draft.protocol.defaultPort,
            tlsEnabled = draft.tlsEnabled,
            timeoutMillis = draft.timeoutMillis.toLongOrNull()?.coerceAtLeast(100) ?: 10_000,
            autoReconnect = draft.autoReconnect,
            payloadEncoding = draft.payloadEncoding,
            createdAt = draft.initialProfile?.createdAt ?: now,
            updatedAt = now,
            mqtt = MqttOptions(
                clientId = draft.mqttClientId.ifBlank { "msa-${idProvider.newId()}" }.trim(),
                username = draft.mqttUsername.emptyToNull(),
                password = draft.mqttPassword.emptyToNull(),
                cleanSession = draft.mqttCleanSession,
                keepAliveSeconds = draft.mqttKeepAliveSeconds.toIntOrNull()?.coerceAtLeast(5) ?: 60,
                qos = draft.mqttQos.toIntOrNull()?.coerceIn(0, 2) ?: 0,
                retain = draft.mqttRetain,
                subscribeTopic = draft.mqttSubscribeTopic.emptyToNull(),
                publishTopic = draft.mqttPublishTopic.emptyToNull()
            ),
            websocket = WebSocketOptions(
                path = draft.wsPath.trim(),
                headersJson = draft.wsHeadersJson.ifBlank { "{}" }.trim()
            ),
            tcp = TcpOptions(
                lineEnding = draft.tcpLineEnding.ifBlank { "NONE" }.trim().uppercase(),
                readBufferSize = draft.tcpReadBufferSize.toIntOrNull()?.coerceIn(256, 1_048_576) ?: 4096
            ),
            udp = UdpOptions(
                localPort = draft.udpLocalPort.toIntOrNull()?.coerceIn(1, 65_535),
                broadcastEnabled = draft.udpBroadcastEnabled,
                listenEnabled = draft.udpListenEnabled,
                readBufferSize = draft.udpReadBufferSize.toIntOrNull()?.coerceIn(256, 1_048_576) ?: 4096
            )
        )
    }
}
