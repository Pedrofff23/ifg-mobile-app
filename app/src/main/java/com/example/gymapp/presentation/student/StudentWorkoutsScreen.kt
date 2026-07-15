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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.domain.model.WorkoutAssignment
import com.example.gymapp.domain.model.WorkoutTemplate
import com.example.gymapp.ui.theme.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.lazy.itemsIndexed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentWorkoutsScreen(
    viewModel: StudentViewModel,
    onStartWorkout: (WorkoutAssignment) -> Unit = {}
) {
    val assignments by viewModel.assignments.collectAsState()
    val currentAssignment by viewModel.currentAssignment.collectAsState()
    val currentTemplate by viewModel.currentTemplate.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedTemplateDetails by viewModel.selectedTemplateDetails.collectAsState()
    val isLoadingTemplateDetails by viewModel.isLoadingTemplateDetails.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAssignments()
        viewModel.loadSessions()
    }

    // When template details start loading, open the sheet
    LaunchedEffect(isLoadingTemplateDetails) {
        if (isLoadingTemplateDetails) {
            showBottomSheet = true
        }
    }

    // Show error as snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearUpdateStatus()
        }
    }


    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                top = Spacing.md,
                end = Spacing.lg,
                bottom = Spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Meus Treinos",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "Treinos atribuídos pelo seu treinador",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Loading
            if (isLoading) {
                items(3) {
                    WorkoutCardSkeleton()
                }
            }

            // Workout cards
            items(assignments) { assignment ->
                val isActive = currentAssignment?.id == assignment.id
                val inProgressSession = if (isActive) {
                    remember(sessions) { sessions.find { it.finishedAt == null } }
                } else null
                val completedTodaySession = if (isActive) {
                    remember(sessions) { sessions.find { it.finishedAt != null && isToday(it.finishedAt) } }
                } else null
                val currentWorkoutDayName = if (isActive) {
                    remember(currentTemplate, assignment) {
                        val index = assignment.currentWorkoutIndex ?: 0
                        currentTemplate?.workoutDays
                            ?.sortedBy { it.orderIndex ?: 0 }
                            ?.getOrNull(index)
                            ?.name
                    }
                } else null

                WorkoutAssignmentCard(
                    assignment = assignment,
                    isActive = isActive,
                    inProgressSession = inProgressSession,
                    completedTodaySession = completedTodaySession,
                    currentWorkoutDayName = currentWorkoutDayName,
                    onStartClick = { onStartWorkout(assignment) },
                    onCardClick = { viewModel.loadTemplateDetails(assignment.templateId) }
                )
            }

            // Empty state
            if (assignments.isEmpty() && !isLoading) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    EmptyState(
                        icon = Icons.Default.FitnessCenter,
                        title = "Nenhum treino atribuído",
                        subtitle = "Seu treinador ainda não atribuiu nenhum treino. Aguarde ou entre em contato."
                    )
                }
            }
        }

        if (showBottomSheet) {
            TemplateDetailsBottomSheet(
                template = selectedTemplateDetails,
                isLoading = isLoadingTemplateDetails,
                onDismiss = {
                    showBottomSheet = false
                    viewModel.clearTemplateDetails()
                }
            )
        }
    }
}

