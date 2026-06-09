package com.msa.iotlab.ui
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ConnectionState
import com.msa.iotlab.protocol.ConnectionStateFormatter
import com.msa.iotlab.protocol.PayloadEncoding
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.canSendPayload
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedLabel
import com.msa.iotlab.i18n.localizedTitle
/**
 * Console event categories used by the live stream filter.
 */
internal enum class ConsoleFilter {
    ALL,
    IN,
    OUT,
    ERROR,
    SYSTEM;
    @Composable
    fun localizedTitle(): String = when (this) {
        ALL -> t("All", "همه")
        IN -> t("Incoming", "دریافتی")
        OUT -> t("Outgoing", "ارسالی")
        ERROR -> t("Errors", "خطاها")
        SYSTEM -> t("System", "سیستمی")
    }
    fun matches(event: ProtocolEvent): Boolean = when (this) {
        ALL -> true
        IN -> event is ProtocolEvent.MessageReceived
        OUT -> event is ProtocolEvent.MessageSent
        ERROR -> event is ProtocolEvent.Error
        SYSTEM -> event is ProtocolEvent.System || event is ProtocolEvent.Connected || event is ProtocolEvent.Disconnected
    }
}
/**
 * Primary console connection controls that stack on compact screens and align on larger ones.
 */
@Composable
internal fun ConsoleToolbar(
    state: ConnectionState,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    ResponsiveActionRow { itemModifier ->
        OutlinedButton(onClick = onBack, modifier = itemModifier) { Text(t("Profiles", "پروفایل‌ها")) }
        Button(onClick = onConnect, enabled = state != ConnectionState.Connected, modifier = itemModifier) { Text(t("Connect", "اتصال")) }
        OutlinedButton(onClick = onDisconnect, modifier = itemModifier) { Text(t("Disconnect", "قطع اتصال")) }
    }
}
/**
 * Compact endpoint summary shown above command controls on every device size.
 */
