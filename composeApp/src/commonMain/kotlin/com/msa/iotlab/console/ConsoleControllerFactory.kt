package com.msa.iotlab.console

import com.msa.iotlab.core.AppClock
import com.msa.iotlab.core.IdGenerator
import com.msa.iotlab.core.IdProvider
import com.msa.iotlab.core.TimeProvider
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ProtocolClientFactory

/**
 * Factory for creating profile-scoped console controllers from application-level dependencies.
 * It keeps controller wiring out of Compose screens and preserves dependency inversion for protocol clients.
 */
class ConsoleControllerFactory(
    private val protocolClientFactory: ProtocolClientFactory,
    private val sessionManager: ConsoleSessionManager,
    private val timeProvider: TimeProvider = AppClock,
    private val idProvider: IdProvider = IdGenerator
) {
    fun create(profile: ConnectionProfile): ConsoleController = ConsoleController(
        profile = profile,
        client = protocolClientFactory.create(profile),
        sessionManager = sessionManager,
        timeProvider = timeProvider,
        idProvider = idProvider
    )
}