@Composable
private fun WorkoutAssignmentCard(
    assignment: WorkoutAssignment,
    isActive: Boolean,
    inProgressSession: com.example.gymapp.domain.model.WorkoutSession? = null,
    completedTodaySession: com.example.gymapp.domain.model.WorkoutSession? = null,
    currentWorkoutDayName: String? = null,
    onStartClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val isCompleted = isActive && completedTodaySession != null
    val isInProgress = isActive && inProgressSession != null

    val cardBg = when {
        isCompleted -> IfgGreen.copy(alpha = 0.08f)
        isInProgress -> Orange600.copy(alpha = 0.08f)
        isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    val cardBorderColor = when {
        isCompleted -> IfgGreen.copy(alpha = 0.3f)
        isInProgress -> Orange600.copy(alpha = 0.3f)
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    }

    val icon = when {
        isCompleted -> Icons.Default.CheckCircle
        isInProgress -> Icons.Default.DirectionsRun
        else -> Icons.Default.FitnessCenter
    }

    val iconColor = when {
        isCompleted -> IfgGreen
        isInProgress -> Orange600
        isActive -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val iconBg = when {
        isCompleted -> IfgGreen.copy(alpha = 0.1f)
        isInProgress -> Orange600.copy(alpha = 0.1f)
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val buttonText = when {
        isCompleted -> "Treino do dia concluído!"
        isInProgress -> "Retomar Treino"
        else -> "Iniciar Treino"
    }

    val buttonColor = when {
        isCompleted -> MaterialTheme.colorScheme.surfaceVariant
        isInProgress -> Orange600
        else -> if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    }

    val buttonContentColor = when {
        isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> if (isActive || isInProgress) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = cardBorderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.templateName ?: "Treino ${assignment.templateId}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (isActive) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge Status
                            val badgeText = when {
                                isCompleted -> "CONCLUÍDO"
                                isInProgress -> "EM ANDAMENTO"
                                else -> "ATIVO"
                            }
                            val badgeColor = when {
                                isCompleted -> IfgGreen
                                isInProgress -> Orange600
                                else -> MaterialTheme.colorScheme.primary
                            }
                            val badgeBgColor = when {
                                isCompleted -> IfgGreen.copy(alpha = 0.15f)
                                isInProgress -> Orange600.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = badgeBgColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = badgeColor
                                    )
                                }
                            }

                            // Subtext describing workout day/session
                            val subtext = when {
                                isInProgress -> inProgressSession?.workoutName
                                isCompleted -> completedTodaySession?.workoutName
                                else -> currentWorkoutDayName
                            }
                            if (!subtext.isNullOrBlank()) {
                                Text(
                                    text = subtext,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = onStartClick,
                enabled = !isCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = buttonColor,
                    disabledContentColor = buttonContentColor
                ),
                shape = RoundedCornerShape(Spacing.md)
            ) {
                Icon(
                    if (isCompleted) Icons.Default.Check else if (isInProgress) Icons.Default.DirectionsRun else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = buttonContentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    buttonText,
                    color = buttonContentColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun isToday(dateString: String?): Boolean {
    if (dateString.isNullOrBlank()) return false
    return try {
        val dateRegex = """(\d{4}-\d{2}-\d{2})""".toRegex()
        val match = dateRegex.find(dateString)
        if (match != null) {
            val datePart = match.groupValues[1]
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            datePart == todayStr
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}

@Composable
private fun WorkoutCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LoadingSkeleton(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    LoadingSkeleton(modifier = Modifier.width(160.dp).height(16.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    LoadingSkeleton(modifier = Modifier.width(60.dp).height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            LoadingSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(Spacing.md))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateDetailsBottomSheet(
    template: WorkoutTemplate?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            if (isLoading || template == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (!template.type.isNullOrBlank() || !template.difficulty.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        template.type?.let {
                            SuggestionChip(onClick = {}, label = { Text(it) })
                        }
                        template.difficulty?.let {
                            SuggestionChip(onClick = {}, label = { Text(it) })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.md))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    val days = template.workoutDays?.sortedBy { it.orderIndex ?: 0 } ?: emptyList()
                    items(days) { day ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text(
                                    text = day.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(Spacing.sm))
                                
                                val exercises = day.exercises?.sortedBy { it.orderIndex } ?: emptyList()
                                if (exercises.isEmpty()) {
                                    Text(
                                        text = "Nenhum exercício cadastrado",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    exercises.forEachIndexed { idx, ex ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}. ${ex.exerciseName ?: "Exercício"}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = if (ex.defaultReps == null || ex.defaultReps <= 0) "${ex.defaultSets}x cardio" else "${ex.defaultSets}x${ex.defaultReps} reps",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

