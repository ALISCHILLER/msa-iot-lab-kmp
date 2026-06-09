package com.msa.iotlab.validation

import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.HeaderJsonParser
import com.msa.iotlab.protocol.MqttTopicValidator
import com.msa.iotlab.protocol.ProtocolType

/**
 * Validates connection profiles before persistence or connection attempts.
 * The validator is dependency-free enough for common tests and prevents invalid domain state early.
 */
object ProfileValidator {
    fun validate(profile: ConnectionProfile): ValidationResult {
        val errors = buildList {
            if (profile.name.isBlank()) add("Profile name is required.")
            if (profile.host.isBlank()) add("Host/IP is required.")
            if (profile.port !in 1..65_535) add("Port must be between 1 and 65535.")
            if (profile.timeoutMillis < 100) add("Timeout must be at least 100 ms.")
            when (profile.protocol) {
                ProtocolType.MQTT -> validateMqtt(profile)
                ProtocolType.WEBSOCKET -> validateWebSocket(profile)
                ProtocolType.TCP -> validateTcp(profile)
                ProtocolType.UDP -> validateUdp(profile)
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    private fun MutableList<String>.validateMqtt(profile: ConnectionProfile) {
        if (profile.mqtt.clientId.isBlank()) add("MQTT client ID is required.")
        if (profile.mqtt.qos !in 0..2) add("MQTT QoS must be 0, 1, or 2.")
        if (profile.mqtt.keepAliveSeconds < 5) add("MQTT keep alive should be at least 5 seconds.")
        if (profile.mqtt.publishTopic.isNullOrBlank() && profile.mqtt.subscribeTopic.isNullOrBlank()) {
            add("MQTT requires at least a publish or subscribe topic.")
        }
        MqttTopicValidator.validatePublishTopic(profile.mqtt.publishTopic)?.let(::add)
        MqttTopicValidator.validateSubscribeTopic(profile.mqtt.subscribeTopic)?.let(::add)
    }

    private fun MutableList<String>.validateWebSocket(profile: ConnectionProfile) {
        if (profile.websocket.headersJson.isBlank()) add("WebSocket headers JSON cannot be blank; use {}.")
        runCatching { HeaderJsonParser.parse(profile.websocket.headersJson) }
            .onFailure { add("WebSocket headers must be a flat JSON object with primitive values.") }
    }

    private fun MutableList<String>.validateTcp(profile: ConnectionProfile) {
        if (profile.tcp.readBufferSize < 256) add("TCP buffer size must be at least 256 bytes.")
        if (profile.tcp.lineEnding.uppercase() !in setOf("NONE", "LF", "CRLF")) add("TCP line ending must be NONE, LF, or CRLF.")
    }

    private fun MutableList<String>.validateUdp(profile: ConnectionProfile) {
        if (profile.udp.readBufferSize < 256) add("UDP buffer size must be at least 256 bytes.")
        profile.udp.localPort?.let { if (it !in 1..65_535) add("UDP local port must be between 1 and 65535.") }
    }
}

/**
 * Represents the result of domain validation without throwing exceptions.
 */
sealed interface ValidationResult {
    /** Marker object for a valid domain object. */
    data object Valid : ValidationResult

    /** Validation failure containing all detected problems. */
    data class Invalid(val errors: List<String>) : ValidationResult

    val isValid: Boolean get() = this is Valid
    fun message(): String = when (this) {
        Valid -> "Valid"
        is Invalid -> errors.joinToString(separator = "\n")
    }
}
