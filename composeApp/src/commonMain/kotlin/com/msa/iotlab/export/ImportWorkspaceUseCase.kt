package com.msa.iotlab.export

import com.msa.iotlab.core.AppResult
import com.msa.iotlab.validation.requireValid

/**
 * Application use case for importing a portable JSON backup with validation and user-readable failures.
 */
class ImportWorkspaceUseCase(
    private val repository: ExportRepository
) {
    suspend fun importJson(jsonText: String): AppResult<Unit> {
        if (jsonText.isBlank()) return AppResult.Error("Import JSON cannot be blank.")
        return runCatching {
            val bundle = repository.decodeBundle(jsonText)
            WorkspaceImportValidator.validate(bundle).requireValid()
            repository.importBundle(bundle)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.message ?: "Import failed.", it) }
        )
    }
}
