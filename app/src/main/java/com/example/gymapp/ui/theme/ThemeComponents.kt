package com.example.gymapp.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// SPACING SYSTEM — consistent 8dp grid
// ============================================================
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// ============================================================
// SECTION HEADER — consistent section titles
// ============================================================
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        action?.invoke()
    }
}

// ============================================================
// ERROR BANNER — proper icon instead of emoji
// ============================================================
@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Erro",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(
                    "Fechar",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ============================================================
// PREMIUM STAT CARD — varied sizes, better hierarchy
// ============================================================
@Composable
fun PremiumStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconBgColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isLarge: Boolean = false
) {
    val cardElevation = if (isLarge) 4.dp else 2.dp
    val iconSize = if (isLarge) 56.dp else 44.dp
    val iconInnerSize = if (isLarge) 28.dp else 22.dp
    val valueSize = if (isLarge) 28.sp else 22.sp

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(if (isLarge) Spacing.xl else Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(iconInnerSize)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = valueSize,
                    color = accentColor
                )
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================
// COMPACT STAT CHIP — for inline stats
// ============================================================
@Composable
fun StatChip(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Spacing.sm),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = tint
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// LOADING SKELETON — shimmer placeholder
// ============================================================
@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by shimmerTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.sm))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
            )
    )
}

@Composable
fun StatCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingSkeleton(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            LoadingSkeleton(
                modifier = Modifier
                    .width(48.dp)
                    .height(20.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            LoadingSkeleton(
                modifier = Modifier
                    .width(64.dp)
                    .height(12.dp)
            )
        }
    }
}

// ============================================================
// PREMIUM CARD — elevated surface with subtle border
// ============================================================
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val card = @Composable {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(Spacing.md),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            content = { Column(modifier = Modifier.padding(Spacing.lg), content = content) }
        )
    }
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            shape = RoundedCornerShape(Spacing.md),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            content = { Column(modifier = Modifier.padding(Spacing.lg), content = content) }
        )
    } else {
        card()
    }
}

// ============================================================
// WELCOME BANNER — rich gradient with depth
// ============================================================
@Composable
fun WelcomeBanner(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(IfgGreen, IfgGreenDark)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.lg))
            .background(
                brush = Brush.horizontalGradient(colors = gradientColors)
            )
            .padding(Spacing.lg)
    ) {
        // Subtle decorative circles
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 40.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(x = (-10).dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp
                )
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
        }
    }
}

// ============================================================
// EMPTY STATE — consistent empty state with icon
// ============================================================
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            action()
        }
    }
}

// ============================================================
// THEME SELECTION DIALOG (existing, kept for compatibility)
// ============================================================
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
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
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

                Spacer(modifier = Modifier.height(Spacing.sm))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.xs))

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
        shape = RoundedCornerShape(Spacing.sm),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(Spacing.xs))
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
            Spacer(modifier = Modifier.width(Spacing.md))
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

// ============================================================
// ANIMATED CONTENT FADE-IN
// ============================================================
@Composable
fun FadeInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        content()
    }
}
