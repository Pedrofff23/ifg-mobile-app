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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.domain.model.BodyMeasurement
import com.example.gymapp.domain.model.WorkoutSession
import com.example.gymapp.domain.model.ExerciseProgressPoint
import com.example.gymapp.domain.model.Exercise
import com.example.gymapp.ui.theme.*
import com.example.gymapp.utils.DateUtils
import androidx.compose.ui.res.stringResource
import java.util.Locale
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
    val exercises by viewModel.exercises.collectAsState()
    val selectedExerciseId by viewModel.selectedExerciseId.collectAsState()

    var showLogWeightDialog by remember { mutableStateOf(false) }
    var showExerciseSelectionDialog by remember { mutableStateOf(false) }
    var exerciseSearchQuery by remember { mutableStateOf("") }

    // Date range filter state for weight chart
    var weightDateRange by remember { mutableStateOf(DateRangeFilter.LAST_90) }

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
        viewModel.loadProfile()
        viewModel.loadMeasurementsChart()
        viewModel.loadAssignments()
        viewModel.loadExercises()
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
                        value = String.format(Locale.US, "%.1f", stats?.weeklyFrequency ?: 0.0),
                        label = "Freq/Sem",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f),
                        tint = Blue600
                    )
                }
            }

            // ============ IMC CARD ============
            item {
                IMCCard(
                    weightKg = profile?.currentWeightKg,
                    heightCm = profile?.heightCm
                )
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
            item {
                SectionHeader(title = "Progresso em Exercícios")
            }

            item {
                val selectedExercise = exercises.find { it.id == selectedExerciseId }
                Card(
                    onClick = { showExerciseSelectionDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.md),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = selectedExercise?.name ?: "Selecionar Exercício",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selectedExercise != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedExercise?.muscleGroup?.replaceFirstChar { it.uppercase() } ?: "Clique para escolher um exercício e ver seu histórico",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Selecionar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (selectedExerciseId != null) {
                val progressPoints = exerciseProgress[selectedExerciseId] ?: emptyList()
                val selectedExercise = exercises.find { it.id == selectedExerciseId }
                val exerciseName = selectedExercise?.name ?: ""

                if (exerciseProgressLoading && progressPoints.isEmpty()) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                } else if (progressPoints.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Box(modifier = Modifier.padding(Spacing.lg).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Nenhum histórico de treino encontrado para este exercício.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    item {
                        val chartData = progressPoints.toExerciseChartData { it.maxWeightKg?.toFloat() }
                        val repData = progressPoints.toExerciseChartData { it.totalReps?.toFloat() }
                        val selectedMetric = exerciseCustomMetrics[selectedExerciseId]?.metricType ?: "weight"

                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            ExerciseProgressChart(
                                exerciseName = "$exerciseName — Carga Máxima",
                                progressPoints = chartData,
                                selectedMetric = selectedMetric,
                                onMetricChanged = { newMetric ->
                                    viewModel.setExerciseMetric(selectedExerciseId!!, newMetric)
                                },
                                lineColor = MaterialTheme.colorScheme.primary
                            )

                            if (repData.isNotEmpty()) {
                                ExerciseProgressChart(
                                    exerciseName = "$exerciseName — Total de Repetições",
                                    progressPoints = repData,
                                    selectedMetric = "reps",
                                    onMetricChanged = { },
                                    lineColor = Blue600
                                )
                            }
                        }
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

    if (showExerciseSelectionDialog) {
        AlertDialog(
            onDismissRequest = { 
                showExerciseSelectionDialog = false
                exerciseSearchQuery = ""
            },
            title = { Text("Selecionar Exercício") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedTextField(
                        value = exerciseSearchQuery,
                        onValueChange = { exerciseSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar exercício...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(Spacing.md)
                    )

                    val filteredExercises = exercises.filter {
                        it.name.contains(exerciseSearchQuery, ignoreCase = true) ||
                        (it.muscleGroup?.contains(exerciseSearchQuery, ignoreCase = true) ?: false)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        if (filteredExercises.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nenhum exercício encontrado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(filteredExercises) { exercise ->
                                Card(
                                    onClick = {
                                        viewModel.selectExerciseForProgress(exercise.id)
                                        showExerciseSelectionDialog = false
                                        exerciseSearchQuery = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (exercise.id == selectedExerciseId) 
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else Color.Transparent
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = exercise.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = exercise.muscleGroup?.replaceFirstChar { it.uppercase() } ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (exercise.id == selectedExerciseId) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showExerciseSelectionDialog = false
                    exerciseSearchQuery = ""
                }) {
                    Text("Fechar")
                }
            },
            shape = RoundedCornerShape(Spacing.lg)
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
                        if (isCompleted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
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
                color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = if (isCompleted) "Concluída" else "Em andamento",
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
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

@Composable
private fun IMCCard(
    weightKg: Double?,
    heightCm: Double?
) {
    val hasMetrics = weightKg != null && heightCm != null && weightKg > 0 && heightCm > 0
    
    val bmi = if (hasMetrics) {
        val heightM = heightCm!! / 100.0
        weightKg!! / (heightM * heightM)
    } else 0.0

    val categoryTitle: String
    val categoryDesc: String
    val statusColor: Color
    val statusBgColor: Color

    if (!hasMetrics) {
        categoryTitle = "Dados incompletos"
        categoryDesc = "Insira seu peso e altura na tela de Perfil para calcular seu IMC."
        statusColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        statusBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    } else if (bmi < 18.5) {
        categoryTitle = "Abaixo do peso"
        categoryDesc = "Seu peso está abaixo do ideal recomendado pela OMS. É importante consultar um profissional de saúde."
        statusColor = Orange600
        statusBgColor = Orange600.copy(alpha = 0.08f)
    } else if (bmi < 25.0) {
        categoryTitle = "Peso saudável"
        categoryDesc = "Parabéns! Seu peso está na faixa saudável recomendada pela OMS. Continue assim!"
        statusColor = IfgGreen
        statusBgColor = IfgGreen.copy(alpha = 0.08f)
    } else if (bmi < 30.0) {
        categoryTitle = "Sobrepeso"
        categoryDesc = "Você está na faixa de sobrepeso. Praticar atividades físicas e ajustar a dieta pode ajudar."
        statusColor = Orange600
        statusBgColor = Orange600.copy(alpha = 0.08f)
    } else {
        categoryTitle = "Obesidade"
        categoryDesc = "Seu IMC aponta obesidade. Recomendamos consultar profissionais de saúde para orientação adequada."
        statusColor = MaterialTheme.colorScheme.error
        statusBgColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = statusBgColor),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Índice de Massa Corporal (IMC)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (hasMetrics) String.format(Locale.US, "%.1f", bmi) else "--",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (hasMetrics) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasMetrics) {
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "kg/m²",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Category Badge
                Surface(
                    shape = RoundedCornerShape(Spacing.sm),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = categoryTitle,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = categoryDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Scale bar if metrics exist
            if (hasMetrics) {
                Spacer(modifier = Modifier.height(Spacing.md))
                IMCScaleBar(bmi = bmi)
            }
        }
    }
}

@Composable
private fun IMCScaleBar(bmi: Double) {
    val minBmi = 15.0
    val maxBmi = 35.0
    val fraction = ((bmi - minBmi) / (maxBmi - minBmi)).coerceIn(0.0, 1.0).toFloat()
    val bias = fraction * 2f - 1f

    Column {
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFBC02D), // Amarelo
                            Color(0xFF4CAF50), // Verde
                            Color(0xFFFFA000), // Laranja
                            Color(0xFFE53935)  // Vermelho
                        )
                    )
                )
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Indicator needle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 8.dp)
                    .align(androidx.compose.ui.BiasAlignment(bias, 0f))
                    .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(2.dp))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("15.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("18.5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("25.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("30.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("35.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