@Composable
internal fun ConsoleConnectionSummary(profile: ConnectionProfile, state: ConnectionState) {
    ResponsiveContent { spec ->
        val labels = listOf(
            state.localizedLabel(),
            "${profile.host}:${profile.port}",
            profile.payloadEncoding.name,
            if (profile.tlsEnabled) "TLS" else t("PLAIN", "ساده")
        )
        Column(verticalArrangement = Arrangement.spacedBy(spec.denseSpacing)) {
            labels.chunked(if (spec.singlePane) 2 else 4).forEach { rowLabels ->
                Row(horizontalArrangement = Arrangement.spacedBy(spec.denseSpacing), modifier = Modifier.fillMaxWidth()) {
                    rowLabels.forEach { StatusBadge(it, modifier = Modifier.weight(1f)) }
                    repeat((if (spec.singlePane) 2 else 4) - rowLabels.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
/**
 * Payload input editor with adaptive height for phone, tablet and desktop targets.
 */
@Composable
internal fun PayloadComposer(
    payload: String,
    encoding: PayloadEncoding,
    onPayloadChange: (String) -> Unit
) {
    ResponsiveContent { spec ->
        val minHeight = when (spec.deviceClass) {
            DeviceLayoutClass.Compact -> 150.dp
            DeviceLayoutClass.Medium -> 190.dp
            DeviceLayoutClass.Expanded -> 230.dp
        }
        OutlinedTextField(
            value = payload,
            onValueChange = onPayloadChange,
            label = { Text(t("Payload", "Payload") + " (${encoding.localizedTitle()})") },
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight, max = 360.dp),
            minLines = if (spec.singlePane) 5 else 8
        )
    }
}
/**
 * Send and formatter actions for the current payload draft.
 */
@Composable
internal fun PayloadActions(
    state: ConnectionState,
    onSend: () -> Unit,
    onPrettyJson: () -> Unit,
    onMinifyJson: () -> Unit
) {
    ResponsiveActionRow { itemModifier ->
        Button(onClick = onSend, enabled = state.canSendPayload, modifier = itemModifier) { Text(t("Send", "ارسال")) }
        OutlinedButton(onClick = onPrettyJson, modifier = itemModifier) { Text(t("Pretty JSON", "مرتب‌سازی JSON")) }
        OutlinedButton(onClick = onMinifyJson, modifier = itemModifier) { Text(t("Minify", "فشرده‌سازی")) }
    }
}

/**
 * Auto-repeat command controls for heartbeat and polling scenarios.
 */
@Composable
internal fun RepeatControls(
    repeatDelay: String,
    onRepeatDelayChange: (String) -> Unit,
    isRepeating: Boolean,
    canSend: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = repeatDelay,
            onValueChange = onRepeatDelayChange,
            label = { Text(t("Repeat delay millis", "تاخیر تکرار بر حسب میلی‌ثانیه")) },
            modifier = Modifier.fillMaxWidth()
        )
        ResponsiveActionRow { itemModifier ->
            Button(onClick = onStart, enabled = canSend && !isRepeating, modifier = itemModifier) { Text(t("Start repeat", "شروع تکرار")) }
            OutlinedButton(onClick = onStop, enabled = isRepeating, modifier = itemModifier) { Text(t("Stop", "توقف")) }
        }
    }
}

/**
 * Filter chip row for large live console streams with compact wrapping on phones.
 */
@Composable
internal fun ConsoleFilters(selected: ConsoleFilter, onSelected: (ConsoleFilter) -> Unit, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ConsoleFilter.entries.take(3).forEach { filter ->
                FilterChip(selected = selected == filter, onClick = { onSelected(filter) }, label = { Text(filter.localizedTitle()) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ConsoleFilter.entries.drop(3).forEach { filter ->
                FilterChip(selected = selected == filter, onClick = { onSelected(filter) }, label = { Text(filter.localizedTitle()) })
            }
            OutlinedButton(onClick = onClear) { Text(t("Clear", "پاک کردن")) }
        }
    }
}

/**
 * Protocol-specific option summary that lets users verify what the transport will actually do before sending traffic.
 */
@Composable
internal fun ProtocolOptionSummary(profile: ConnectionProfile) {
    val options = when (profile.protocol) {
        com.msa.iotlab.protocol.ProtocolType.MQTT -> listOf(
            t("Client", "کلاینت") + " ${profile.mqtt.clientId}",
            "QoS ${profile.mqtt.qos}",
            if (profile.mqtt.retain) t("Retain", "Retain") else t("No retain", "بدون Retain"),
            t("Pub", "ارسال") + " ${profile.mqtt.publishTopic?.ifBlank { "-" } ?: "-"}",
            t("Sub", "دریافت") + " ${profile.mqtt.subscribeTopic?.ifBlank { "-" } ?: "-"}"
        )
        com.msa.iotlab.protocol.ProtocolType.WEBSOCKET -> listOf(
            t("Path", "مسیر") + " ${profile.websocket.path.ifBlank { "/" }}",
            "Ping ${profile.websocket.pingIntervalMillis}ms",
            if (profile.websocket.headersJson != "{}") t("Headers", "Headerها") else t("No headers", "بدون Header")
        )
        com.msa.iotlab.protocol.ProtocolType.TCP -> listOf(
            t("Line", "خط") + " ${profile.tcp.lineEnding}",
            t("Buffer", "بافر") + " ${profile.tcp.readBufferSize}",
            if (profile.tcp.tcpNoDelay) "NoDelay" else "Nagle"
        )
        com.msa.iotlab.protocol.ProtocolType.UDP -> listOf(
            t("Local", "محلی") + " ${profile.udp.localPort ?: t("auto", "خودکار")}",
            if (profile.udp.broadcastEnabled) "Broadcast" else "Unicast",
            if (profile.udp.listenEnabled) t("Listen", "Listen") else t("Send only", "فقط ارسال"),
            "Buffer ${profile.udp.readBufferSize}"
        )
    }
    ResponsiveContent { spec ->
        Column(verticalArrangement = Arrangement.spacedBy(spec.denseSpacing)) {
            Text(t("Protocol options", "گزینه‌های پروتکل"), style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
            options.chunked(if (spec.singlePane) 2 else 3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spec.denseSpacing), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { StatusBadge(it, modifier = Modifier.weight(1f)) }
                    repeat((if (spec.singlePane) 2 else 3) - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
