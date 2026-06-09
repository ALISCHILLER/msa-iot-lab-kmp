package com.msa.iotlab

import com.msa.iotlab.console.ConsoleHistoryGateway
import com.msa.iotlab.core.AppDispatchers
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.OutgoingPayload
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolClient
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolMessage
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.protocol.SessionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Test dispatcher provider that routes all application dispatchers to the same deterministic test dispatcher.
 */
class TestAppDispatchers(
    private val dispatcher: CoroutineDispatcher
) : AppDispatchers {
    override val default: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}

/**
 * In-memory protocol client fake used to verify console orchestration without opening real sockets.
 */
class FakeProtocolClient(
    private val connectOutcomes: MutableList<ConnectionState> = mutableListOf(ConnectionState.Connected)
) : ProtocolClient {
    private val mutableState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    private val mutableEvents = MutableSharedFlow<ProtocolEvent>(extraBufferCapacity = 64)

    override val state: StateFlow<ConnectionState> = mutableState.asStateFlow()
    override val events: Flow<ProtocolEvent> = mutableEvents.asSharedFlow()

    val connectedProfiles: MutableList<ConnectionProfile> = mutableListOf()
    val sentPayloads: MutableList<OutgoingPayload> = mutableListOf()
    var disconnectCalls: Int = 0
        private set

    override suspend fun connect(profile: ConnectionProfile) {
        connectedProfiles += profile
        val outcome = if (connectOutcomes.isNotEmpty()) connectOutcomes.removeAt(0) else ConnectionState.Connected
        mutableState.value = outcome
        when (outcome) {
            ConnectionState.Connected -> mutableEvents.emit(ProtocolEvent.Connected(timestampMillis = 1L))
            is ConnectionState.Failed -> mutableEvents.emit(ProtocolEvent.Error(outcome.message, timestampMillis = 1L))
            else -> Unit
        }
    }

    override suspend fun disconnect() {
        disconnectCalls++
        mutableState.value = ConnectionState.Disconnected("Test disconnect")
        mutableEvents.emit(ProtocolEvent.Disconnected("Test disconnect", timestampMillis = 2L))
    }

    override suspend fun send(payload: OutgoingPayload) {
        sentPayloads += payload
        val profile = connectedProfiles.lastOrNull() ?: sampleProfile()
        mutableEvents.emit(
            ProtocolEvent.MessageSent(
                ProtocolMessage(
                    id = "sent-${sentPayloads.size}",
                    profileId = profile.id,
                    protocol = profile.protocol,
                    direction = MessageDirection.OUT,
                    payloadText = payload.text,
                    payloadHex = payload.text.encodeToByteArray().joinToString(" ") { byte ->
                        (byte.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase()
                    },
                    payloadSizeBytes = payload.text.encodeToByteArray().size,
                    timestampMillis = 3L
                )
            )
        )
    }

    suspend fun emitRemoteDisconnect(reason: String = "Remote disconnect") {
        mutableState.value = ConnectionState.Disconnected(reason)
        mutableEvents.emit(ProtocolEvent.Disconnected(reason, timestampMillis = 4L))
    }
}

/**
 * Recording history gateway used by console tests to verify session and message persistence behavior.
 */
class RecordingConsoleHistoryGateway : ConsoleHistoryGateway {
    data class SessionStart(val profileId: String?, val protocol: ProtocolType, val name: String)
    data class SessionFinish(val sessionId: String, val status: SessionStatus)

    val starts: MutableList<SessionStart> = mutableListOf()
    val finishes: MutableList<SessionFinish> = mutableListOf()
    val logs: MutableList<ProtocolMessage> = mutableListOf()

    override suspend fun startSession(profileId: String?, protocol: ProtocolType, name: String): String {
        starts += SessionStart(profileId, protocol, name)
        return "session-${starts.size}"
    }

    override suspend fun finishSession(sessionId: String, status: SessionStatus) {
        finishes += SessionFinish(sessionId, status)
    }

    override suspend fun log(message: ProtocolMessage) {
        logs += message
    }
}

/**
 * Factory object for small immutable domain objects shared across unit tests.
 */
object TestDomainFactory {
    fun profile(
        protocol: ProtocolType = ProtocolType.TCP,
        autoReconnect: Boolean = false
    ): ConnectionProfile = sampleProfile(protocol, autoReconnect)
}

private fun sampleProfile(
    protocol: ProtocolType = ProtocolType.TCP,
    autoReconnect: Boolean = false
): ConnectionProfile = ConnectionProfile(
    id = "profile-1",
    name = "Test Profile",
    protocol = protocol,
    host = "127.0.0.1",
    port = protocol.defaultPort,
    autoReconnect = autoReconnect,
    payloadEncoding = PayloadEncoding.TEXT,
    createdAt = 1L,
    updatedAt = 1L
)

/**
 * In-memory profile repository fake that exercises use cases without Room or platform dependencies.
 */
class InMemoryProfileRepository : com.msa.iotlab.profile.ProfileRepository {
    private val profiles = MutableStateFlow<List<ConnectionProfile>>(emptyList())

    override fun observeProfiles(): Flow<List<ConnectionProfile>> = profiles.asStateFlow()

    override suspend fun getProfile(id: String): ConnectionProfile? = profiles.value.firstOrNull { it.id == id }

    override suspend fun save(profile: ConnectionProfile) {
        profiles.value = profiles.value.filterNot { it.id == profile.id } + profile
    }

    override suspend fun saveAll(profiles: List<ConnectionProfile>) {
        this.profiles.value = profiles
    }

    override suspend fun delete(id: String) {
        profiles.value = profiles.value.filterNot { it.id == id }
    }
}

/**
 * In-memory template repository fake used to test template and export use cases without Room.
 */
class InMemoryPayloadTemplateRepository : com.msa.iotlab.template.PayloadTemplateRepository {
    private val templates = MutableStateFlow<List<com.msa.iotlab.protocol.PayloadTemplate>>(emptyList())

    override fun observeAll(): Flow<List<com.msa.iotlab.protocol.PayloadTemplate>> = templates.asStateFlow()

    override fun observeForProtocol(protocol: ProtocolType): Flow<List<com.msa.iotlab.protocol.PayloadTemplate>> =
        MutableStateFlow(templates.value.filter { it.protocol == null || it.protocol == protocol }).asStateFlow()

    override suspend fun save(template: com.msa.iotlab.protocol.PayloadTemplate) {
        templates.value = templates.value.filterNot { it.id == template.id } + template
    }

    override suspend fun saveAll(templates: List<com.msa.iotlab.protocol.PayloadTemplate>) {
        this.templates.value = templates
    }

    override suspend fun delete(id: String) {
        templates.value = templates.value.filterNot { it.id == id }
    }

    override suspend fun seedDefaults() = Unit
}

/**
 * Deterministic test clock used to assert generated timestamps without relying on wall-clock time.
 */
class FixedTimeProvider(
    private val fixedMillis: Long
) : com.msa.iotlab.core.TimeProvider {
    override fun nowMillis(): Long = fixedMillis
}

/**
 * Deterministic ID provider that emits predefined IDs first and then stable sequential fallbacks.
 */
class SequentialIdProvider(
    private val ids: MutableList<String> = mutableListOf("id-1", "id-2", "id-3", "id-4")
) : com.msa.iotlab.core.IdProvider {
    private var fallbackCounter: Int = 0

    override fun newId(): String = if (ids.isNotEmpty()) {
        ids.removeAt(0)
    } else {
        fallbackCounter += 1
        "generated-$fallbackCounter"
    }
}
