package com.msa.iotlab

import com.msa.iotlab.console.ConsoleCommandService
import com.msa.iotlab.core.AppResult
import com.msa.iotlab.protocol.PayloadEncoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

/**
 * Unit tests for outgoing payload validation before transport clients receive data.
 */
class ConsoleCommandServiceTest {
    @Test
    fun invalidHexPayloadReturnsError() {
        val result = ConsoleCommandService.createOutgoingPayload("ABC", PayloadEncoding.HEX, counter = 1)
        assertIs<AppResult.Error>(result)
    }

    @Test
    fun textPayloadExpandsCounterVariable() {
        when (val result = ConsoleCommandService.createOutgoingPayload("ping {counter}", PayloadEncoding.TEXT, counter = 42)) {
            is AppResult.Success -> assertEquals("ping 42", result.data.text)
            is AppResult.Error -> fail(result.message)
        }
    }

    @Test
    fun jsonPayloadIsMinifiedBeforeSending() {
        when (val result = ConsoleCommandService.createOutgoingPayload(
            rawPayload = """{
              "cmd": "status"
            }""",
            encoding = PayloadEncoding.JSON,
            counter = 1
        )) {
            is AppResult.Success -> assertEquals("""{"cmd":"status"}""", result.data.text)
            is AppResult.Error -> fail(result.message)
        }
    }

    @Test
    fun payloadVariablesUseInjectedRuntimeProviders() {
        when (val result = ConsoleCommandService.createOutgoingPayload(
            rawPayload = "ts={timestamp};id={uuid};n={counter}",
            encoding = PayloadEncoding.TEXT,
            counter = 7,
            timeProvider = FixedTimeProvider(1234L),
            idProvider = SequentialIdProvider(mutableListOf("uuid-1"))
        )) {
            is AppResult.Success -> assertEquals("ts=1234;id=uuid-1;n=7", result.data.text)
            is AppResult.Error -> fail(result.message)
        }
    }

}
