package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VantaColorScheme = darkColorScheme(
    primary = VantaPrimary,
    onPrimary = VantaOnPrimary,
    primaryContainer = VantaPrimaryContainer,
    onPrimaryContainer = VantaTextPrimary,
    secondary = VantaSecondary,
    onSecondary = VantaOnSecondary,
    secondaryContainer = VantaSecondaryContainer,
    onSecondaryContainer = VantaTextPrimary,
    tertiary = VantaAccentGold,
    onTertiary = VantaBackground,
    background = VantaBackground,
    onBackground = VantaTextPrimary,
    surface = VantaSurface,
    onSurface = VantaTextPrimary,
    surfaceVariant = VantaSurfaceVariant,
    onSurfaceVariant = VantaTextSecondary,
    surfaceContainer = VantaSurfaceContainer,
    outline = VantaOutline,
    outlineVariant = VantaOutlineVariant,
    error = VantaError,
    onError = VantaOnPrimary
)

@Composable
fun VantaTheme(
    content: @Composable () -> Unit
) {
    // VANTA is strictly an immersive, cinematic dark interface
    MaterialTheme(
        colorScheme = VantaColorScheme,
        typography = Typography,
        content = content
    )
}
