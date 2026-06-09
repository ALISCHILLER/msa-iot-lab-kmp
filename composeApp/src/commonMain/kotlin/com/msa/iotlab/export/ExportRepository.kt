package com.msa.iotlab.export

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.AppJson
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.profile.ProfileRepository
import com.msa.iotlab.security.SecretMasker
import com.msa.iotlab.template.PayloadTemplateRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles JSON import/export for user-owned protocol profiles and payload templates.
 * The repository intentionally depends on domain repositories, not DAOs, to preserve validation rules.
 */
class ExportRepository(
    private val profileRepository: ProfileRepository,
    private val templateRepository: PayloadTemplateRepository,
    private val json: Json = AppJson.pretty,
    private val timeProvider: TimeProvider = AppClock
) {
    suspend fun exportProfilesAndTemplates(policy: ExportSecurityPolicy = ExportSecurityPolicy()): String {
        val profiles = profileRepository.observeProfiles().first()
        val exportProfiles = if (policy.includeSecrets) profiles else profiles.map(SecretMasker::sanitize)
        val bundle = MsaExportBundle(
            exportedAt = timeProvider.nowMillis(),
            profiles = exportProfiles,
            templates = templateRepository.observeAll().first()
        )
        return json.encodeToString(bundle)
    }

    suspend fun decodeBundle(jsonText: String): MsaExportBundle = json.decodeFromString(jsonText)

    suspend fun importBundle(bundle: MsaExportBundle) {
        profileRepository.saveAll(bundle.profiles)
        templateRepository.saveAll(bundle.templates)
    }
}
