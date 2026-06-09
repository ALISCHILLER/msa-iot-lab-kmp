package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.console.ConsoleControllerFactory
import com.msa.iotlab.console.ConsoleLimits
import com.msa.iotlab.i18n.localizedTitle
import com.msa.iotlab.payload.DefaultPayloadProvider
import com.msa.iotlab.payload.JsonFormatter
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.PayloadTemplate
import com.msa.iotlab.protocol.ProtocolEvent
import com.msa.iotlab.protocol.ProtocolFinding
import com.msa.iotlab.protocol.ProtocolProfileInspector
import com.msa.iotlab.protocol.ProtocolTrafficAnalyzer
import com.msa.iotlab.protocol.ProtocolTrafficSnapshot
import com.msa.iotlab.protocol.canSendPayload
import com.msa.iotlab.template.PayloadTemplateRepository
import com.msa.iotlab.i18n.t

/**
 * High-density adaptive live console for protocol command composition, runtime diagnostics and traffic monitoring.
 */
@Composable
fun ConsoleScreen(
    profile: ConnectionProfile,
    controllerFactory: ConsoleControllerFactory,
    templateRepository: PayloadTemplateRepository,
    onBack: () -> Unit
) {
    val controller = remember(profile.id, controllerFactory) { controllerFactory.create(profile) }
    val state by controller.connectionState.collectAsState()
    val events by controller.events.collectAsState()
    val isRepeating by controller.isRepeating.collectAsState()
    val templates by templateRepository.observeForProtocol(profile.protocol).collectAsState(emptyList())
    val findings = remember(profile) { ProtocolProfileInspector.inspect(profile) }
    val trafficSnapshot = remember(events) { ProtocolTrafficAnalyzer.analyze(events) }
    var payload by remember(profile.id) { mutableStateOf(DefaultPayloadProvider.forEncoding(profile.payloadEncoding)) }
    var filter by remember { mutableStateOf(ConsoleFilter.ALL) }
    var repeatDelay by remember { mutableStateOf(ConsoleLimits.DEFAULT_REPEAT_DELAY_MS.toString()) }

    DisposableEffect(controller) { onDispose { controller.close() } }

    AdaptiveTwoPane(
        primaryWeight = 0.9f,
        secondaryWeight = 1.35f,
        primary = {
            ConsoleCommandPanel(
                profile = profile,
                state = state,
                payload = payload,
                onPayloadChange = { payload = it },
                templates = templates,
                findings = findings,
                repeatDelay = repeatDelay,
                onRepeatDelayChange = { repeatDelay = it },
                isRepeating = isRepeating,
                onBack = onBack,
                onConnect = { controller.connect() },
                onDisconnect = { controller.disconnect() },
                onSend = { controller.send(payload, profile.payloadEncoding) },
                onPrettyJson = { payload = runCatching { JsonFormatter.prettyPrint(payload) }.getOrElse { payload } },
                onMinifyJson = { payload = runCatching { JsonFormatter.minify(payload) }.getOrElse { payload } },
                onStartRepeat = { controller.startRepeat(payload, profile.payloadEncoding, repeatDelay) },
                onStopRepeat = { controller.stopRepeat() }
            )
        },
        secondary = { spec ->
            ConsoleEventPanel(
                profile = profile,
                events = events,
                snapshot = trafficSnapshot,
                filter = filter,
                onFilter = { filter = it },
                onClear = { controller.clearEvents() },
                compact = spec.singlePane
            )
        }
    )
}

@Composable
private fun ConsoleCommandPanel(
    profile: ConnectionProfile,
    state: com.msa.iotlab.protocol.ConnectionState,
    payload: String,
    onPayloadChange: (String) -> Unit,
    templates: List<PayloadTemplate>,
    findings: List<ProtocolFinding>,
    repeatDelay: String,
    onRepeatDelayChange: (String) -> Unit,
    isRepeating: Boolean,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSend: () -> Unit,
    onPrettyJson: () -> Unit,
    onMinifyJson: () -> Unit,
    onStartRepeat: () -> Unit,
    onStopRepeat: () -> Unit
) {
    SectionCard(t("Command center", "مرکز فرمان"), "${profile.name} • ${profile.protocol.localizedTitle()}") {
        ConsoleToolbar(state, onBack, onConnect, onDisconnect)
        ConsoleConnectionSummary(profile, state)
        ProtocolInspectorPanel(findings)
        ProtocolOptionSummary(profile)
        PayloadComposer(payload, profile.payloadEncoding, onPayloadChange)
        PayloadActions(state = state, onSend, onPrettyJson, onMinifyJson)
        RepeatControls(repeatDelay, onRepeatDelayChange, isRepeating, state.canSendPayload, onStartRepeat, onStopRepeat)
        TemplatePicker(templates, onUse = onPayloadChange)
    }
}

@Composable
private fun TemplatePicker(templates: List<PayloadTemplate>, onUse: (String) -> Unit) {
    if (templates.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(t("Payload library", "کتابخانه Payload"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.heightIn(max = 190.dp)) {
            items(templates, key = { it.id }) { template ->
                SectionCard(
                    title = template.name,
                    subtitle = "${template.encoding.name} • ${template.protocol?.localizedTitle() ?: t("Any protocol", "همه پروتکل‌ها")}",
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text(template.payload.take(160), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { onUse(template.payload) }, modifier = Modifier.fillMaxWidth()) { Text(t("Load into composer", "بارگذاری در ویرایشگر")) }
                }
            }
        }
    }
}

@Composable
private fun ConsoleEventPanel(
    profile: ConnectionProfile,
    events: List<ProtocolEvent>,
    snapshot: ProtocolTrafficSnapshot,
    filter: ConsoleFilter,
    onFilter: (ConsoleFilter) -> Unit,
    onClear: () -> Unit,
    compact: Boolean
) {
    SectionCard(t("Traffic intelligence", "تحلیل ترافیک"), t("Live stream, byte counters and protocol runtime state", "جریان زنده، شمارنده byte و وضعیت runtime پروتکل")) {
        ProtocolRuntimeHeader(profile, snapshot)
        ConsoleTrafficMetrics(snapshot)
        ConsoleFilters(filter, onFilter, onClear)
        val visibleEvents = remember(events, filter) { events.filter { filter.matches(it) } }
        if (visibleEvents.isEmpty()) EmptyState(t("No events", "رویدادی وجود ندارد"), t("Connect and send a payload to see protocol traffic here.", "برای دیدن ترافیک پروتکل، متصل شوید و یک payload ارسال کنید."))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(min = if (compact) 300.dp else 460.dp, max = 820.dp)) {
            items(visibleEvents) { event -> EventCard(event) }
        }
    }
}

