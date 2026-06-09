package com.msa.iotlab.history

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.database.MessageLogDao
import com.msa.iotlab.database.MessageLogEntity
import com.msa.iotlab.database.SessionDao
import com.msa.iotlab.database.SessionEntity
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.ProtocolMessage
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.protocol.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.msa.iotlab.core.AppJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for console sessions and message logs.
 * It is the only shared component allowed to write session/message Room records.
 */
class HistoryRepository(
    private val sessionDao: SessionDao,
    private val messageLogDao: MessageLogDao,
    private val json: Json = AppJson.pretty,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator
) {
    fun observeRecentSessions(limit: Int = 50): Flow<List<SessionEntity>> = sessionDao.observeRecent(limit)

    fun observeRecentMessages(limit: Int = 250): Flow<List<ProtocolMessage>> =
        messageLogDao.observeRecent(limit).map { list -> list.map { it.toDomain(json) } }

    fun observeSessionMessages(sessionId: String): Flow<List<ProtocolMessage>> =
        messageLogDao.observeBySession(sessionId).map { list -> list.map { it.toDomain(json) } }

    suspend fun startSession(profileId: String?, protocol: ProtocolType, name: String): String {
        val id = idProvider.newId()
        sessionDao.upsert(
            SessionEntity(
                id = id,
                profileId = profileId,
                protocol = protocol.name,
                name = name,
                startedAt = timeProvider.nowMillis(),
                endedAt = null,
                status = SessionStatus.RUNNING.name
            )
        )
        return id
    }

    suspend fun finishSession(id: String, status: SessionStatus = SessionStatus.FINISHED) {
        sessionDao.finish(id, timeProvider.nowMillis(), status.name)
    }

    suspend fun log(message: ProtocolMessage) {
        messageLogDao.insert(message.toEntity(json))
    }

    suspend fun clearAllMessages() {
        messageLogDao.deleteAll()
    }
}

/**
 * Maps a protocol message into a Room message log entity.
 */
fun ProtocolMessage.toEntity(json: Json): MessageLogEntity = MessageLogEntity(
    id = id,
    sessionId = sessionId,
    profileId = profileId,
    protocol = protocol.name,
    direction = direction.name,
    payloadText = payloadText,
    payloadHex = payloadHex,
    payloadSizeBytes = payloadSizeBytes,
    timestamp = timestampMillis,
    metadataJson = json.encodeToString(metadata)
)

/**
 * Maps a Room message log entity back into the shared protocol message model.
 */
fun MessageLogEntity.toDomain(json: Json): ProtocolMessage {
    val metadata = runCatching { json.decodeFromString<Map<String, String>>(metadataJson) }.getOrDefault(emptyMap())
    return ProtocolMessage(
        id = id,
        sessionId = sessionId,
        profileId = profileId,
        protocol = ProtocolType.valueOf(protocol),
        direction = MessageDirection.valueOf(direction),
        payloadText = payloadText,
        payloadHex = payloadHex,
        payloadSizeBytes = payloadSizeBytes,
        timestampMillis = timestamp,
        metadata = metadata
    )
}
