package com.msa.iotlab.payload

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider

/**
 * Expands runtime variables inside payload templates before messages are sent.
 * Supported placeholders: {timestamp}, {uuid}, and {counter}.
 */
object PayloadVariables {
    fun apply(
        input: String,
        counter: Long,
        timeProvider: TimeProvider = AppClock,
        idProvider: IdProvider = IdGenerator
    ): String = input
        .replace("{timestamp}", timeProvider.nowMillis().toString())
        .replace("{uuid}", idProvider.newId())
        .replace("{counter}", counter.toString())
}
