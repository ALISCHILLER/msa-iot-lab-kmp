package com.msa.iotlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Device width buckets used by shared Compose UI to adapt phone, tablet and desktop layouts.
 */
enum class DeviceLayoutClass { Compact, Medium, Expanded }

/**
 * Immutable spacing and sizing decisions derived from the active device layout class.
 */
data class AdaptiveLayoutSpec(
    val deviceClass: DeviceLayoutClass,
    val spacing: Dp,
    val denseSpacing: Dp,
    val maxFormWidth: Dp
) {
    /** True when the UI should avoid side-by-side panes. */
    val singlePane: Boolean get() = deviceClass == DeviceLayoutClass.Compact
}

/**
 * Calculates the app layout class from the currently available width.
 */
fun deviceLayoutClassFor(width: Dp): DeviceLayoutClass = when {
    width < 700.dp -> DeviceLayoutClass.Compact
    width < 1120.dp -> DeviceLayoutClass.Medium
    else -> DeviceLayoutClass.Expanded
}

/**
 * Converts a layout class into reusable spacing and sizing tokens.
 */
fun layoutSpecFor(deviceClass: DeviceLayoutClass): AdaptiveLayoutSpec = when (deviceClass) {
    DeviceLayoutClass.Compact -> AdaptiveLayoutSpec(deviceClass, spacing = 12.dp, denseSpacing = 8.dp, maxFormWidth = 560.dp)
    DeviceLayoutClass.Medium -> AdaptiveLayoutSpec(deviceClass, spacing = 16.dp, denseSpacing = 10.dp, maxFormWidth = 760.dp)
    DeviceLayoutClass.Expanded -> AdaptiveLayoutSpec(deviceClass, spacing = 20.dp, denseSpacing = 12.dp, maxFormWidth = 920.dp)
}

/**
 * Provides responsive layout metadata to screens without exposing BoxWithConstraints everywhere.
 */
@Composable
fun ResponsiveContent(content: @Composable (AdaptiveLayoutSpec) -> Unit) {
    BoxWithConstraints {
        content(layoutSpecFor(deviceLayoutClassFor(maxWidth)))
    }
}

/**
 * Responsive two-pane container: stacked on phones, balanced row on tablet and desktop windows.
 */
@Composable
fun AdaptiveTwoPane(
    modifier: Modifier = Modifier,
    primaryWeight: Float = 1f,
    secondaryWeight: Float = 1f,
    primary: @Composable (AdaptiveLayoutSpec) -> Unit,
    secondary: @Composable (AdaptiveLayoutSpec) -> Unit
) {
    ResponsiveContent { spec ->
        if (spec.singlePane) {
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spec.spacing)) {
                primary(spec)
                secondary(spec)
            }
        } else {
            Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spec.spacing)) {
                Column(Modifier.weight(primaryWeight)) { primary(spec) }
                Column(Modifier.weight(secondaryWeight)) { secondary(spec) }
            }
        }
    }
}

/**
 * Responsive card grid used by dashboard metrics and action surfaces.
 */
@Composable
fun AdaptiveCardGrid(
    itemCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int, Modifier) -> Unit
) {
    ResponsiveContent { spec ->
        val columns = when (spec.deviceClass) {
            DeviceLayoutClass.Compact -> 1
            DeviceLayoutClass.Medium -> 2
            DeviceLayoutClass.Expanded -> 4
        }.coerceAtMost(itemCount.coerceAtLeast(1))
        Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spec.denseSpacing)) {
            (0 until itemCount).chunked(columns).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(spec.denseSpacing), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { index -> content(index, Modifier.weight(1f)) }
                    repeat(columns - rowItems.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
