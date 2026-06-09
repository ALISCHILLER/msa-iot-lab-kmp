package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.history.HistoryRepository
import com.msa.iotlab.protocol.MessageDirection
import com.msa.iotlab.protocol.ProtocolMessage
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedTitle

/**
 * Adaptive history workbench that displays sessions and traffic side by side on large screens.
 */
@Composable
fun HistoryScreen(historyRepository: HistoryRepository) {
    val sessions by historyRepository.observeRecentSessions().collectAsState(emptyList())
    val messages by historyRepository.observeRecentMessages().collectAsState(emptyList())
    AdaptiveTwoPane(
        primaryWeight = 0.9f,
        secondaryWeight = 1.25f,
        primary = { SessionPanel(sessions) },
        secondary = { MessagePanel(messages) }
    )
}

@Composable
private fun SessionPanel(sessions: List<com.msa.iotlab.database.SessionEntity>) {
    SectionCard(t("Recent sessions", "نشست‌های اخیر"), t("Lifecycle of console runs", "چرخه‌عمر اجرای کنسول")) {
        MetricRow(
            listOf(
                WorkbenchMetric(t("Sessions", "نشست‌ها"), sessions.size.toString(), t("loaded", "بارگذاری‌شده")),
                WorkbenchMetric(t("Running", "در حال اجرا"), sessions.count { it.status == "RUNNING" }.toString(), t("active", "فعال")),
                WorkbenchMetric(t("Failed", "ناموفق"), sessions.count { it.status == "FAILED" }.toString(), t("errors", "خطاها")),
                WorkbenchMetric(t("Finished", "تمام‌شده"), sessions.count { it.status == "FINISHED" }.toString(), t("closed", "بسته‌شده"))
            )
        )
        if (sessions.isEmpty()) EmptyState(t("No sessions", "نشستی وجود ندارد"), t("Open a profile console and connect to create history.", "برای ساخت history یک پروفایل را در کنسول باز کنید و متصل شوید."))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 720.dp)) {
            items(sessions, key = { it.id }) { session -> SessionRow(session) }
        }
    }
}

@Composable
private fun SessionRow(session: com.msa.iotlab.database.SessionEntity) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(session.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text("${session.protocol} • ${session.status} • ${session.startedAt} → ${session.endedAt ?: "running"}") },
            trailingContent = { StatusBadge(session.status) }
        )
    }
}

@Composable
private fun MessagePanel(messages: List<ProtocolMessage>) {
    SectionCard(t("Recent traffic", "ترافیک اخیر"), t("Persisted IN / OUT / SYSTEM / ERROR events", "رویدادهای ذخیره‌شده IN / OUT / SYSTEM / ERROR")) {
        MetricRow(
            listOf(
                WorkbenchMetric(t("Incoming", "دریافتی"), messages.count { it.direction == MessageDirection.IN }.toString(), t("received", "دریافت‌شده")),
                WorkbenchMetric(t("Outgoing", "ارسالی"), messages.count { it.direction == MessageDirection.OUT }.toString(), t("sent", "ارسال‌شده")),
                WorkbenchMetric(t("System", "سیستمی"), messages.count { it.direction == MessageDirection.SYSTEM }.toString(), "diagnostic"),
                WorkbenchMetric(t("Errors", "خطاها"), messages.count { it.direction == MessageDirection.ERROR }.toString(), t("failures", "شکست‌ها"))
            )
        )
        if (messages.isEmpty()) EmptyState(t("No messages", "پیامی وجود ندارد"), t("Traffic logs appear here after live console events are persisted.", "بعد از ذخیره رویدادهای کنسول زنده، logهای ترافیک اینجا نمایش داده می‌شوند."))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 640.dp)) {
            items(messages, key = { it.id }) { message -> MessageRow(message) }
        }
    }
}

@Composable
private fun MessageRow(message: ProtocolMessage) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ListItem(
                headlineContent = { Text("${message.direction} • ${message.protocol.localizedTitle()} • ${message.payloadSizeBytes} bytes") },
                supportingContent = { Text(message.timestampMillis.toString()) },
                trailingContent = { StatusBadge(message.direction.name) }
            )
            Text(message.payloadText.take(520), fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth())
        }
    }
}
