package com.msa.iotlab.core

/**
 * Exception thrown when invalid domain data attempts to cross an application boundary.
 * Repositories and import flows use this specific type instead of generic IllegalStateException/error calls.
 */
class DomainValidationException(
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)
