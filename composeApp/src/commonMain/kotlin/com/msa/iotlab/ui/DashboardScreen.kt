package com.msa.iotlab.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.history.HistoryRepository
import com.msa.iotlab.profile.ProfileRepository
import com.msa.iotlab.protocol.ProtocolCapabilityRegistry
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedSummary
import com.msa.iotlab.i18n.localizedTitle

/**
 * Adaptive dashboard for quick protocol creation, workspace metrics and recent activity.
 */
@Composable
fun DashboardScreen(
    profileRepository: ProfileRepository,
    historyRepository: HistoryRepository,
    onOpenProfiles: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenGuide: () -> Unit,
    onCreateProtocol: (ProtocolType) -> Unit
) {
    val profiles by profileRepository.observeProfiles().collectAsState(emptyList())
    val sessions by historyRepository.observeRecentSessions(limit = 5).collectAsState(emptyList())
    val messages by historyRepository.observeRecentMessages(limit = 5).collectAsState(emptyList())
    AdaptiveTwoPane(
        primaryWeight = 1.35f,
        secondaryWeight = 1f,
        primary = { DashboardMain(onCreateProtocol) },
        secondary = {
            DashboardSidePanel(
                profileCount = profiles.size,
                sessionCount = sessions.size,
                messageCount = messages.size,
                onOpenProfiles = onOpenProfiles,
                onOpenHistory = onOpenHistory,
                onOpenTemplates = onOpenTemplates,
                onOpenGuide = onOpenGuide,
                onOpenSettings = onOpenSettings
            )
        }
    )
}

@Composable
private fun DashboardMain(onCreateProtocol: (ProtocolType) -> Unit) {
    SectionCard(
        title = t("Start a protocol test", "شروع تست پروتکل"),
        subtitle = t("Create a saved profile for a backend, broker, gateway or physical IoT device.", "برای بک‌اند، بروکر، gateway یا دستگاه IoT یک پروفایل ذخیره‌شده بسازید.")
    ) {
        AdaptiveCardGrid(itemCount = ProtocolType.entries.size) { index, modifier ->
            ProtocolCard(ProtocolType.entries[index], onCreateProtocol, modifier)
        }
    }
}

@Composable
private fun ProtocolCard(protocol: ProtocolType, onCreateProtocol: (ProtocolType) -> Unit, modifier: Modifier) {
    OutlinedCard(
        modifier = modifier.clickable { onCreateProtocol(protocol) },
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(protocol.localizedTitle(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                StatusBadge(protocol.defaultPort.toString())
            }
            Text(protocol.localizedSummary(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(protocol.describeCapabilities(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DashboardSidePanel(
    profileCount: Int,
    sessionCount: Int,
    messageCount: Int,
    onOpenProfiles: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard(t("Workspace health", "وضعیت Workspace"), t("Current local Room data summary", "خلاصه داده‌های محلی Room")) {
            MetricRow(
                listOf(
                    WorkbenchMetric(t("Profiles", "پروفایل‌ها"), profileCount.toString(), t("saved endpoints", "endpoint ذخیره‌شده")),
                    WorkbenchMetric(t("Sessions", "نشست‌ها"), sessionCount.toString(), t("recent tests", "تست اخیر")),
                    WorkbenchMetric(t("Messages", "پیام‌ها"), messageCount.toString(), t("recent traffic", "ترافیک اخیر")),
                    WorkbenchMetric(t("Targets", "هدف‌ها"), "4", "MQTT / WS / TCP / UDP")
                )
            )
        }
        SectionCard(t("Quick actions", "دسترسی سریع"), t("Jump directly to operational screens", "رفتن سریع به صفحه‌های عملیاتی")) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 320.dp)) {
                item { ActionRow(t("Profiles", "پروفایل‌ها"), t("Create, edit and open connection profiles", "ساخت، ویرایش و باز کردن پروفایل‌های اتصال"), onOpenProfiles) }
                item { ActionRow(t("History", "تاریخچه"), t("Inspect persisted sessions and messages", "بررسی نشست‌ها و پیام‌های ذخیره‌شده"), onOpenHistory) }
                item { ActionRow(t("Templates", "قالب‌ها"), t("Prepare reusable payloads", "آماده‌سازی payloadهای قابل استفاده مجدد"), onOpenTemplates) }
                item { ActionRow(t("Guide", "راهنما"), t("Read the complete operator manual", "خواندن راهنمای کامل کاربری"), onOpenGuide) }
                item { ActionRow(t("Settings", "تنظیمات"), t("Import/export workspace JSON", "ورود/خروج JSON فضای کاری"), onOpenSettings) }
            }
        }
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { OutlinedButton(onClick = onClick) { Text(t("Open", "باز کردن")) } }
    )
}

private fun ProtocolType.summary(): String = when (this) {
    ProtocolType.MQTT -> "Publish, subscribe and monitor broker topics for IoT devices."
    ProtocolType.WEBSOCKET -> "Validate realtime backend gateways using ws/wss endpoints."
    ProtocolType.TCP -> "Send raw text, hex or JSON to TCP-based devices and services."
    ProtocolType.UDP -> "Broadcast discovery packets or listen for datagrams on local networks."
}

private fun ProtocolType.describeCapabilities(): String = ProtocolCapabilityRegistry.capabilitiesFor(this)
    .joinToString(separator = " • ") { it.name.lowercase().replace('_', ' ') }
