package com.msa.iotlab.payload

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Provides JSON pretty-print and minify operations for console payload editing.
 * Invalid JSON is surfaced to callers as an exception so the UI can decide how to recover.
 */
object JsonFormatter {
    private val pretty = Json { prettyPrint = true }
    private val compact = Json { prettyPrint = false }

    fun prettyPrint(input: String): String {
        val element = Json.parseToJsonElement(input)
        return pretty.encodeToString(JsonElement.serializer(), element)
    }

    fun minify(input: String): String {
        val element = Json.parseToJsonElement(input)
        return compact.encodeToString(JsonElement.serializer(), element)
    }
}
