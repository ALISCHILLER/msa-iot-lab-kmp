package com.msa.iotlab.validation

import com.msa.iotlab.payload.PayloadCodec
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.PayloadTemplate

/**
 * Validates reusable payload templates before they are saved or imported.
 * The validator protects Room persistence from malformed HEX, BASE64 and JSON-like template payloads.
 */
object PayloadTemplateValidator {
    fun validate(template: PayloadTemplate): ValidationResult {
        val errors = buildList {
            if (template.name.isBlank()) add("Template name is required.")
            if (template.payload.isBlank()) add("Template payload cannot be blank.")
            runCatching { PayloadCodec.encode(template.payload, template.encoding) }
                .onFailure { add("Template payload is invalid for ${template.encoding}: ${it.message}") }
            if (template.encoding == PayloadEncoding.JSON && !template.payload.trim().startsWith('{') && !template.payload.trim().startsWith('[')) {
                add("JSON templates should start with an object or array.")
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}
