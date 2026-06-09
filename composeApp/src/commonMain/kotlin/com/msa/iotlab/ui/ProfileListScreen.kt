package com.msa.iotlab.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.msa.iotlab.profile.ProfileRepository
import com.msa.iotlab.protocol.ConnectionProfile
import com.msa.iotlab.protocol.ProtocolType
import com.msa.iotlab.i18n.t
import com.msa.iotlab.i18n.localizedTitle

/**
 * Responsive profile browser with inventory metrics and fast console launch actions.
 */
@Composable
fun ProfileListScreen(
    repository: ProfileRepository,
    onCreate: () -> Unit,
    onEdit: (ConnectionProfile) -> Unit,
    onOpenConsole: (ConnectionProfile) -> Unit
) {
    val profiles by repository.observeProfiles().collectAsState(emptyList())
    AdaptiveTwoPane(
        primaryWeight = 0.85f,
        secondaryWeight = 1.45f,
        primary = { ProfileInventory(profiles, onCreate) },
        secondary = { ProfileList(profiles, onEdit, onOpenConsole) }
    )
}

@Composable
private fun ProfileInventory(profiles: List<ConnectionProfile>, onCreate: () -> Unit) {
    val byProtocol = ProtocolType.entries.associateWith { protocol -> profiles.count { it.protocol == protocol } }
    SectionCard(t("Profile inventory", "موجودی پروفایل‌ها"), t("Saved endpoints grouped by protocol", "Endpointهای ذخیره‌شده بر اساس پروتکل")) {
        MetricRow(
            ProtocolType.entries.map { protocol ->
                WorkbenchMetric(protocol.localizedTitle(), byProtocol.getValue(protocol).toString(), t("default", "پیش‌فرض") + " ${protocol.defaultPort}")
            }
        )
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text(t("Create new profile", "ساخت پروفایل جدید")) }
        Text(t("Tip: save each backend, broker or device as a profile so the console can persist sessions cleanly.", "نکته: هر بک‌اند، بروکر یا دستگاه را به‌عنوان پروفایل ذخیره کنید تا کنسول نشست‌ها را تمیز نگه دارد."))
    }
}

@Composable
private fun ProfileList(
    profiles: List<ConnectionProfile>,
    onEdit: (ConnectionProfile) -> Unit,
    onOpenConsole: (ConnectionProfile) -> Unit
) {
    SectionCard(t("Saved profiles", "پروفایل‌های ذخیره‌شده"), t("Open a profile to send/receive live traffic", "برای ارسال/دریافت ترافیک زنده یک پروفایل را باز کنید")) {
        if (profiles.isEmpty()) {
            EmptyState(t("No profiles yet", "هنوز پروفایلی ندارید"), t("Create an MQTT, WebSocket, TCP or UDP profile to start testing.", "برای شروع تست یک پروفایل MQTT، وب‌سوکت، TCP یا UDP بسازید."))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 720.dp)) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileRow(profile, onEdit, onOpenConsole)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(profile: ConnectionProfile, onEdit: (ConnectionProfile) -> Unit, onOpenConsole: (ConnectionProfile) -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth().clickable { onOpenConsole(profile) }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ListItem(
                headlineContent = { Text(profile.name, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("${profile.protocol.localizedTitle()} • ${profile.endpointLabel()} • ${profile.payloadEncoding}") }
            )
            ResponsiveActionRow { itemModifier ->
                OutlinedButton(onClick = { onEdit(profile) }, modifier = itemModifier) { Text(t("Edit", "ویرایش")) }
                Button(onClick = { onOpenConsole(profile) }, modifier = itemModifier) { Text(t("Open console", "باز کردن کنسول")) }
            }
            Text(if (profile.autoReconnect) t("Auto reconnect enabled", "اتصال مجدد خودکار فعال") else t("Manual reconnect", "اتصال مجدد دستی"), style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        }
    }
}

private fun ConnectionProfile.endpointLabel(): String = buildString {
    append(if (tlsEnabled) "tls://" else "")
    append(host)
    append(':')
    append(port)
}
