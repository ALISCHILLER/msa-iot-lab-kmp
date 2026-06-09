package com.msa.iotlab.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFF0F766E),
    tertiary = Color(0xFF7C3AED),
    surface = Color(0xFFFAFBFF),
    surfaceVariant = Color(0xFFE9EEF8),
    background = Color(0xFFF4F7FB),
    error = Color(0xFFB42318)
)
private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4FF),
    primaryContainer = Color(0xFF183B72),
    secondary = Color(0xFF5EEAD4),
    tertiary = Color(0xFFC4B5FD),
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF1F2937),
    background = Color(0xFF0B1120),
    error = Color(0xFFFFB4AB)
)

/**
 * Shared Material 3 theme used by Android, desktop and iOS Compose targets.
 */
@Composable
fun MsaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
