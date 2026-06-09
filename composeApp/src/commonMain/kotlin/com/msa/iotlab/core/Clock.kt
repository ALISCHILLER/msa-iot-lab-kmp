package com.msa.iotlab.core

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Shared clock wrapper to make timestamp creation consistent and testable.
 */
object AppClock : TimeProvider {
    @OptIn(ExperimentalTime::class)
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
