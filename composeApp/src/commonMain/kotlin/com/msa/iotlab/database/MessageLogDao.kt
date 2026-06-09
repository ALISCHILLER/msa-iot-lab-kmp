package com.msa.iotlab.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for persisted protocol traffic and console log messages.
 */
@Dao
interface MessageLogDao {
    @Query("SELECT * FROM message_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun observeBySession(sessionId: String): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs WHERE direction = :direction ORDER BY timestamp DESC LIMIT :limit")
    fun observeByDirection(direction: String, limit: Int): Flow<List<MessageLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MessageLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MessageLogEntity>)

    @Query("DELETE FROM message_logs WHERE sessionId = :sessionId")
    suspend fun deleteSessionMessages(sessionId: String)

    @Query("DELETE FROM message_logs")
    suspend fun deleteAll()
}
