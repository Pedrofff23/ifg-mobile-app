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
import com.example.gymapp.domain.model.BodyMeasurement
import com.example.gymapp.domain.model.WorkoutSession
import com.example.gymapp.domain.model.ExerciseProgressPoint
import com.example.gymapp.ui.theme.*
import com.example.gymapp.utils.DateUtils
import androidx.compose.ui.res.stringResource
import com.example.gymapp.R
import com.example.gymapp.presentation.components.*

@Composable
fun StudentProgressScreen(viewModel: StudentViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val measurementsChart by viewModel.measurementsChart.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val exerciseCustomMetrics by viewModel.exerciseCustomMetrics.collectAsState()
    val exerciseProgress by viewModel.exerciseProgress.collectAsState()
    val exerciseProgressLoading by viewModel.exerciseProgressLoading.collectAsState()

    var showLogWeightDialog by remember { mutableStateOf(false) }

    // Date range filter state for weight chart
    var weightDateRange by remember { mutableStateOf(DateRangeFilter.LAST_90) }

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
        viewModel.loadProfile()
        viewModel.loadAssignments()
    }

    // Load exercise progress when sessions are loaded (sessions contain exercise IDs)
    LaunchedEffect(sessions) {
        val exerciseIds = (sessions ?: emptyList())
            .flatMap { it.exercises ?: emptyList() }
            .mapNotNull { it.exerciseId }
            .distinct()
        if (exerciseIds.isNotEmpty()) {
            viewModel.loadExerciseProgress(exerciseIds.take(10))
        }
    }

    // Ensure custom metrics are loaded (they are loaded inside loadExerciseProgress)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess != null) {
            snackbarHostState.showSnackbar(updateSuccess!!)
            viewModel.clearUpdateStatus()
        }
    }

    // Error handling
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearUpdateStatus()
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
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.student_progress_title),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = stringResource(R.string.student_progress_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { showLogWeightDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.student_progress_log_weight),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Key Stats — hero layout
            item {
                // Primary stat: weight
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xl),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeroStat(
                            value = profile?.currentWeightKg?.let { "$it kg" } ?: "--",
                            label = stringResource(R.string.student_progress_current_weight),
                            icon = Icons.Default.MonitorWeight
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        HeroStat(
                            value = "${stats?.currentStreak ?: 0}",
                            label = stringResource(R.string.student_progress_streak),
                            icon = Icons.Default.LocalFireDepartment
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        HeroStat(
                            value = "${stats?.completedSessions ?: sessions.count { it.finishedAt != null }}",
                            label = "Completos",
                            icon = Icons.Default.CheckCircle
                        )
                    }
                }
            }

            // Secondary stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    StatChip(
                        value = "${stats?.activeAssignments ?: assignments.size}",
                        label = "Ativos",
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    StatChip(
                        value = String.format("%.1f", stats?.weeklyFrequency ?: 0.0),
                        label = "Freq/Sem",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f),
                        tint = Blue600
                    )
                }
            }

            // ============ BODY WEIGHT CHART ============
            item {
                SectionHeader(title = "Evolução do Peso")
            }

            item {
                DateRangeSelector(
                    selectedRange = weightDateRange,
                    onRangeSelected = { weightDateRange = it }
                )
            }

            item {
                val chartSource = if (measurementsChart.isNotEmpty()) measurementsChart else (measurements ?: emptyList())
                val weightChartData = chartSource
                    .toWeightChartData()
                    .filterByDateRange(weightDateRange)
                val latestWeight = weightChartData.lastOrNull()?.value
                val firstWeight = weightChartData.firstOrNull()?.value
                val weightChange = if (latestWeight != null && firstWeight != null) {
                    val diff = latestWeight - firstWeight
                    String.format("%+.1f kg", diff)
                } else null

                ProgressLineChart(
                    data = weightChartData,
                    title = if (weightChange != null) "Peso Corporal ($weightChange)" else "Peso Corporal",
                    valueLabel = "kg",
                    lineColor = MaterialTheme.colorScheme.primary,
                    showDots = true,
                    showGradient = true
                )
            }

            // ============ EXERCISE PROGRESS CHARTS ============
            if (exerciseProgress.isNotEmpty()) {
                item {
                    SectionHeader(title = "Progresso dos Exercícios")
                }

                if (exerciseProgressLoading) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                // Collect all exercise names from sessions for labeling
                val exerciseNameMap = (sessions ?: emptyList())
                    .flatMap { it.exercises ?: emptyList() }
                    .associate { it.exerciseId to (it.exerciseName ?: it.exerciseId) }

                items(exerciseProgress.entries.toList().take(6)) { (exerciseId, progressPoints) ->
                    val exerciseName = exerciseNameMap[exerciseId] ?: exerciseId
                    val chartData = progressPoints.toExerciseChartData { it.maxWeightKg?.toFloat() }
                    val repData = progressPoints.toExerciseChartData { it.totalReps?.toFloat() }

                    val selectedMetric = exerciseCustomMetrics[exerciseId]?.metricType ?: "weight"
                    // Weight chart with metric selector
                    ExerciseProgressChart(
                        exerciseName = exerciseName,
                        progressPoints = chartData,
                        selectedMetric = selectedMetric,
                        onMetricChanged = { newMetric ->
                            viewModel.setExerciseMetric(exerciseId, newMetric)
                        },
                        lineColor = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    // Reps chart (no custom metric selector needed)
                    if (repData.isNotEmpty()) {
                        ExerciseProgressChart(
                            exerciseName = "$exerciseName — Reps",
                            progressPoints = repData,
                            selectedMetric = "reps",
                            onMetricChanged = { },
                            lineColor = Blue600
                        )
                    }
                }
            }

            // Recent Sessions
            item {
                SectionHeader(title = "Sessões Recentes")
            }

            if (sessions.isNullOrEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.FitnessCenter,
                        title = "Nenhuma sessão ainda",
                        subtitle = "Comece seu primeiro treino para ver o progresso aqui."
                    )
                }
            } else {
                items(sessions.take(5)) { session ->
                    SessionRow(session = session)
                }
            }

            // Measurements list
            val measList = measurements ?: emptyList()
            if (measList.isNotEmpty()) {
                item {
                    SectionHeader(title = "Histórico de Peso")
                }
                items(measList.take(5)) { measurement ->
                    MeasurementRow(measurement = measurement)
                }
            }
        }
    }

    if (showLogWeightDialog) {
        LogWeightDialog(
            isUpdating = isUpdating,
            onDismiss = { showLogWeightDialog = false },
            onSave = { weightKg ->
                viewModel.addMeasurement(weightKg = weightKg)
                showLogWeightDialog = false
            }
        )
    }
}

@Composable
private fun HeroStat(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SessionRow(session: WorkoutSession) {
    val isCompleted = session.finishedAt != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Orange100
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else Orange600,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sessão ${session.sessionNumber ?: "N/A"}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = DateUtils.formatIsoDate(session.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(Spacing.sm),
                color = if (isCompleted) Green100 else Orange100
            ) {
                Text(
                    text = if (isCompleted) "Concluída" else "Em andamento",
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) IfgGreen else Orange600,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MeasurementRow(measurement: BodyMeasurement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column {
                    Text(
                        text = "${measurement.weightKg ?: 0.0} kg",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = DateUtils.formatIsoDate(measurement.measuredAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
