package com.msa.iotlab.console

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.AppDispatchers
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.core.DefaultAppDispatchers
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.canSendPayload
import com.msa.iotlab.protocol.displayName
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolClient
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Presentation-agnostic controller for one live console session.
 * It owns protocol orchestration, retry policy, session persistence, bounded event storage and auto-repeat lifecycle.
 */
class ConsoleController(
    private val profile: ConnectionProfile,
    private val client: ProtocolClient,
    private val sessionManager: ConsoleSessionManager,
    private val eventStore: ConsoleEventStore = ConsoleEventStore(),
    private val retryPolicy: ConnectionRetryPolicy = if (profile.autoReconnect) ConnectionRetryPolicy.default() else ConnectionRetryPolicy.singleAttempt(),
    dispatchers: AppDispatchers = DefaultAppDispatchers,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.default),
    private val ownsScope: Boolean = true
) {
    val connectionState: StateFlow<ConnectionState> = client.state

    private val mutableEvents = MutableStateFlow<List<ProtocolEvent>>(emptyList())
    val events: StateFlow<List<ProtocolEvent>> = mutableEvents.asStateFlow()

    private val mutableIsRepeating = MutableStateFlow(false)
    val isRepeating: StateFlow<Boolean> = mutableIsRepeating.asStateFlow()

    private var sessionId: String? = null
    private var repeatJob: Job? = null
    private var connectJob: Job? = null
    private var sendCounter: Long = 0L
    private var manualDisconnectRequested: Boolean = false

    init {
        scope.launch {
            client.events.collect { event ->
                appendAndPersist(event)
                if (event is ProtocolEvent.Disconnected && profile.autoReconnect && !manualDisconnectRequested) {
                    scheduleReconnect("Transport disconnected: ${event.reason.orEmpty()}")
                }
            }
        }
    }

    fun connect() {
        manualDisconnectRequested = false
        connectJob?.cancel()
        connectJob = scope.launch {
            sessionId = sessionManager.startIfNeeded(sessionId, profile)
            connectWithRetry(reason = "Manual connect")
        }
    }

    fun disconnect() {
        manualDisconnectRequested = true
        scope.launch {
            stopRepeat()
            connectJob?.cancel()
            client.disconnect()
            finishCurrentSession(SessionStatus.FINISHED)
        }
    }

    fun send(rawPayload: String, encoding: PayloadEncoding) {
        scope.launch {
            if (!ensureConnectedForSend()) return@launch
            sendCounter++
            sendPrepared(rawPayload, encoding, sendCounter)
        }
    }

    fun startRepeat(rawPayload: String, encoding: PayloadEncoding, delayText: String) {
        if (repeatJob != null) return
        val delayMillis = delayText.toLongOrNull()
            ?.coerceAtLeast(ConsoleLimits.MIN_REPEAT_DELAY_MS)
            ?: ConsoleLimits.DEFAULT_REPEAT_DELAY_MS
        repeatJob = scope.launch {
            try {
                if (!ensureConnectedForSend()) return@launch
                val validationCounter = sendCounter + 1
                when (val validation = ConsoleCommandService.createOutgoingPayload(rawPayload, encoding, validationCounter, timeProvider, idProvider)) {
                    is AppResult.Error -> {
                        appendAndPersist(ProtocolEvent.Error(validation.message, validation.cause, timeProvider.nowMillis()))
                        return@launch
                    }
                    is AppResult.Success -> Unit
                }
                mutableIsRepeating.value = true
                while (isActive && connectionState.value.canSendPayload) {
                    sendCounter++
                    sendPrepared(rawPayload, encoding, sendCounter)
                    delay(delayMillis)
                }
                if (isActive && !connectionState.value.canSendPayload) {
                    appendAndPersist(
                        ProtocolEvent.System(
                            message = "Auto-repeat stopped because connection state is ${connectionState.value.displayName}.",
                            timestampMillis = timeProvider.nowMillis()
                        )
                    )
                }
            } finally {
                repeatJob = null
                mutableIsRepeating.value = false
            }
        }
    }

    fun stopRepeat() {
        repeatJob?.cancel()
        repeatJob = null
        mutableIsRepeating.value = false
    }

    fun clearEvents() {
        eventStore.clear()
        mutableEvents.value = emptyList()
    }

    fun close() {
        manualDisconnectRequested = true
        stopRepeat()
        connectJob?.cancel()
        scope.launch {
            client.disconnect()
            finishCurrentSession(SessionStatus.CANCELLED)
            if (ownsScope) scope.cancel()
        }
    }

    private suspend fun connectWithRetry(reason: String) {
        val attempts = retryPolicy.maxAttempts.coerceAtLeast(1)
        for (attempt in 1..attempts) {
            if (attempt > 1) delay(retryPolicy.delayMillis)
            appendAndPersist(ProtocolEvent.System("$reason. Connection attempt $attempt/$attempts", timeProvider.nowMillis()))
            runCatching { client.connect(profile) }
                .onFailure { error ->
                    appendAndPersist(ProtocolEvent.Error("Connection attempt $attempt failed: ${error.message}", error, timeProvider.nowMillis()))
                }
            if (client.state.value == ConnectionState.Connected) return
        }
        appendAndPersist(ProtocolEvent.Error("Connection failed after $attempts attempt(s).", null, timeProvider.nowMillis()))
        finishCurrentSession(SessionStatus.FAILED)
    }

    private suspend fun finishCurrentSession(status: SessionStatus) {
        val activeSession = sessionId
        if (activeSession != null) {
            sessionManager.finish(activeSession, status)
            sessionId = null
        }
    }

    private fun scheduleReconnect(reason: String) {
        if (connectJob?.isActive == true) return
        connectJob = scope.launch {
            sessionId = sessionManager.startIfNeeded(sessionId, profile)
            appendAndPersist(ProtocolEvent.System("Auto reconnect scheduled. $reason", timeProvider.nowMillis()))
            connectWithRetry(reason = "Auto reconnect")
        }
    }

    private suspend fun sendPrepared(rawPayload: String, encoding: PayloadEncoding, counter: Long) {
        if (!ensureConnectedForSend()) return
        when (val result = ConsoleCommandService.createOutgoingPayload(rawPayload, encoding, counter, timeProvider, idProvider)) {
            is AppResult.Success -> client.send(result.data)
            is AppResult.Error -> appendAndPersist(ProtocolEvent.Error(result.message, result.cause, timeProvider.nowMillis()))
        }
    }

    private suspend fun ensureConnectedForSend(): Boolean {
        if (connectionState.value.canSendPayload) return true
        appendAndPersist(
            ProtocolEvent.Error(
                message = "Cannot send payload while connection state is ${connectionState.value.displayName}. Connect first.",
                cause = null,
                timestampMillis = timeProvider.nowMillis()
            )
        )
        return false
    }

    private suspend fun appendAndPersist(event: ProtocolEvent) {
        eventStore.add(event)
        mutableEvents.value = eventStore.snapshotNewestFirst()
        sessionManager.persistEvent(event, profile, sessionId)
    }
}
