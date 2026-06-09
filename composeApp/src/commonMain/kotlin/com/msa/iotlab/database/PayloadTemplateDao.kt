package com.msa.iotlab.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user-defined reusable payload templates.
 */
@Dao
interface PayloadTemplateDao {
    @Query("SELECT * FROM payload_templates ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PayloadTemplateEntity>>

    @Query("SELECT * FROM payload_templates WHERE protocol IS NULL OR protocol = :protocol ORDER BY updatedAt DESC")
    fun observeForProtocol(protocol: String): Flow<List<PayloadTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PayloadTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PayloadTemplateEntity>)

    @Query("DELETE FROM payload_templates WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM payload_templates")
    suspend fun deleteAll()
}
