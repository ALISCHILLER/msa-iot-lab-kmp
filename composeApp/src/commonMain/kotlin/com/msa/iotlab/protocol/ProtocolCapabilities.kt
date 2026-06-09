package com.msa.iotlab.protocol

/**
 * Describes optional protocol features so UI and docs can explain platform limitations explicitly.
 */
enum class ProtocolCapability {
    CONNECT,
    SEND,
    RECEIVE,
    TLS,
    AUTHENTICATION,
    BROADCAST,
    SUBSCRIBE,
    AUTO_REPEAT,
    HISTORY_LOGGING
}

/**
 * Central capability registry for each protocol in the shared product model.
 * This keeps feature flags out of UI screens and makes future protocol additions safer.
 */
object ProtocolCapabilityRegistry {
    fun capabilitiesFor(type: ProtocolType): Set<ProtocolCapability> = when (type) {
        ProtocolType.MQTT -> setOf(
            ProtocolCapability.CONNECT,
            ProtocolCapability.SEND,
            ProtocolCapability.RECEIVE,
            ProtocolCapability.TLS,
            ProtocolCapability.AUTHENTICATION,
            ProtocolCapability.SUBSCRIBE,
            ProtocolCapability.AUTO_REPEAT,
            ProtocolCapability.HISTORY_LOGGING
        )
        ProtocolType.WEBSOCKET -> setOf(
            ProtocolCapability.CONNECT,
            ProtocolCapability.SEND,
            ProtocolCapability.RECEIVE,
            ProtocolCapability.TLS,
            ProtocolCapability.AUTHENTICATION,
            ProtocolCapability.AUTO_REPEAT,
            ProtocolCapability.HISTORY_LOGGING
        )
        ProtocolType.TCP -> setOf(
            ProtocolCapability.CONNECT,
            ProtocolCapability.SEND,
            ProtocolCapability.RECEIVE,
            ProtocolCapability.AUTO_REPEAT,
            ProtocolCapability.HISTORY_LOGGING
        )
        ProtocolType.UDP -> setOf(
            ProtocolCapability.CONNECT,
            ProtocolCapability.SEND,
            ProtocolCapability.RECEIVE,
            ProtocolCapability.BROADCAST,
            ProtocolCapability.AUTO_REPEAT,
            ProtocolCapability.HISTORY_LOGGING
        )
    }
}
