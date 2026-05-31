package com.example.gymapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightForestScheme = lightColorScheme(
    primary = LightForestPrimary,
    onPrimary = LightForestOnPrimary,
    primaryContainer = Green100,
    onPrimaryContainer = IfgGreenDark,
    secondary = Secondary,
    onSecondary = LightForestTextPrimary,
    secondaryContainer = LightForestSurfaceVariant,
    tertiary = Muted,
    background = LightForestBg,
    onBackground = LightForestTextPrimary,
    surface = LightForestSurface,
    onSurface = LightForestTextPrimary,
    surfaceVariant = LightForestSurfaceVariant,
    onSurfaceVariant = LightForestTextSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = Color(0xFFE2E8F0),
    outlineVariant = LightForestTextSecondary,
)

private val LightOceanScheme = lightColorScheme(
    primary = LightOceanPrimary,
    onPrimary = LightOceanOnPrimary,
    primaryContainer = Blue100,
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFFE3F2FD),
    onSecondary = LightOceanTextPrimary,
    secondaryContainer = LightOceanSurfaceVariant,
    tertiary = Muted,
    background = LightOceanBg,
    onBackground = LightOceanTextPrimary,
    surface = LightOceanSurface,
    onSurface = LightOceanTextPrimary,
    surfaceVariant = LightOceanSurfaceVariant,
    onSurfaceVariant = LightOceanTextSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = Color(0xFFCFD8DC),
    outlineVariant = LightOceanTextSecondary,
)

private val LightSandScheme = lightColorScheme(
    primary = LightSandPrimary,
    onPrimary = LightSandOnPrimary,
    primaryContainer = Yellow100,
    onPrimaryContainer = Color(0xFF5D3A1A),
    secondary = Color(0xFFF5EDE0),
    onSecondary = LightSandTextPrimary,
    secondaryContainer = LightSandSurfaceVariant,
    tertiary = Muted,
    background = LightSandBg,
    onBackground = LightSandTextPrimary,
    surface = LightSandSurface,
    onSurface = LightSandTextPrimary,
    surfaceVariant = LightSandSurfaceVariant,
    onSurfaceVariant = LightSandTextSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = Color(0xFFDDD0BC),
    outlineVariant = LightSandTextSecondary,
)

private val DarkAntigravityScheme = darkColorScheme(
    primary = DarkAntigravityPrimary,
    onPrimary = DarkAntigravityOnPrimary,
    primaryContainer = IfgGreenDark,
    onPrimaryContainer = Green100,
    secondary = DarkAntigravitySurfaceVariant,
    onSecondary = DarkAntigravityTextPrimary,
    secondaryContainer = DarkAntigravitySurfaceVariant,
    tertiary = Color(0xFF2A3A4E),
    background = DarkAntigravityBg,
    onBackground = DarkAntigravityTextPrimary,
    surface = DarkAntigravitySurface,
    onSurface = DarkAntigravityTextPrimary,
    surfaceVariant = DarkAntigravitySurfaceVariant,
    onSurfaceVariant = DarkAntigravityTextSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = DarkAntigravitySurfaceVariant,
    outlineVariant = DarkAntigravityTextSecondary,
)

private val DarkMidnightScheme = darkColorScheme(
    primary = DarkMidnightPrimary,
    onPrimary = DarkMidnightOnPrimary,
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFB3D4FF),
    secondary = DarkMidnightSurfaceVariant,
    onSecondary = DarkMidnightTextPrimary,
    secondaryContainer = DarkMidnightSurfaceVariant,
    tertiary = Color(0xFF162340),
    background = DarkMidnightBg,
    onBackground = DarkMidnightTextPrimary,
    surface = DarkMidnightSurface,
    onSurface = DarkMidnightTextPrimary,
    surfaceVariant = DarkMidnightSurfaceVariant,
    onSurfaceVariant = DarkMidnightTextSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = DarkMidnightSurfaceVariant,
    outlineVariant = DarkMidnightTextSecondary,
)

private val DarkObsidianScheme = darkColorScheme(
    primary = DarkObsidianPrimary,
    onPrimary = DarkObsidianOnPrimary,
    primaryContainer = Color(0xFF3B2563),
    onPrimaryContainer = Color(0xFFD4BAFF),
    secondary = DarkObsidianSurfaceVariant,
    onSecondary = DarkObsidianTextPrimary,
    secondaryContainer = DarkObsidianSurfaceVariant,
    tertiary = Color(0xFF241A38),
    background = DarkObsidianBg,
    onBackground = DarkObsidianTextPrimary,
    surface = DarkObsidianSurface,
    onSurface = DarkObsidianTextPrimary,
    surfaceVariant = DarkObsidianSurfaceVariant,
    onSurfaceVariant = DarkObsidianTextSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = DarkObsidianSurfaceVariant,
    outlineVariant = DarkObsidianTextSecondary,
)

fun getColorSchemeForTheme(scheme: AppThemeScheme) = when (scheme) {
    AppThemeScheme.LIGHT_FOREST -> LightForestScheme
    AppThemeScheme.LIGHT_OCEAN -> LightOceanScheme
    AppThemeScheme.LIGHT_SAND -> LightSandScheme
    AppThemeScheme.DARK_ANTIGRAVITY -> DarkAntigravityScheme
    AppThemeScheme.DARK_MIDNIGHT -> DarkMidnightScheme
    AppThemeScheme.DARK_OBSIDIAN -> DarkObsidianScheme
}

@Composable
fun GymAppTheme(
    themeScheme: AppThemeScheme = AppThemeScheme.DARK_ANTIGRAVITY,
    content: @Composable () -> Unit
) {
    val colorScheme = getColorSchemeForTheme(themeScheme)
    val isDark = themeScheme.isDark

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
