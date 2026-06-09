package com.msa.iotlab.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Compact metric model used by dashboard and desktop workbench headers.
 */
data class WorkbenchMetric(val label: String, val value: String, val hint: String? = null)

/**
 * Reusable elevated card that groups one feature or form section.
 */
@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

/**
 * Read-only status badge with a soft container background.
 */
@Composable
fun StatusBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
    }
}

/**
 * Metric card optimized for desktop dashboard grids.
 */
@Composable
fun MetricCard(metric: WorkbenchMetric, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(metric.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(metric.label, style = MaterialTheme.typography.labelLarge)
            if (metric.hint != null) Text(metric.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Empty state panel used when profiles, templates or history records are missing.
 */
@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Displays metric cards using one, two or four columns depending on available width.
 */
@Composable
fun MetricRow(metrics: List<WorkbenchMetric>, modifier: Modifier = Modifier) {
    AdaptiveCardGrid(itemCount = metrics.size, modifier = modifier) { index, itemModifier ->
        MetricCard(metrics[index], modifier = itemModifier)
    }
}


/**
 * Wraps important actions and supplies a safe item modifier for compact and wide layouts.
 */
@Composable
fun ResponsiveActionRow(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    ResponsiveContent { spec ->
        if (spec.singlePane) {
            Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spec.denseSpacing)) {
                content(Modifier.fillMaxWidth())
            }
        } else {
            Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spec.denseSpacing)) {
                content(Modifier.weight(1f))
            }
        }
    }
}

/**
 * Small horizontal spacer helper for dense desktop action rows.
 */
@Composable
fun ActionSpacer() {
    Spacer(Modifier.width(8.dp).height(1.dp))
}
