package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DairyBlueSecondary,
    secondary = DairyGold,
    tertiary = DairyBlueTertiary,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
    primary = DairyBluePrimary,
    secondary = DairyBlueSecondary,
    tertiary = DairyGold,
    background = SurfaceLight,
    surface = Color.White,
    primaryContainer = DairyBlueContainer,
    onPrimaryContainer = OnDairyBlueContainer,
    secondaryContainer = DairyGoldContainer,
    onSecondaryContainer = Color(0xFF7F6000),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight
)

@Composable
fun MilkHisabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
