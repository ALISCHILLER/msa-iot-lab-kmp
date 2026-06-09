package com.msa.iotlab.protocol

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider

/**
 * Converts protocol lifecycle events into loggable domain messages.
 * Runtime providers are injectable so console persistence can be tested without wall-clock time or random IDs.
 */
object ProtocolEventLogger {
    fun toLogMessage(
        event: ProtocolEvent,
        profile: ConnectionProfile,
        sessionId: String?,
        timeProvider: TimeProvider = AppClock,
        idProvider: IdProvider = IdGenerator
    ): ProtocolMessage? = when (event) {
        is ProtocolEvent.MessageReceived -> event.message.copy(sessionId = sessionId)
        is ProtocolEvent.MessageSent -> event.message.copy(sessionId = sessionId)
        is ProtocolEvent.System -> ProtocolMessageFactory.system(profile, event.message, sessionId, timeProvider = timeProvider, idProvider = idProvider)
        is ProtocolEvent.Error -> ProtocolMessageFactory.error(profile, event.message, sessionId, timeProvider = timeProvider, idProvider = idProvider)
        is ProtocolEvent.Connected -> ProtocolMessageFactory.system(profile, "Connected", sessionId, timeProvider = timeProvider, idProvider = idProvider)
        is ProtocolEvent.Disconnected -> ProtocolMessageFactory.system(
            profile = profile,
            message = "Disconnected: ${event.reason.orEmpty()}",
            sessionId = sessionId,
            timeProvider = timeProvider,
            idProvider = idProvider
        )
    }
}
