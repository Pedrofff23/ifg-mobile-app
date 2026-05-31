package com.example.gymapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IfgGreenLight,
    onPrimary = Color.White,
    primaryContainer = IfgGreenDark,
    onPrimaryContainer = Green100,
    secondary = DarkSurfaceVariant,
    onSecondary = TextOnDark,
    secondaryContainer = DarkSurfaceVariant,
    tertiary = Muted,
    background = DarkBackground,
    onBackground = TextOnDark,
    surface = DarkSurface,
    onSurface = TextOnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextOnDarkSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = DarkSurfaceVariant,
    outlineVariant = TextOnDarkSecondary,
)

private val LightColorScheme = lightColorScheme(
    primary = IfgGreen,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = IfgGreenDark,
    secondary = Secondary,
    onSecondary = TextPrimary,
    secondaryContainer = LightSurfaceVariant,
    tertiary = Muted,
    background = LightBackground,
    onBackground = TextPrimary,
    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    outline = Color(0xFFE2E8F0),
    outlineVariant = TextSecondary,
)

@Composable
fun GymAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
