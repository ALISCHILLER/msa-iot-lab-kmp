package com.msa.iotlab.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msa.iotlab.i18n.AppLanguage
import com.msa.iotlab.i18n.t

/**
 * Navigation primitives used by the adaptive workbench shell on phone, tablet and desktop.
 */
@Composable
internal fun Sidebar(
    selected: NavigationSection,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigate: (NavigationSection) -> Unit,
    modifier: Modifier
) {
    Surface(modifier = modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("MSA IoT Lab", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(t("Protocol workbench", "میزکار پروتکل"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LanguageSwitcher(language, onLanguageChange)
            NavigationSection.entries.forEach { item -> NavItem(item, item == selected, onNavigate) }
        }
    }
}

@Composable
internal fun TabletRail(
    selected: NavigationSection,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigate: (NavigationSection) -> Unit,
    modifier: Modifier
) {
    Surface(modifier = modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("MSA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LanguageSwitcher(language, onLanguageChange)
            NavigationSection.entries.forEach { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(item) },
                    color = if (item == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (item == selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.title().take(8), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.description().take(12), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
internal fun CompactNavigation(
    selected: NavigationSection,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigate: (NavigationSection) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("MSA IoT Lab", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            LanguageSwitcher(language, onLanguageChange)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            NavigationSection.entries.forEach { item -> CompactNavChip(item, item == selected, onNavigate) }
        }
    }
}

@Composable
private fun CompactNavChip(item: NavigationSection, selected: Boolean, onNavigate: (NavigationSection) -> Unit) {
    Surface(
        modifier = Modifier.clickable { onNavigate(item) },
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.large
    ) {
        Text(item.title(), modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun NavItem(item: NavigationSection, selected: Boolean, onNavigate: (NavigationSection) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onNavigate(item) },
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(item.description(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LanguageSwitcher(language: AppLanguage, onLanguageChange: (AppLanguage) -> Unit) {
    OutlinedButton(onClick = { onLanguageChange(language.toggled()) }) {
        Text(if (language == AppLanguage.English) "FA" else "EN")
    }
}

@Composable
private fun NavigationSection.title(): String = when (this) {
    NavigationSection.Dashboard -> t("Dashboard", "داشبورد")
    NavigationSection.Profiles -> t("Profiles", "پروفایل‌ها")
    NavigationSection.Templates -> t("Templates", "قالب‌ها")
    NavigationSection.History -> t("History", "تاریخچه")
    NavigationSection.Guide -> t("Guide", "راهنما")
    NavigationSection.Settings -> t("Settings", "تنظیمات")
}

@Composable
private fun NavigationSection.description(): String = when (this) {
    NavigationSection.Dashboard -> t("Overview", "نمای کلی")
    NavigationSection.Profiles -> t("Connections", "اتصال‌ها")
    NavigationSection.Templates -> t("Payloads", "Payloadها")
    NavigationSection.History -> t("Sessions", "نشست‌ها")
    NavigationSection.Guide -> t("Manual", "دفترچه")
    NavigationSection.Settings -> t("Import / Export", "ورود / خروج")
}
