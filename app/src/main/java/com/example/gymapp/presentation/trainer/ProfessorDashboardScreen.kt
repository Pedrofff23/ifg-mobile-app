package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.ui.theme.*

@Composable
fun ProfessorDashboardScreen(
    viewModel: ProfessorViewModel,
    onNavigate: (String) -> Unit = {}
) {
    val students by viewModel.students.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Styled snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        if (isLoading && students.isEmpty() && templates.isEmpty() && exercises.isEmpty() && announcements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Header
                Column {
                    Text(
                        text = "Painel do Professor",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Visão geral da academia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    StatChip(
                        value = "${students.size}",
                        label = "Alunos",
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    StatChip(
                        value = "${templates.size}",
                        label = "Treinos",
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    StatChip(
                        value = "${exercises.size}",
                        label = "Exercícios",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        modifier = Modifier.weight(1f),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    StatChip(
                        value = "${announcements.size}",
                        label = "Avisos",
                        icon = Icons.Default.Notifications,
                        modifier = Modifier.weight(1f),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Quick Actions
                SectionHeader(title = "Ações Rápidas")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    QuickActionButton(
                        label = "Treinos",
                        icon = Icons.Default.FitnessCenter,
                        onClick = { onNavigate("workout_hub") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        label = "Alunos",
                        icon = Icons.Default.People,
                        onClick = { onNavigate("students_hub") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    QuickActionButton(
                        label = "Novo Aviso",
                        icon = Icons.Default.Add,
                        onClick = { onNavigate("announcements") },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        label = "Exercícios",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        onClick = { onNavigate("workout_hub") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Recent Activity
                SectionHeader(title = "Atividade Recente")

                if (templates.isNotEmpty()) {
                    Text(
                        text = "Treinos",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        templates.take(3).forEach { template ->
                            RecentActivityItem(
                                title = template.name,
                                subtitle = "${template.type} • ${template.difficulty}",
                                icon = Icons.Default.FitnessCenter
                            )
                        }
                    }
                }

                if (announcements.isNotEmpty()) {
                    Text(
                        text = "Avisos",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        announcements.take(3).forEach { announcement ->
                            RecentActivityItem(
                                title = announcement.title,
                                subtitle = announcement.content.take(60) + if (announcement.content.length > 60) "…" else "",
                                icon = Icons.Default.Notifications
                            )
                        }
                    }
                }

                if (templates.isEmpty() && announcements.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        title = "Nenhuma atividade recente",
                        subtitle = "Crie treinos e avisos para aparecerem aqui."
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RecentActivityItem(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
