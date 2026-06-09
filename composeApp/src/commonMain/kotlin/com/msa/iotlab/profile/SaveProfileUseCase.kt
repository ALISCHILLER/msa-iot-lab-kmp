package com.msa.iotlab.profile

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.validation.ProfileDraftValidator
import com.msa.iotlab.validation.ProfileValidator
import com.msa.iotlab.validation.requireValid

/**
 * Application use case for creating, validating and saving a connection profile.
 * The UI depends on this orchestration boundary instead of directly coordinating factory and repository calls.
 */
class SaveProfileUseCase(
    private val repository: ProfileRepository,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator
) {
    suspend fun save(draft: ProfileDraft): AppResult<ConnectionProfile> {
        val draftValidation = ProfileDraftValidator.validate(draft)
        if (!draftValidation.isValid) return AppResult.Error(draftValidation.message())

        return runCatching {
            val profile = ProfileFactory.create(draft, timeProvider, idProvider)
            ProfileValidator.validate(profile).requireValid()
            repository.save(profile)
            profile
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.message ?: "Profile could not be saved.", it) }
        )
    }
}
