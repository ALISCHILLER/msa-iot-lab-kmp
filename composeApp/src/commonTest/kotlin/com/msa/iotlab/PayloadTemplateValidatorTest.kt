package com.msa.iotlab

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.validation.PayloadTemplateValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests template validation rules used by import and persistence flows.
 */
class PayloadTemplateValidatorTest {
    @Test
    fun rejectsInvalidHexTemplate() {
        val now = AppClock.nowMillis()
        val template = PayloadTemplate(IdGenerator.newId(), "Bad hex", null, PayloadEncoding.HEX, "ABC", now, now)

        assertFalse(PayloadTemplateValidator.validate(template).isValid)
    }

    @Test
    fun acceptsTextTemplate() {
        val now = AppClock.nowMillis()
        val template = PayloadTemplate(IdGenerator.newId(), "Ping", null, PayloadEncoding.TEXT, "ping {counter}", now, now)

        assertTrue(PayloadTemplateValidator.validate(template).isValid)
    }
}
