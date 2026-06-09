package com.msa.iotlab.protocol

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.payload.PayloadCodec

/**
 * Centralizes ProtocolMessage creation so protocol engines do not duplicate
 * timestamp, identifier, preview, hex and metadata conversion logic.
 */
object ProtocolMessageFactory {
    fun fromBytes(
        profile: ConnectionProfile,
        direction: MessageDirection,
        bytes: ByteArray,
        metadata: Map<String, String> = emptyMap(),
        sessionId: String? = null,
        timeProvider: TimeProvider = AppClock,
        idProvider: IdProvider = IdGenerator
    ): ProtocolMessage = ProtocolMessage(
        id = idProvider.newId(),
        sessionId = sessionId,
        profileId = profile.id,
        protocol = profile.protocol,
        direction = direction,
        payloadText = PayloadCodec.previewSafely(bytes),
        payloadHex = PayloadCodec.toHex(bytes),
        payloadSizeBytes = bytes.size,
        timestampMillis = timeProvider.nowMillis(),
        metadata = metadata
    )

    fun system(
        profile: ConnectionProfile,
        message: String,
        sessionId: String? = null,
        metadata: Map<String, String> = emptyMap(),
        timeProvider: TimeProvider = AppClock,
        idProvider: IdProvider = IdGenerator
    ): ProtocolMessage = text(profile, MessageDirection.SYSTEM, message, sessionId, metadata, timeProvider, idProvider)

    fun error(
        profile: ConnectionProfile,
        message: String,
        sessionId: String? = null,
        metadata: Map<String, String> = emptyMap(),
        timeProvider: TimeProvider = AppClock,
        idProvider: IdProvider = IdGenerator
    ): ProtocolMessage = text(profile, MessageDirection.ERROR, message, sessionId, metadata, timeProvider, idProvider)

    private fun text(
        profile: ConnectionProfile,
        direction: MessageDirection,
        message: String,
        sessionId: String?,
        metadata: Map<String, String>,
        timeProvider: TimeProvider,
        idProvider: IdProvider
    ): ProtocolMessage {
        val bytes = message.encodeToByteArray()
        return ProtocolMessage(
            id = idProvider.newId(),
            sessionId = sessionId,
            profileId = profile.id,
            protocol = profile.protocol,
            direction = direction,
            payloadText = message,
            payloadHex = PayloadCodec.toHex(bytes),
            payloadSizeBytes = bytes.size,
            timestampMillis = timeProvider.nowMillis(),
            metadata = metadata
        )
    }
}
