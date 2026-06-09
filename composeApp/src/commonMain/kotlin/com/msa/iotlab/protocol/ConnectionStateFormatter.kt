package com.msa.iotlab.protocol

/**
 * Maps protocol states into stable UI strings without leaking presentation
 * conditionals into individual protocol clients.
 */
object ConnectionStateFormatter {
    fun toReadableText(state: ConnectionState): String = when (state) {
        ConnectionState.Idle -> "Idle"
        ConnectionState.Connecting -> "Connecting"
        ConnectionState.Connected -> "Connected"
        is ConnectionState.Disconnecting -> "Disconnecting ${state.reason.orEmpty()}".trim()
        is ConnectionState.Disconnected -> "Disconnected ${state.reason.orEmpty()}".trim()
        is ConnectionState.Failed -> "Failed: ${state.message}"
    }
}
