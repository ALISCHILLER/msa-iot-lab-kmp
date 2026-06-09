package com.msa.iotlab.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for lightweight application settings stored as key-value rows.
 */
@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    fun observe(key: String): Flow<AppSettingEntity?>

    @Query("SELECT * FROM app_settings")
    fun observeAll(): Flow<List<AppSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppSettingEntity)

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun delete(key: String)
}
