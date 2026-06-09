package com.example.gymapp.presentation.student

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.domain.model.WorkoutAssignment
import com.example.gymapp.ui.theme.*

@Composable
fun StudentHomeScreen(
    viewModel: StudentViewModel,
    onStartWorkout: (WorkoutAssignment) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null
) {
    val assignments by viewModel.assignments.collectAsState()
    val currentAssignment by viewModel.currentAssignment.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStudentData()
    }

    // Clear error when dismissed
    LaunchedEffect(error) {
        if (error != null && snackbarHostState != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearUpdateStatus()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            top = Spacing.md,
            end = Spacing.lg,
            bottom = Spacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Welcome Banner
        item {
            WelcomeBanner(
                title = "Olá, ${userName ?: "Atleta"}! 👋",
                subtitle = "Pronto para o treino de hoje?",
                gradientColors = listOf(IfgGreen, IfgGreenDark)
            )
        }

        // Today's Workout — highlighted card
        item {
            val activeAssignment = currentAssignment
                ?: assignments.firstOrNull { it.endsAt == null }
                ?: assignments.firstOrNull()

            if (activeAssignment != null) {
                TodayWorkoutCard(
                    assignment = activeAssignment,
                    sessionsCompleted = stats?.completedSessions ?: 0,
                    onStartWorkout = { onStartWorkout(activeAssignment) }
                )
            } else {
                EmptyWorkoutCard()
            }
        }

        // Stats Row — compact chips
        item {
            SectionHeader(title = "Suas Estatísticas")
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                StatChip(
                    value = "${stats?.completedSessions ?: 0}",
                    label = "Concluídos",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f),
                    tint = IfgGreen
                )
                StatChip(
                    value = "${stats?.currentStreak ?: 0}",
                    label = "Sequência",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f),
                    tint = Orange600
                )
                StatChip(
                    value = String.format("%.1f", stats?.weeklyFrequency ?: 0.0),
                    label = "Freq.",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f),
                    tint = Blue600
                )
            }
        }

        // Quick Actions
        item {
            SectionHeader(title = "Ações Rápidas")
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                QuickActionCard(
                    label = "Ver Progresso",
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    onClick = { onNavigate("progress") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    label = "Biblioteca",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    onClick = { onNavigate("workout_hub") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Loading
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xxxl),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun TodayWorkoutCard(
    assignment: WorkoutAssignment,
    sessionsCompleted: Int,
    onStartWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Treino de Hoje",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = assignment.templateName ?: "Treino ${assignment.templateId}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$sessionsCompleted sessões realizadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Large icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen),
                shape = RoundedCornerShape(Spacing.md)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    "Iniciar Treino",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyWorkoutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = "Nenhum treino atribuído",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
