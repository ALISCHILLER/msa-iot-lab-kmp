package com.msa.iotlab.profile

import com.msa.iotlab.database.ProfileDao
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.validation.ProfileValidator
import com.msa.iotlab.validation.requireValid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.msa.iotlab.core.AppJson
import kotlinx.serialization.json.Json

/**
 * Persistence contract for connection profiles.
 * UI and import flows depend on this abstraction instead of a Room-specific implementation.
 */
interface ProfileRepository {
    fun observeProfiles(): Flow<List<ConnectionProfile>>
    suspend fun getProfile(id: String): ConnectionProfile?
    suspend fun save(profile: ConnectionProfile)
    suspend fun saveAll(profiles: List<ConnectionProfile>)
    suspend fun delete(id: String)
}

/**
 * Room-backed profile repository that maps between domain models and database entities.
 * Validation is enforced here so invalid profiles cannot enter persistence from imports or UI.
 */
class RoomProfileRepository(
    private val dao: ProfileDao,
    private val json: Json = AppJson.pretty
) : ProfileRepository {
    override fun observeProfiles(): Flow<List<ConnectionProfile>> =
        dao.observeProfiles().map { entities -> entities.map { it.toDomain(json) } }

    override suspend fun getProfile(id: String): ConnectionProfile? = dao.getProfileById(id)?.toDomain(json)

    override suspend fun save(profile: ConnectionProfile) {
        validate(profile)
        dao.upsert(profile.toEntity(json))
    }

    override suspend fun saveAll(profiles: List<ConnectionProfile>) {
        profiles.forEach(::validate)
        dao.upsertAll(profiles.map { it.toEntity(json) })
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    private fun validate(profile: ConnectionProfile) {
        ProfileValidator.validate(profile).requireValid()
    }
}
