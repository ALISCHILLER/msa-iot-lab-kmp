package com.msa.iotlab

import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolMessage
import com.msa.iotlab.protocol.ProtocolTrafficAnalyzer
import com.msa.iotlab.protocol.ProtocolType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies live traffic aggregation used by the console traffic intelligence panel.
 */
class ProtocolTrafficAnalyzerTest {
    @Test
    fun analyzeCountsDirectionsAndBytes() {
        val events = listOf(
            ProtocolEvent.MessageReceived(message("in", MessageDirection.IN, 5)),
            ProtocolEvent.MessageSent(message("out", MessageDirection.OUT, 7)),
            ProtocolEvent.System("ready", 1L),
            ProtocolEvent.Error("boom", timestampMillis = 2L)
        )
        val snapshot = ProtocolTrafficAnalyzer.analyze(events)
        assertEquals(1, snapshot.incomingCount)
        assertEquals(1, snapshot.outgoingCount)
        assertEquals(1, snapshot.systemCount)
        assertEquals(1, snapshot.errorCount)
        assertEquals(5, snapshot.incomingBytes)
        assertEquals(7, snapshot.outgoingBytes)
    }

    private fun message(id: String, direction: MessageDirection, bytes: Int): ProtocolMessage = ProtocolMessage(
        id = id,
        protocol = ProtocolType.TCP,
        direction = direction,
        payloadText = "x".repeat(bytes),
        payloadHex = "00",
        payloadSizeBytes = bytes,
        timestampMillis = 1L
    )
}
