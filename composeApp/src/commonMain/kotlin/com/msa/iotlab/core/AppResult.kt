package com.msa.iotlab.core

/**
 * Minimal result type for shared application use cases that should not throw.
 */
sealed interface AppResult<out T> {
    /** Successful operation result carrying data. */
    data class Success<T>(val data: T) : AppResult<T>

    /** Failed operation result carrying user-readable error context. */
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
}
