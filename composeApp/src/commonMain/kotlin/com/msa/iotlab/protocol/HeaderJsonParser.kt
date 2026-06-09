package com.msa.iotlab.protocol

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Parses user-entered WebSocket header JSON into a transport-ready string map.
 * The parser only accepts flat JSON objects with primitive values and rejects header injection characters.
 */
object HeaderJsonParser {
    private val headerNamePattern = Regex("^[!#$%&'*+.^_`|~0-9A-Za-z-]+$")

    fun parse(headersJson: String): Map<String, String> {
        val element = Json.parseToJsonElement(headersJson.ifBlank { "{}" })
        require(element is JsonObject) { "Headers must be a JSON object." }
        val headers = mutableMapOf<String, String>()
        element.entries.forEach { (rawKey, value) ->
            val key = rawKey.trim()
            require(key.isValidHeaderName()) { "Header name '$rawKey' is invalid." }
            val normalizedKey = key.lowercase()
            require(headers.keys.none { it.lowercase() == normalizedKey }) { "Duplicate header name '$key'." }
            require(value is JsonPrimitive) { "Header '$key' must be a primitive value." }
            val headerValue = value.contentOrNull ?: value.toString()
            require(headerValue.isSafeHeaderValue()) { "Header '$key' contains forbidden control characters." }
            headers[key] = headerValue.trim()
        }
        return headers
    }

    private fun String.isValidHeaderName(): Boolean = isNotBlank() && headerNamePattern.matches(this)

    private fun String.isSafeHeaderValue(): Boolean = none { it == '\r' || it == '\n' || it.code < 0x20 && it != '\t' }
}
