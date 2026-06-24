package com.example.gymapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightForestScheme = lightColorScheme(
    primary = LightForestPrimary,
    onPrimary = LightForestOnPrimary,
    primaryContainer = Green100,
    onPrimaryContainer = IfgGreenDark,
    secondary = Color(0xFF1565C0),
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Color(0xFF0D47A1),
    tertiary = Color(0xFFE65100),
    onTertiary = Color.White,
    tertiaryContainer = Orange100,
    onTertiaryContainer = Color(0xFFBF360C),
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
    secondary = Color(0xFF0097A7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF006064),
    tertiary = Color(0xFF512DA8),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1C4E9),
    onTertiaryContainer = Color(0xFF311B92),
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
    secondary = Color(0xFF388E3C),
    onSecondary = Color.White,
    secondaryContainer = Green100,
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiary = Color(0xFFD32F2F),
    onTertiary = Color.White,
    tertiaryContainer = Red100,
    onTertiaryContainer = Color(0xFFB71C1C),
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
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF003366),
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFD1E9FF),
    tertiary = Color(0xFFC084FC),
    onTertiary = Color(0xFF3B0764),
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFF3E8FF),
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
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFF9575CD),
    onTertiary = Color(0xFF311B92),
    tertiaryContainer = Color(0xFF4527A0),
    onTertiaryContainer = Color(0xFFD1C4E9),
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
    secondary = Color(0xFFF06292),
    onSecondary = Color(0xFF560027),
    secondaryContainer = Color(0xFF880E4F),
    onSecondaryContainer = Color(0xFFF8BBD0),
    tertiary = Color(0xFFF59E0B),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
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
            // Status bar and Navigation bar are handled by enableEdgeToEdge() in MainActivity
            // We only need to control the appearance of the icons (light/dark)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
