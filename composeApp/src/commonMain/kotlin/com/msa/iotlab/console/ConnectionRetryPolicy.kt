package com.msa.iotlab.console

/**
 * Retry configuration for profile-driven connection attempts.
 * The policy is immutable so it can be safely shared by controllers and tests.
 */
data class ConnectionRetryPolicy(
    val maxAttempts: Int,
    val delayMillis: Long
) {
    companion object {
        /** Default conservative retry behavior for IoT devices that may boot slowly. */
        fun default(): ConnectionRetryPolicy = ConnectionRetryPolicy(maxAttempts = 3, delayMillis = 1_500)

        /** One-shot connection policy used when auto reconnect is disabled. */
        fun singleAttempt(): ConnectionRetryPolicy = ConnectionRetryPolicy(maxAttempts = 1, delayMillis = 0)
    }
}
