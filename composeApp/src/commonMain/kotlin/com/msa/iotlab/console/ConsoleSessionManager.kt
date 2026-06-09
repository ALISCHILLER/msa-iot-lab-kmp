package com.msa.iotlab.console

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolEventLogger
import com.msa.iotlab.protocol.SessionStatus

/**
 * Coordinates console session lifecycle and event persistence, leaving the
 * Compose screen focused on rendering and user interaction only.
 */
class ConsoleSessionManager(
    private val historyGateway: ConsoleHistoryGateway,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator
) {
    suspend fun startIfNeeded(currentSessionId: String?, profile: ConnectionProfile): String {
        return currentSessionId ?: historyGateway.startSession(profile.id, profile.protocol, profile.name)
    }

    suspend fun finish(sessionId: String?, status: SessionStatus = SessionStatus.FINISHED) {
        if (sessionId != null) historyGateway.finishSession(sessionId, status)
    }

    suspend fun persistEvent(event: ProtocolEvent, profile: ConnectionProfile, sessionId: String?) {
        ProtocolEventLogger.toLogMessage(
            event = event,
            profile = profile,
            sessionId = sessionId,
            timeProvider = timeProvider,
            idProvider = idProvider
        )?.let { historyGateway.log(it) }
    }
}
