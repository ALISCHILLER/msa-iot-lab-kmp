package com.msa.iotlab.console

import com.msa.iotlab.protocol.ProtocolMessage
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.protocol.SessionStatus

/**
 * Persistence port used by console orchestration to record sessions and message logs.
 * Depending on this interface keeps the console domain independent from Room repositories and improves testability.
 */
interface ConsoleHistoryGateway {
    suspend fun startSession(profileId: String?, protocol: ProtocolType, name: String): String
    suspend fun finishSession(sessionId: String, status: SessionStatus)
    suspend fun log(message: ProtocolMessage)
}
