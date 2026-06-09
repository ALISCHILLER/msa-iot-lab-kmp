package com.msa.iotlab.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for reading and mutating persisted connection profiles.
 */
@Dao
interface ProfileDao {
    @Query("SELECT * FROM connection_profiles ORDER BY updatedAt DESC")
    fun observeProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM connection_profiles WHERE protocol = :protocol ORDER BY updatedAt DESC")
    fun observeByProtocol(protocol: String): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM connection_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ProfileEntity>)

    @Query("DELETE FROM connection_profiles WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM connection_profiles")
    suspend fun deleteAll()
}
