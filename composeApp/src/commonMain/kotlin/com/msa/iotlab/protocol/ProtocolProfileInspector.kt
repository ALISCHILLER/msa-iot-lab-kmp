package com.msa.iotlab.protocol

/**
 * Severity assigned to profile inspection findings displayed before a live protocol test starts.
 */
enum class ProtocolFindingSeverity { INFO, WARNING, ERROR }

/**
 * One actionable protocol configuration finding produced by ProtocolProfileInspector.
 */
data class ProtocolFinding(
    val severity: ProtocolFindingSeverity,
    val title: String,
    val message: String
)

/**
 * Read-only protocol profile inspector used by UI and tests to explain risks before connecting.
 * It keeps protocol-specific diagnostics outside Compose screens and transport clients.
 */
object ProtocolProfileInspector {
    fun inspect(profile: ConnectionProfile): List<ProtocolFinding> {
        val findings = mutableListOf<ProtocolFinding>()
        inspectCommon(profile, findings)
        when (profile.protocol) {
            ProtocolType.MQTT -> inspectMqtt(profile, findings)
            ProtocolType.WEBSOCKET -> inspectWebSocket(profile, findings)
            ProtocolType.TCP -> inspectTcp(profile, findings)
            ProtocolType.UDP -> inspectUdp(profile, findings)
        }
        return findings
    }

    private fun inspectCommon(profile: ConnectionProfile, findings: MutableList<ProtocolFinding>) {
        if (profile.host.isBlank()) findings += error("Missing host", "Host/IP is required before connecting.")
        if (profile.port !in 1..65_535) findings += error("Invalid port", "Port must be between 1 and 65535.")
        if (profile.timeoutMillis < 500) findings += warning("Very short timeout", "Timeout below 500ms may fail on busy networks.")
        if (!profile.tlsEnabled && profile.protocol.supportsTls()) {
            findings += info("Plain connection", "TLS is disabled. Use TLS for production brokers and gateways.")
        }
    }

    private fun inspectMqtt(profile: ConnectionProfile, findings: MutableList<ProtocolFinding>) {
        MqttTopicValidator.validatePublishTopic(profile.mqtt.publishTopic)?.let { findings += error("Publish topic", it) }
        MqttTopicValidator.validateSubscribeTopic(profile.mqtt.subscribeTopic)?.let { findings += error("Subscribe topic", it) }
        if (profile.mqtt.publishTopic.isNullOrBlank() && profile.mqtt.subscribeTopic.isNullOrBlank()) {
            findings += warning("No MQTT topic", "Set a publish topic, subscribe topic, or both to make the console useful.")
        }
        if (profile.mqtt.qos !in 0..2) findings += error("Invalid QoS", "MQTT QoS must be 0, 1 or 2.")
        if (profile.mqtt.keepAliveSeconds !in 5..1_200) {
            findings += warning("Keep-alive range", "Keep-alive outside 5..1200 seconds can behave poorly with some brokers.")
        }
    }

    private fun inspectWebSocket(profile: ConnectionProfile, findings: MutableList<ProtocolFinding>) {
        runCatching { HeaderJsonParser.parse(profile.websocket.headersJson) }
            .onFailure { findings += error("Header JSON", it.message ?: "Headers are not valid JSON.") }
        if (profile.websocket.pingIntervalMillis < 5_000) {
            findings += warning("Aggressive ping", "Ping intervals below 5 seconds can create noisy traffic.")
        }
    }

    private fun inspectTcp(profile: ConnectionProfile, findings: MutableList<ProtocolFinding>) {
        if (profile.tcp.readBufferSize !in 128..65_536) findings += warning("TCP buffer", "Read buffer should usually be between 128 and 65536 bytes.")
        if (profile.tcp.lineEnding.uppercase() !in setOf("NONE", "LF", "CRLF")) {
            findings += error("Line ending", "TCP line ending must be NONE, LF or CRLF.")
        }
    }

    private fun inspectUdp(profile: ConnectionProfile, findings: MutableList<ProtocolFinding>) {
        val localPort = profile.udp.localPort
        if (localPort != null && localPort !in 1..65_535) findings += error("UDP local port", "Local bind port must be between 1 and 65535.")
        if (profile.udp.readBufferSize !in 128..65_507) findings += warning("UDP buffer", "UDP payload buffers above 65507 bytes exceed normal datagram payload size.")
        if (profile.udp.broadcastEnabled && !profile.host.endsWith(".255") && profile.host != "255.255.255.255") {
            findings += info("Broadcast enabled", "Use a broadcast address such as 255.255.255.255 or your subnet broadcast when discovering devices.")
        }
        if (!profile.udp.listenEnabled) findings += info("Listen disabled", "Incoming datagrams will not appear in the traffic monitor.")
    }

    private fun ProtocolType.supportsTls(): Boolean = this == ProtocolType.MQTT || this == ProtocolType.WEBSOCKET

    private fun info(title: String, message: String): ProtocolFinding = ProtocolFinding(ProtocolFindingSeverity.INFO, title, message)
    private fun warning(title: String, message: String): ProtocolFinding = ProtocolFinding(ProtocolFindingSeverity.WARNING, title, message)
    private fun error(title: String, message: String): ProtocolFinding = ProtocolFinding(ProtocolFindingSeverity.ERROR, title, message)
}
