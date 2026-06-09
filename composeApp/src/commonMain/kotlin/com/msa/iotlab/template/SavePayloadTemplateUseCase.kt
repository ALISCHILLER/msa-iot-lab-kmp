package com.msa.iotlab.template

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.validation.PayloadTemplateValidator

/**
 * Application use case for validating and saving reusable payload templates.
 * It keeps ID generation, timestamps and validation outside the Compose screen.
 */
class SavePayloadTemplateUseCase(
    private val repository: PayloadTemplateRepository,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator
) {
    suspend fun save(draft: PayloadTemplateDraft): AppResult<PayloadTemplate> {
        val now = timeProvider.nowMillis()
        val template = PayloadTemplate(
            id = idProvider.newId(),
            name = draft.name.trim(),
            protocol = draft.protocol,
            encoding = draft.encoding,
            payload = draft.payload,
            createdAt = now,
            updatedAt = now
        )
        val validation = PayloadTemplateValidator.validate(template)
        if (!validation.isValid) return AppResult.Error(validation.message())
        return runCatching {
            repository.save(template)
            template
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.message ?: "Template could not be saved.", it) }
        )
    }
}
