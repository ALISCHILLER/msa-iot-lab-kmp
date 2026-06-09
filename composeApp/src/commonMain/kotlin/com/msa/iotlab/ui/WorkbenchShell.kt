package com.msa.iotlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.iotlab.i18n.AppLanguage

/**
 * Top-level navigation destinations shown by desktop sidebar, tablet rail and phone tabs.
 */
enum class NavigationSection {
    Dashboard,
    Profiles,
    Templates,
    History,
    Guide,
    Settings
}

/**
 * Adaptive application shell that scales from phones to tablets and wide desktop windows.
 */
@Composable
fun WorkbenchShell(
    selected: NavigationSection,
    title: String,
    subtitle: String,
    metrics: List<WorkbenchMetric>,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigate: (NavigationSection) -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val spec = layoutSpecFor(deviceLayoutClassFor(maxWidth))
        when (spec.deviceClass) {
            DeviceLayoutClass.Expanded -> ExpandedShell(selected, title, subtitle, metrics, language, onLanguageChange, onNavigate, content)
            DeviceLayoutClass.Medium -> MediumShell(selected, title, subtitle, metrics, language, onLanguageChange, onNavigate, content)
            DeviceLayoutClass.Compact -> CompactShell(selected, title, subtitle, metrics, language, onLanguageChange, onNavigate, content)
        }
    }
}

@Composable
private fun ExpandedShell(
    selected: NavigationSection,
    title: String,
    subtitle: String,
    metrics: List<WorkbenchMetric>,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigate: (NavigationSection) -> Unit,
    content: @Composable () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        Sidebar(selected, language, onLanguageChange, onNavigate, Modifier.width(304.dp))
        Column(Modifier.weight(1f).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            WorkbenchHeader(title, subtitle, metrics)
            content()
        }
    }
}

@Composable
private fun MediumShell(
    selected: NavigationSection,
    title: String,
    subtitle: String,
    metrics: List<WorkbenchMetric>,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigate: (NavigationSection) -> Unit,
    content: @Composable () -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        TabletRail(selected, language, onLanguageChange, onNavigate, Modifier.width(108.dp))
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WorkbenchHeader(title, subtitle, metrics)
            content()
        }
    }
}

@Composable
private fun CompactShell(
    selected: NavigationSection,
    title: String,
    subtitle: String,
    metrics: List<WorkbenchMetric>,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigate: (NavigationSection) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CompactNavigation(selected, language, onLanguageChange, onNavigate)
        WorkbenchHeader(title, subtitle, metrics)
        content()
    }
}

@Composable
private fun WorkbenchHeader(title: String, subtitle: String, metrics: List<WorkbenchMetric>) {
    SectionCard(title = title, subtitle = subtitle) {
        if (metrics.isNotEmpty()) MetricRow(metrics)
    }
}
