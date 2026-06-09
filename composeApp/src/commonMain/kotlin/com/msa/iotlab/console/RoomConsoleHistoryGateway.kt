package com.msa.iotlab.console

import com.msa.iotlab.history.HistoryRepository
import com.msa.iotlab.protocol.ProtocolMessage
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.protocol.SessionStatus

/**
 * Room-backed adapter for console session persistence.
 * It implements the console history port and isolates controllers from database-specific repository details.
 */
class RoomConsoleHistoryGateway(
    private val historyRepository: HistoryRepository
) : ConsoleHistoryGateway {
    override suspend fun startSession(profileId: String?, protocol: ProtocolType, name: String): String {
        return historyRepository.startSession(profileId, protocol, name)
    }

    override suspend fun finishSession(sessionId: String, status: SessionStatus) {
        historyRepository.finishSession(sessionId, status)
    }

    override suspend fun log(message: ProtocolMessage) {
        historyRepository.log(message)
    }
}
