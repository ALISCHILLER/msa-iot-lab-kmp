package com.msa.iotlab.protocol

/**
 * Aggregated traffic metrics calculated from the bounded live event stream.
 */
data class ProtocolTrafficSnapshot(
    val incomingCount: Int,
    val outgoingCount: Int,
    val systemCount: Int,
    val errorCount: Int,
    val incomingBytes: Int,
    val outgoingBytes: Int,
    val lastEventLabel: String
) {
    /** Total number of live events represented by this snapshot. */
    val totalEvents: Int get() = incomingCount + outgoingCount + systemCount + errorCount
}

/**
 * Stateless analyzer that converts raw protocol events into dashboard-ready traffic metrics.
 */
object ProtocolTrafficAnalyzer {
    fun analyze(events: List<ProtocolEvent>): ProtocolTrafficSnapshot {
        val incoming = events.filterIsInstance<ProtocolEvent.MessageReceived>()
        val outgoing = events.filterIsInstance<ProtocolEvent.MessageSent>()
        val system = events.count { it is ProtocolEvent.System || it is ProtocolEvent.Connected || it is ProtocolEvent.Disconnected }
        val errors = events.count { it is ProtocolEvent.Error }
        return ProtocolTrafficSnapshot(
            incomingCount = incoming.size,
            outgoingCount = outgoing.size,
            systemCount = system,
            errorCount = errors,
            incomingBytes = incoming.sumOf { it.message.payloadSizeBytes },
            outgoingBytes = outgoing.sumOf { it.message.payloadSizeBytes },
            lastEventLabel = events.firstOrNull()?.toLabel() ?: "No traffic yet"
        )
    }

    private fun ProtocolEvent.toLabel(): String = when (this) {
        is ProtocolEvent.Connected -> "Connected"
        is ProtocolEvent.Disconnected -> "Disconnected: ${reason.orEmpty()}".trim()
        is ProtocolEvent.Error -> "Error: $message"
        is ProtocolEvent.MessageReceived -> "IN ${message.payloadSizeBytes} bytes"
        is ProtocolEvent.MessageSent -> "OUT ${message.payloadSizeBytes} bytes"
        is ProtocolEvent.System -> message
    }
}
