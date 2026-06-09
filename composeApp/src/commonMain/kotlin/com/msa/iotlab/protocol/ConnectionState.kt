package com.msa.iotlab.protocol

/**
 * Protocol connection state observed by shared UI screens.
 */
sealed interface ConnectionState {
    /** No active connection attempt has started. */
    data object Idle : ConnectionState

    /** A connection attempt is currently in progress. */
    data object Connecting : ConnectionState

    /** The protocol client has an active transport connection. */
    data object Connected : ConnectionState

    /** The protocol client is closing its transport connection. */
    data class Disconnecting(val reason: String? = null) : ConnectionState

    /** The protocol client is fully disconnected. */
    data class Disconnected(val reason: String? = null) : ConnectionState

    /** The last connection attempt failed with a user-visible message. */
    data class Failed(val message: String) : ConnectionState
}

/**
 * Returns true only when a protocol client can safely accept outbound payloads.
 */
val ConnectionState.canSendPayload: Boolean
    get() = this is ConnectionState.Connected

/**
 * User-readable state name used by controllers and lightweight diagnostics.
 */
val ConnectionState.displayName: String
    get() = when (this) {
        ConnectionState.Idle -> "Idle"
        ConnectionState.Connecting -> "Connecting"
        ConnectionState.Connected -> "Connected"
        is ConnectionState.Disconnecting -> "Disconnecting"
        is ConnectionState.Disconnected -> "Disconnected"
        is ConnectionState.Failed -> "Failed"
    }

