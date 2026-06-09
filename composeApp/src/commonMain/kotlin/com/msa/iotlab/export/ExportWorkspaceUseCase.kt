package com.msa.iotlab.export

import com.msa.iotlab.core.AppResult

/**
 * Application use case for producing a portable JSON backup of user profiles and templates.
 * Secrets are redacted by default so generated bundles are safer to share during debugging.
 */
class ExportWorkspaceUseCase(
    private val repository: ExportRepository
) {
    suspend fun exportJson(policy: ExportSecurityPolicy = ExportSecurityPolicy()): AppResult<String> = runCatching {
        repository.exportProfilesAndTemplates(policy)
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.message ?: "Export failed.", it) }
    )
}
