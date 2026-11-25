package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AjafLightColorScheme = lightColorScheme(
    primary = AjafNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = AjafNavyContainer,
    onPrimaryContainer = AjafOnNavyContainer,
    secondary = AjafEmerald,
    onSecondary = Color.White,
    secondaryContainer = AjafEmeraldContainer,
    onSecondaryContainer = AjafOnEmeraldContainer,
    tertiary = AjafAmber,
    onTertiary = Color.White,
    tertiaryContainer = AjafAmberContainer,
    onTertiaryContainer = AjafOnAmberContainer,
    error = AjafError,
    onError = Color.White,
    errorContainer = AjafErrorContainer,
    onErrorContainer = AjafOnErrorContainer,
    background = AjafBackgroundLight,
    onBackground = AjafOnSurfaceLight,
    surface = AjafSurfaceLight,
    onSurface = AjafOnSurfaceLight,
    surfaceVariant = AjafSurfaceVariantLight,
    onSurfaceVariant = AjafOnSurfaceVariantLight,
    outline = AjafOutlineLight
)

private val AjafDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7CB7F2),
    onPrimary = Color(0xFF062038),
    primaryContainer = AjafNavyPrimary,
    onPrimaryContainer = Color(0xFFD6E4F0),
    secondary = AjafEmeraldBright,
    onSecondary = Color(0xFF033324),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = AjafAmberBright,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF92400E),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = AjafBackgroundDark,
    onBackground = AjafOnSurfaceDark,
    surface = AjafSurfaceDark,
    onSurface = AjafOnSurfaceDark,
    surfaceVariant = AjafSurfaceVariantDark,
    onSurfaceVariant = AjafOnSurfaceVariantDark,
    outline = AjafOutlineDark
)

@Composable
fun AjafTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AjafDarkColorScheme else AjafLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
