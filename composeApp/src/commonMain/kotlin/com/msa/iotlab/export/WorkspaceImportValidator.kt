package com.msa.iotlab.export

import com.msa.iotlab.validation.PayloadTemplateValidator
import com.msa.iotlab.validation.ProfileValidator
import com.msa.iotlab.validation.ValidationResult

/**
 * Validates imported workspace bundles before they are persisted.
 * The validator guards backup restore operations from corrupt schema versions, duplicate identifiers and invalid domain objects.
 */
object WorkspaceImportValidator {
    private const val SUPPORTED_SCHEMA_VERSION = 1

    fun validate(bundle: MsaExportBundle): ValidationResult {
        val errors = buildList {
            if (bundle.app != "MSA IoT Lab") add("Unsupported export app: ${bundle.app}.")
            if (bundle.schemaVersion != SUPPORTED_SCHEMA_VERSION) add("Unsupported export schema version: ${bundle.schemaVersion}.")
            addDuplicateErrors("profile", bundle.profiles.map { it.id })
            addDuplicateErrors("template", bundle.templates.map { it.id })
            bundle.profiles.forEachIndexed { index, profile ->
                when (val validation = ProfileValidator.validate(profile)) {
                    ValidationResult.Valid -> Unit
                    is ValidationResult.Invalid -> add("Profile #${index + 1} '${profile.name}' is invalid: ${validation.message()}")
                }
            }
            bundle.templates.forEachIndexed { index, template ->
                when (val validation = PayloadTemplateValidator.validate(template)) {
                    ValidationResult.Valid -> Unit
                    is ValidationResult.Invalid -> add("Template #${index + 1} '${template.name}' is invalid: ${validation.message()}")
                }
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    private fun MutableList<String>.addDuplicateErrors(label: String, ids: List<String>) {
        ids.groupingBy { it }.eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .forEach { duplicatedId -> add("Duplicate $label id in import bundle: $duplicatedId") }
    }
}
