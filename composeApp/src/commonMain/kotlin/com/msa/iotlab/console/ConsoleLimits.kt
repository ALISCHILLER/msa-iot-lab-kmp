package com.msa.iotlab.console

/**
 * Defines console memory limits so performance-sensitive values are named,
 * documented and easy to tune during production profiling.
 */
object ConsoleLimits {
    const val MAX_LIVE_EVENTS: Int = 800
    const val MIN_REPEAT_DELAY_MS: Long = 100
    const val DEFAULT_REPEAT_DELAY_MS: Long = 1_000
}
