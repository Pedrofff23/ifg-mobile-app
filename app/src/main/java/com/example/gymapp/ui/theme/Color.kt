package com.example.gymapp.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== IFG Brand ====================
val IfgGreen = Color(0xFF0a5a19)
val IfgGreenLight = Color(0xFF22c55e)
val IfgGreenDark = Color(0xFF084a15)
val IfgGreenGradientStart = Color(0xFF0a5a19)
val IfgGreenGradientEnd = Color(0xFF084a15)

// ==================== Accent backgrounds ====================
val Green100 = Color(0xFFbbf7d0)
val Purple100 = Color(0xFFddd6fe)
val Orange100 = Color(0xFFfdba74)
val Yellow100 = Color(0xFFfde68a)
val Blue100 = Color(0xFFbfdbfe)
val Red100 = Color(0xFFfecaca)

// ==================== Semantic ====================
val Red500 = Color(0xFFD4183D)
val Orange600 = Color(0xFFea580c)
val Blue600 = Color(0xFF2563eb)

// ==================== LIGHT SCHEMES ====================

// Light 1: Forest (default - clean white + green accents)
val LightForestBg = Color(0xFFF1F5F9)
val LightForestSurface = Color(0xFFFFFFFF)
val LightForestSurfaceVariant = Color(0xFFE2E8F0)
val LightForestPrimary = Color(0xFF0a5a19)
val LightForestOnPrimary = Color(0xFFFFFFFF)
val LightForestTextPrimary = Color(0xFF111827)
val LightForestTextSecondary = Color(0xFF4B5563)

// Light 2: Ocean (cool blue tones)
val LightOceanBg = Color(0xFFE0F2FE)
val LightOceanSurface = Color(0xFFFFFFFF)
val LightOceanSurfaceVariant = Color(0xFFBAE6FD)
val LightOceanPrimary = Color(0xFF1565C0)
val LightOceanOnPrimary = Color(0xFFFFFFFF)
val LightOceanTextPrimary = Color(0xFF0D2137)
val LightOceanTextSecondary = Color(0xFF334155)

// Light 3: Sand (warm neutral tones)
val LightSandBg = Color(0xFFF5E6D3)
val LightSandSurface = Color(0xFFFFFFFF)
val LightSandSurfaceVariant = Color(0xFFE5D5C0)
val LightSandPrimary = Color(0xFF8B5E34)
val LightSandOnPrimary = Color(0xFFFFFFFF)
val LightSandTextPrimary = Color(0xFF2D1F0E)
val LightSandTextSecondary = Color(0xFF451A03)

// ==================== DARK SCHEMES ====================

// Dark 1: Antigravity (primary requested - #122025 / #1a2334)
val DarkAntigravityBg = Color(0xFF122025)
val DarkAntigravitySurface = Color(0xFF1a2334)
val DarkAntigravitySurfaceVariant = Color(0xFF243447)
val DarkAntigravityPrimary = Color(0xFF22c55e)
val DarkAntigravityOnPrimary = Color(0xFF000000)
val DarkAntigravityTextPrimary = Color(0xFFF0F4F8)
val DarkAntigravityTextSecondary = Color(0xFF8A9BB5)

// Dark 2: Midnight (deep blue-black)
val DarkMidnightBg = Color(0xFF0B1120)
val DarkMidnightSurface = Color(0xFF141E33)
val DarkMidnightSurfaceVariant = Color(0xFF1E2D47)
val DarkMidnightPrimary = Color(0xFF5BA3F5)
val DarkMidnightOnPrimary = Color(0xFF000000)
val DarkMidnightTextPrimary = Color(0xFFE8EDF5)
val DarkMidnightTextSecondary = Color(0xFF7A8DA8)

// Dark 3: Obsidian (dark purple-grey)
val DarkObsidianBg = Color(0xFF16101E)
val DarkObsidianSurface = Color(0xFF1F1830)
val DarkObsidianSurfaceVariant = Color(0xFF2B2344)
val DarkObsidianPrimary = Color(0xFFA78BFA)
val DarkObsidianOnPrimary = Color(0xFF000000)
val DarkObsidianTextPrimary = Color(0xFFEBE5F5)
val DarkObsidianTextSecondary = Color(0xFF9588B2)

// ==================== Legacy compat aliases ====================
// These map to the Antigravity dark theme as the default project feel.
// New code should use MaterialTheme.colorScheme instead.
val Primary = IfgGreen
val Secondary = Color(0xFFF3F4F6)
val Muted = Color(0xFFECECF0)
val Destructive = Red500
val InputBackground = Color(0xFFF3F3F5)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightBackground = DarkAntigravityBg
val TextPrimary = DarkAntigravityTextPrimary
val TextSecondary = DarkAntigravityTextSecondary
