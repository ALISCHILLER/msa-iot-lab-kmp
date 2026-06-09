package com.msa.iotlab.validation

import com.msa.iotlab.core.DomainValidationException

/**
 * Throws a domain-specific validation exception when the result is invalid.
 * This helper keeps repository validation terse while preserving clear error semantics.
 */
fun ValidationResult.requireValid() {
    when (this) {
        ValidationResult.Valid -> Unit
        is ValidationResult.Invalid -> throw DomainValidationException(message())
    }
}
