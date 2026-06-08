package com.example.gymapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reusable theme selection dialog used by both Student and Trainer profile screens.
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: AppThemeScheme,
    onThemeSelected: (AppThemeScheme) -> Unit,
    onDismiss: () -> Unit
) {
    val schemes = AppThemeScheme.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Tema") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Claro",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.filter { !it.isDark }.forEach { scheme ->
                    ThemeOptionRow(
                        scheme = scheme,
                        isSelected = currentTheme == scheme,
                        onClick = { onThemeSelected(scheme) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Escuro",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.filter { it.isDark }.forEach { scheme ->
                    ThemeOptionRow(
                        scheme = scheme,
                        isSelected = currentTheme == scheme,
                        onClick = { onThemeSelected(scheme) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Concluído") }
        }
    )
}

/**
 * Reusable theme option row used by ThemeSelectionDialog.
 */
@Composable
fun ThemeOptionRow(
    scheme: AppThemeScheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when (scheme) {
        AppThemeScheme.LIGHT_FOREST -> LightForestBg
        AppThemeScheme.LIGHT_OCEAN -> LightOceanBg
        AppThemeScheme.LIGHT_SAND -> LightSandBg
        AppThemeScheme.DARK_ANTIGRAVITY -> DarkAntigravityBg
        AppThemeScheme.DARK_MIDNIGHT -> DarkMidnightBg
        AppThemeScheme.DARK_OBSIDIAN -> DarkObsidianBg
    }
    val accentColor = when (scheme) {
        AppThemeScheme.LIGHT_FOREST -> LightForestPrimary
        AppThemeScheme.LIGHT_OCEAN -> LightOceanPrimary
        AppThemeScheme.LIGHT_SAND -> LightSandPrimary
        AppThemeScheme.DARK_ANTIGRAVITY -> DarkAntigravityPrimary
        AppThemeScheme.DARK_MIDNIGHT -> DarkMidnightPrimary
        AppThemeScheme.DARK_OBSIDIAN -> DarkObsidianPrimary
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview swatch
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .align(Alignment.BottomEnd)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = scheme.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
