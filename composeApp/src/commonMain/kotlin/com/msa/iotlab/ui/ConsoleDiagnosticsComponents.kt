package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ProtocolFinding
import com.msa.iotlab.protocol.ProtocolFindingSeverity
import com.msa.iotlab.protocol.ProtocolTrafficSnapshot
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedTitle

/**
 * Diagnostic UI components for protocol findings, runtime traffic metrics and endpoint summaries.
 */
@Composable
internal fun ProtocolInspectorPanel(findings: List<ProtocolFinding>) {
    if (findings.isEmpty()) {
        StatusBadge(t("Profile diagnostics passed", "Diagnostic پروفایل موفق بود"))
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(t("Protocol diagnostics", "Diagnostic پروتکل"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        findings.take(5).forEach { finding ->
            val label = when (finding.severity) {
                ProtocolFindingSeverity.INFO -> "INFO"
                ProtocolFindingSeverity.WARNING -> t("WARN", "هشدار")
                ProtocolFindingSeverity.ERROR -> t("ERROR", "خطا")
            }
            SectionCard(
                title = "$label • ${finding.title}",
                subtitle = finding.message,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) { }
        }
    }
}

/**
 * Header strip for the live protocol monitor showing endpoint and latest activity summary.
 */
@Composable
internal fun ProtocolRuntimeHeader(profile: ConnectionProfile, snapshot: ProtocolTrafficSnapshot) {
    ResponsiveContent { spec ->
        val chips = listOf(
            profile.protocol.localizedTitle(),
            "${profile.host}:${profile.port}",
            "${snapshot.totalEvents} ${t("events", "رویداد")}",
            snapshot.lastEventLabel.take(32)
        )
        Column(verticalArrangement = Arrangement.spacedBy(spec.denseSpacing)) {
            chips.chunked(if (spec.singlePane) 2 else 4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spec.denseSpacing), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { StatusBadge(it, modifier = Modifier.weight(1f)) }
                    repeat((if (spec.singlePane) 2 else 4) - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * Metric row specialized for traffic intelligence counters and byte totals.
 */
@Composable
internal fun ConsoleTrafficMetrics(snapshot: ProtocolTrafficSnapshot) {
    MetricRow(
        listOf(
            WorkbenchMetric(t("Incoming", "دریافتی"), snapshot.incomingCount.toString(), "${snapshot.incomingBytes} bytes"),
            WorkbenchMetric(t("Outgoing", "ارسالی"), snapshot.outgoingCount.toString(), "${snapshot.outgoingBytes} bytes"),
            WorkbenchMetric(t("System", "سیستمی"), snapshot.systemCount.toString(), t("lifecycle", "چرخه‌عمر")),
            WorkbenchMetric(t("Errors", "خطاها"), snapshot.errorCount.toString(), t("failures", "شکست‌ها"))
        )
    )
}
