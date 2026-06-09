package com.msa.iotlab.protocol

/**
 * Streamed protocol events emitted by transport clients.
 */
sealed interface ProtocolEvent {
    /** Connection success event emitted after transport setup completes. */
    data class Connected(val timestampMillis: Long) : ProtocolEvent

    /** Disconnection event emitted when transport is closed. */
    data class Disconnected(val reason: String?, val timestampMillis: Long) : ProtocolEvent

    /** Incoming payload event normalized from the active protocol. */
    data class MessageReceived(val message: ProtocolMessage) : ProtocolEvent

    /** Outgoing payload event normalized from a send operation. */
    data class MessageSent(val message: ProtocolMessage) : ProtocolEvent

    /** Non-error diagnostic event shown in the console. */
    data class System(val message: String, val timestampMillis: Long) : ProtocolEvent

    /** Error event containing user-readable context and optional technical cause. */
    data class Error(val message: String, val cause: Throwable? = null, val timestampMillis: Long) : ProtocolEvent
}
