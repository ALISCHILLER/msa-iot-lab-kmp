package com.msa.iotlab

import com.msa.iotlab.console.ConsoleEventStore
import com.msa.iotlab.protocol.ProtocolEvent
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the bounded live-console event buffer.
 */
class ConsoleEventStoreTest {
    @Test
    fun keepsOnlyNewestEventsWhenLimitIsExceeded() {
        val store = ConsoleEventStore(maxSize = 2)

        store.add(ProtocolEvent.System("first", timestampMillis = 1))
        store.add(ProtocolEvent.System("second", timestampMillis = 2))
        store.add(ProtocolEvent.System("third", timestampMillis = 3))

        val messages = store.snapshotNewestFirst().map { (it as ProtocolEvent.System).message }
        assertEquals(listOf("third", "second"), messages)
    }

    @Test
    fun clearRemovesAllBufferedEvents() {
        val store = ConsoleEventStore(maxSize = 2)

        store.add(ProtocolEvent.System("event", timestampMillis = 1))
        store.clear()

        assertEquals(emptyList(), store.snapshotNewestFirst())
    }
}
