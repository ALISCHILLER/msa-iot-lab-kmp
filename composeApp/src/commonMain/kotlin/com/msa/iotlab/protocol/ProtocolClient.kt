package com.msa.iotlab.protocol

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Common transport contract implemented by MQTT, WebSocket, TCP and UDP clients.
 * The UI talks only to this interface, preserving dependency inversion across protocol engines.
 */
interface ProtocolClient {
    val state: StateFlow<ConnectionState>
    val events: Flow<ProtocolEvent>

    suspend fun connect(profile: ConnectionProfile)
    suspend fun disconnect()
    suspend fun send(payload: OutgoingPayload)
}

/**
 * Abstract factory that lets each platform provide protocol implementations through dependency inversion.
 */
interface ProtocolClientFactory {
    fun create(type: ProtocolType): ProtocolClient

    /** Creates a protocol client for a full profile, giving factories room to apply profile-aware decorators later. */
    fun create(profile: ConnectionProfile): ProtocolClient = create(profile.protocol)
}

/**
 * Safe fallback client used when a protocol is not available on a specific platform.
 * It behaves like a real client from the UI perspective and reports clear unsupported-operation events.
 */
class UnsupportedProtocolClient(
    private val protocol: ProtocolType,
    private val reason: String
) : BaseProtocolClient(protocol.title) {
    override suspend fun connect(profile: ConnectionProfile) {
        activeProfile = profile
        updateState(ConnectionState.Failed(reason))
        emit(ProtocolEvent.Error("${protocol.title} is not supported here. $reason", null, nowMillis()))
    }

    override suspend fun disconnect() {
        updateState(ConnectionState.Disconnected("Unsupported"))
        emitDisconnected("Unsupported")
    }

    override suspend fun send(payload: OutgoingPayload) {
        emit(ProtocolEvent.Error("Cannot send. ${protocol.title} is not connected on this platform.", null, nowMillis()))
    }
}
