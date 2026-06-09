package com.msa.iotlab.payload

/**
 * Central policy for user payload size limits before data is handed to protocol transports.
 * It protects the UI and socket layers from accidental huge payloads during stress testing.
 */
object PayloadSizePolicy {
    const val DEFAULT_MAX_BYTES: Int = 1_048_576

    fun validate(sizeBytes: Int, maxBytes: Int = DEFAULT_MAX_BYTES) {
        require(sizeBytes <= maxBytes) { "Payload is $sizeBytes bytes; maximum allowed is $maxBytes bytes." }
    }
}
