package com.msa.iotlab

import com.msa.iotlab.protocol.HeaderJsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Security-focused tests for WebSocket header parsing and header injection prevention.
 */
class HeaderJsonParserSecurityTest {
    @Test
    fun parserTrimsValidHeaderNames() {
        val headers = HeaderJsonParser.parse("{\" Authorization \":\"Bearer token\"}")

        assertEquals("Bearer token", headers["Authorization"])
    }

    @Test
    fun parserRejectsBlankHeaderNames() {
        assertFailsWith<IllegalArgumentException> {
            HeaderJsonParser.parse("{\"   \":\"value\"}")
        }
    }

    @Test
    fun parserRejectsHeaderNameWithColon() {
        assertFailsWith<IllegalArgumentException> {
            HeaderJsonParser.parse("{\"Bad:Header\":\"value\"}")
        }
    }

    @Test
    fun parserRejectsHeaderValueWithNewline() {
        assertFailsWith<IllegalArgumentException> {
            HeaderJsonParser.parse("{\"X-Test\":\"safe\\nInjected: yes\"}")
        }
    }
    @Test
    fun parserRejectsDuplicateNormalizedHeaderNames() {
        assertFailsWith<IllegalArgumentException> {
            HeaderJsonParser.parse("""{"X-Test":"one"," X-Test ":"two"}""")
        }
    }

}
