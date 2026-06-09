package com.msa.iotlab.validation

import com.msa.iotlab.profile.ProfileDraft
import com.msa.iotlab.protocol.HeaderJsonParser
import com.msa.iotlab.protocol.MqttTopicValidator
import com.msa.iotlab.protocol.ProtocolType

/**
 * Validates raw profile editor input before defaults and coercion are applied.
 * This protects users from silently saving mistyped ports, timeouts or protocol options.
 */
object ProfileDraftValidator {
    fun validate(draft: ProfileDraft): ValidationResult {
        val errors = buildList {
            if (draft.name.isBlank()) add("Profile name is required.")
            if (draft.host.isBlank()) add("Host/IP is required.")
            validatePort("Port", draft.port)
            validateLongAtLeast("Timeout", draft.timeoutMillis, minimum = 100)
            when (draft.protocol) {
                ProtocolType.MQTT -> validateMqttDraft(draft)
                ProtocolType.WEBSOCKET -> validateWebSocketDraft(draft)
                ProtocolType.TCP -> validateTcpDraft(draft)
                ProtocolType.UDP -> validateUdpDraft(draft)
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    private fun MutableList<String>.validateMqttDraft(draft: ProfileDraft) {
        if (draft.mqttClientId.isBlank()) add("MQTT client ID is required.")
        validateIntRange("MQTT QoS", draft.mqttQos, 0, 2)
        validateLongAtLeast("MQTT keep alive", draft.mqttKeepAliveSeconds, minimum = 5)
        if (draft.mqttSubscribeTopic.isBlank() && draft.mqttPublishTopic.isBlank()) {
            add("MQTT requires at least a subscribe or publish topic.")
        }
        MqttTopicValidator.validateSubscribeTopic(draft.mqttSubscribeTopic.takeIf { it.isNotBlank() })?.let(::add)
        MqttTopicValidator.validatePublishTopic(draft.mqttPublishTopic.takeIf { it.isNotBlank() })?.let(::add)
    }

    private fun MutableList<String>.validateWebSocketDraft(draft: ProfileDraft) {
        val headers = draft.wsHeadersJson.ifBlank { "{}" }
        runCatching { HeaderJsonParser.parse(headers) }
            .onFailure { add("WebSocket headers must be a flat JSON object with primitive values.") }
    }

    private fun MutableList<String>.validateTcpDraft(draft: ProfileDraft) {
        if (draft.tcpLineEnding.trim().uppercase() !in setOf("NONE", "LF", "CRLF")) {
            add("TCP line ending must be NONE, LF, or CRLF.")
        }
        validateIntRange("TCP read buffer", draft.tcpReadBufferSize, 256, 1_048_576)
    }

    private fun MutableList<String>.validateUdpDraft(draft: ProfileDraft) {
        if (draft.udpLocalPort.isNotBlank()) validatePort("UDP local port", draft.udpLocalPort)
        validateIntRange("UDP read buffer", draft.udpReadBufferSize, 256, 1_048_576)
    }

    private fun MutableList<String>.validatePort(label: String, value: String) {
        validateIntRange(label, value, 1, 65_535)
    }

    private fun MutableList<String>.validateIntRange(label: String, value: String, min: Int, max: Int) {
        val parsed = value.trim().toIntOrNull()
        when {
            parsed == null -> add("$label must be a number.")
            parsed !in min..max -> add("$label must be between $min and $max.")
        }
    }

    private fun MutableList<String>.validateLongAtLeast(label: String, value: String, minimum: Long) {
        val parsed = value.trim().toLongOrNull()
        when {
            parsed == null -> add("$label must be a number.")
            parsed < minimum -> add("$label must be at least $minimum.")
        }
    }
}
