package com.msa.iotlab.protocol

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.AppDispatchers
import com.msa.iotlab.core.DefaultAppDispatchers
import com.msa.iotlab.core.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base implementation for protocol clients that centralizes state and event publishing.
 * Concrete transports keep only protocol-specific socket/MQTT/WebSocket behavior, which improves SRP.
 */
abstract class BaseProtocolClient(
    private val protocolName: String,
    dispatchers: AppDispatchers = DefaultAppDispatchers,
    private val timeProvider: TimeProvider = AppClock
) : ProtocolClient {
    /** Coroutine scope reserved for long-running transport read loops. */
    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.io)

    /** Last connected profile, used by send operations after connect succeeds. */
    protected var activeProfile: ConnectionProfile? = null

    private val mutableState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    private val mutableEvents = MutableSharedFlow<ProtocolEvent>(extraBufferCapacity = 256)

    override val state: StateFlow<ConnectionState> = mutableState.asStateFlow()
    override val events: Flow<ProtocolEvent> = mutableEvents.asSharedFlow()

    /** Returns the current event timestamp through the injected clock provider. */
    protected fun nowMillis(): Long = timeProvider.nowMillis()

    /** Updates the observable connection state in one controlled place. */
    protected fun updateState(state: ConnectionState) {
        mutableState.value = state
    }

    /** Emits one normalized event to the UI/history pipeline. */
    protected suspend fun emit(event: ProtocolEvent) {
        mutableEvents.emit(event)
    }

    /** Emits a standard connected lifecycle event. */
    protected suspend fun emitConnected(message: String? = null) {
        emit(ProtocolEvent.Connected(timeProvider.nowMillis()))
        if (message != null) emitSystem(message)
    }

    /** Emits a standard disconnected lifecycle event. */
    protected suspend fun emitDisconnected(reason: String = "Closed") {
        emit(ProtocolEvent.Disconnected(reason, timeProvider.nowMillis()))
    }

    /** Emits a non-fatal diagnostic message for the live console. */
    protected suspend fun emitSystem(message: String) {
        emit(ProtocolEvent.System(message, timeProvider.nowMillis()))
    }

    /** Emits a user-readable transport error with optional technical cause. */
    protected suspend fun emitFailure(operation: String, error: Throwable? = null) {
        val technicalMessage = error?.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
        emit(ProtocolEvent.Error("$protocolName $operation failed: $technicalMessage", error, timeProvider.nowMillis()))
    }

    /** Returns the active profile or emits a consistent not-connected error. */
    protected suspend fun requireActiveProfile(operation: String): ConnectionProfile? {
        return activeProfile ?: run {
            emit(ProtocolEvent.Error("Cannot $operation. $protocolName has no active profile.", null, timeProvider.nowMillis()))
            null
        }
    }
}
