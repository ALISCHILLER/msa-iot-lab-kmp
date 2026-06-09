package com.msa.iotlab

import com.msa.iotlab.protocol.HeaderJsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Unit tests for WebSocket header JSON parsing used by validation and Ktor transport setup.
 */
class HeaderJsonParserTest {
    @Test
    fun parsesFlatHeaderObject() {
        val headers = HeaderJsonParser.parse("""{"Authorization":"Bearer token","x-device":42}""")

        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("42", headers["x-device"])
    }

    @Test
    fun rejectsJsonArrays() {
        assertFailsWith<IllegalArgumentException> {
            HeaderJsonParser.parse("[]")
        }
    }
}
