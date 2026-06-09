package com.msa.iotlab.protocol

import kotlinx.serialization.Serializable

/**
 * Payload requested by the user for one send operation.
 */
@Serializable
data class OutgoingPayload(
    val text: String,
    val encoding: PayloadEncoding = PayloadEncoding.TEXT
)

/**
 * Normalized protocol message used by the live console and Room history log.
 */
@Serializable
data class ProtocolMessage(
    val id: String,
    val sessionId: String? = null,
    val profileId: String? = null,
    val protocol: ProtocolType,
    val direction: MessageDirection,
    val payloadText: String,
    val payloadHex: String,
    val payloadSizeBytes: Int,
    val timestampMillis: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Saved reusable payload template with optional protocol scoping.
 */
@Serializable
data class PayloadTemplate(
    val id: String,
    val name: String,
    val protocol: ProtocolType? = null,
    val encoding: PayloadEncoding = PayloadEncoding.TEXT,
    val payload: String,
    val createdAt: Long,
    val updatedAt: Long
)
