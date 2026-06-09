package com.msa.iotlab.console

import com.msa.iotlab.protocol.ProtocolEvent

/**
 * Small bounded event buffer used by tests and non-Compose orchestration code.
 * Compose screens may still mirror this behavior with snapshot state for rendering.
 */
class ConsoleEventStore(
    private val maxSize: Int = ConsoleLimits.MAX_LIVE_EVENTS
) {
    private val items: ArrayDeque<ProtocolEvent> = ArrayDeque()

    fun add(event: ProtocolEvent) {
        items.addLast(event)
        while (items.size > maxSize) items.removeFirst()
    }

    fun snapshotNewestFirst(): List<ProtocolEvent> = items.toList().asReversed()

    fun clear() {
        items.clear()
    }
}
