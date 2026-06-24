package com.example.gymapp.presentation.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build
import com.example.gymapp.BuildConfig
import com.example.gymapp.domain.model.*
import com.example.gymapp.domain.model.sessionExerciseStatus
import com.example.gymapp.ui.theme.*
import com.example.gymapp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    assignmentId: String,
    viewModel: WorkoutSessionViewModel = hiltViewModel(),
    onFinish: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()
    val isFinishing by viewModel.isFinishing.collectAsState()
    val showRatingDialog by viewModel.showRatingDialog.collectAsState()

    LaunchedEffect(assignmentId) {
        viewModel.tryResumeSession(assignmentId)
    }

    when (sessionState) {
        WorkoutSessionState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        "Carregando sessão...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        WorkoutSessionState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text("Erro ao carregar sessão", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Voltar") }
                }
            }
        }
        WorkoutSessionState.Active, WorkoutSessionState.Resumed -> {
            val sessionExercises = viewModel.sessionExercises
            val templateExercises = viewModel.templateExercises
            val session = viewModel.session

            // null = list, index = detail
            var selectedExerciseIndex by remember { mutableStateOf<Int?>(null) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (selectedExerciseIndex == null) {
                    ExerciseListScreen(
                        session = session,
                        sessionState = sessionState,
                        sessionExercises = sessionExercises,
                        templateExercises = templateExercises,
                        onBack = onBack,
                        onSelectExercise = { index -> selectedExerciseIndex = index },
                        onFinishWorkout = { viewModel.showRating() }
                    )
                } else {
                    val idx = selectedExerciseIndex!!
                    val sessionEx = sessionExercises.getOrNull(idx)
                    val templateEx = templateExercises.getOrNull(idx)
                    val exerciseName = sessionEx?.exerciseName ?: templateEx?.exerciseName ?: "Exercício"
                    val defaultSets = templateEx?.defaultSets ?: sessionEx?.sets?.size ?: 3
                    val defaultReps = templateEx?.defaultReps ?: 12
                    val isCardio = sessionEx?.muscleGroup.equals("cardio", ignoreCase = true)
                    val exerciseId = sessionEx?.exerciseId ?: templateEx?.exerciseId
                    val loadHistory by viewModel.loadHistory.collectAsState()
                    val exerciseDetails by viewModel.currentExerciseDetails.collectAsState()

                    LaunchedEffect(idx) {
                        if (exerciseId != null) {
                            viewModel.loadExerciseHistory(exerciseId)
                            viewModel.loadExerciseDetails(exerciseId)
                        }
                    }

                    ExerciseDetailScreen(
                        exerciseName = exerciseName,
                        exerciseDetails = exerciseDetails,
                        sessionEx = sessionEx,
                        templateEx = templateEx,
                        isCardio = isCardio,
                        defaultSets = defaultSets,
                        defaultReps = defaultReps,
                        currentSet = currentSet,
                        loadHistory = loadHistory,
                        isLastExercise = idx == sessionExercises.size - 1,
                        onBack = { selectedExerciseIndex = null },
                        onCompleteSet = { setNum, weight, duration, distance ->
                            val durSec = duration?.let { (it * 60).toInt() }
                            val distM = distance?.let { it * 1000.0 }
                            viewModel.completeSet(setNum, weight, defaultReps, durSec, distM)
                            if (setNum >= defaultSets && idx == sessionExercises.size - 1) {
                                viewModel.showRating()
                            }
                            if (setNum >= defaultSets && idx < sessionExercises.size - 1) {
                                selectedExerciseIndex = null
                            }
                        },
                        onEditSet = { setNum ->
                            val exId = sessionEx?.id ?: ""
                            if (exId.isNotEmpty()) {
                                viewModel.markSetIncomplete(exId, setNum)
                            }
                        }
                    )
                }

                if (showRatingDialog) {
                    RatingDialog(
                        onDismiss = { viewModel.dismissRating() },
                        onConfirm = { rating, feedback ->
                            viewModel.finishWorkout(rating, feedback)
                            onFinish()
                        }
                    )
                }
            }
        }
    }
}

// ============================================================
// EXERCISE LIST SCREEN
// ============================================================
@Composable
private fun ExerciseListScreen(
    session: WorkoutSession?,
    sessionState: WorkoutSessionState,
    sessionExercises: List<SessionExercise>,
    templateExercises: List<TemplateExercise>,
    onBack: () -> Unit,
    onSelectExercise: (Int) -> Unit,
    onFinishWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session?.workoutName ?: "Sessão de Treino",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text(
                                text = "Sessão ${session?.sessionNumber ?: "..."}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (sessionState == WorkoutSessionState.Resumed) {
                                Surface(shape = RoundedCornerShape(Spacing.xs), color = Orange100) {
                                    Text("Retomada", modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Orange600, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onFinishWorkout,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Finalizar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        // Exercise list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                top = Spacing.lg,
                end = Spacing.lg,
                bottom = Spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Text(
                    text = "Escolha um exercício",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            }

            itemsIndexed(sessionExercises) { index, sessionEx ->
                val templateEx = templateExercises.getOrNull(index)
                val exerciseName = sessionEx.exerciseName ?: templateEx?.exerciseName ?: "Exercício ${index + 1}"
                val defaultSets = templateEx?.defaultSets ?: sessionEx.sets?.size ?: 3
                val defaultReps = templateEx?.defaultReps ?: 12
                val isCardio = sessionEx.muscleGroup.equals("cardio", ignoreCase = true)
                val status = sessionEx.status.sessionExerciseStatus

                ExerciseListCard(
                    position = index + 1,
                    name = exerciseName,
                    sets = defaultSets,
                    reps = defaultReps,
                    isCardio = isCardio,
                    status = status.raw,
                    onClick = { onSelectExercise(index) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseListCard(
    position: Int,
    name: String,
    sets: Int,
    reps: Int,
    isCardio: Boolean,
    status: String,
    onClick: () -> Unit
) {
    val isCompleted = status == "completed"
    val isInProgress = status == "in_progress"

    val containerColor = when {
        isCompleted -> IfgGreen.copy(alpha = 0.25f)
        isInProgress -> Orange600.copy(alpha = 0.22f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isCompleted -> IfgGreen.copy(alpha = 0.5f)
        isInProgress -> Orange600.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }
    val numberBgColor = when {
        isCompleted -> IfgGreen
        isInProgress -> Orange600
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val numberTextColor = when {
        isCompleted -> Color.White
        isInProgress -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = if (isInProgress) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(numberBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = "$position", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = numberTextColor)
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isCardio) "${sets}x cardio" else "${sets}x${reps} reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                isCompleted -> {
                    Surface(shape = RoundedCornerShape(Spacing.xs), color = IfgGreen.copy(alpha = 0.25f)) {
                        Text(
                            "Concluído",
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = IfgGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                isInProgress -> {
                    Surface(shape = RoundedCornerShape(Spacing.xs), color = Orange600.copy(alpha = 0.25f)) {
                        Text(
                            "Em andamento",
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Orange600,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                else -> {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// EXERCISE DETAIL SCREEN
// ============================================================
@Composable
private fun ExerciseDetailScreen(
    exerciseName: String,
    exerciseDetails: Exercise?,
    sessionEx: SessionExercise?,
    templateEx: TemplateExercise?,
    isCardio: Boolean,
    defaultSets: Int,
    defaultReps: Int,
    currentSet: Int,
    loadHistory: List<ExerciseProgressPoint>,
    isLastExercise: Boolean,
    onBack: () -> Unit,
    onCompleteSet: (Int, Double?, Double?, Double?) -> Unit,
    onEditSet: (Int) -> Unit
) {
    // Build a set of completed set numbers from actual session data
    val completedSetNumbers = remember(sessionEx) {
        (sessionEx?.sets ?: emptyList())
            .filter { it.isCompleted == true }
            .mapNotNull { it.setNumber }
            .toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar aos exercícios")
                }
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                top = Spacing.lg,
                end = Spacing.lg,
                bottom = Spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                ExerciseInfoCard(
                    exerciseName = exerciseName,
                    exerciseDetails = exerciseDetails,
                    sessionEx = sessionEx,
                    isCardio = isCardio,
                    defaultSets = defaultSets,
                    defaultReps = defaultReps
                )
            }

            if (loadHistory.isNotEmpty()) {
                item { LoadHistoryCard(loadHistory = loadHistory, isCardio = isCardio) }
            }

            items(defaultSets) { setIndex ->
                val setNum = setIndex + 1
                val isCompleted = completedSetNumbers.contains(setNum)
                val isCurrent = setNum == currentSet
                val sessionSet = sessionEx?.sets?.find { it.setNumber == setNum }

                SetRow(
                    setNumber = setNum,
                    reps = defaultReps,
                    isCardio = isCardio,
                    isCurrentSet = isCurrent,
                    isCompleted = isCompleted,
                    sessionSet = sessionSet,
                    onComplete = { weight, duration, distance ->
                        onCompleteSet(setNum, weight, duration, distance)
                    },
                    onEdit = {
                        onEditSet(setNum)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(Spacing.lg)) }
        }
    }
}

@Composable
private fun ExerciseInfoCard(
    exerciseName: String,
    exerciseDetails: Exercise?,
    sessionEx: SessionExercise?,
    isCardio: Boolean,
    defaultSets: Int,
    defaultReps: Int
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Text(text = exerciseName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.sp), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                if (!sessionEx?.muscleGroup.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(Spacing.sm), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                        Text(sessionEx?.muscleGroup ?: "", modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(text = if (isCardio) "${defaultSets}x cardio" else "${defaultSets}x${defaultReps} reps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (!exerciseDetails?.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(text = exerciseDetails?.description ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (exerciseDetails != null) {
                val mediaPath = exerciseDetails.mediaPath
                val mediaType = exerciseDetails.mediaType
                val videoUrl = exerciseDetails.videoUrl

                if (!mediaPath.isNullOrBlank() && !mediaType.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    ExerciseMediaDisplay(mediaPath = mediaPath, mediaType = mediaType, exerciseName = exerciseName)
                } else if (!videoUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    com.example.gymapp.presentation.components.VideoPlayer(videoUrl = videoUrl)
                }
            }
        }
    }
}

@Composable
private fun ExerciseMediaDisplay(mediaPath: String, mediaType: String, exerciseName: String) {
    val context = LocalContext.current
    val fullUrl = "${BuildConfig.SUPABASE_URL}$mediaPath"

    when (mediaType.lowercase()) {
        "image", "gif" -> {
            AsyncImage(
                model = ImageRequest.Builder(context).data(fullUrl)
                    .decoderFactory(if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory())
                    .crossfade(true).build(),
                contentDescription = "Imagem do exercício: $exerciseName",
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(Spacing.md)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        "video" -> {
            com.example.gymapp.presentation.components.VideoPlayer(videoUrl = fullUrl)
        }
        else -> {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Spacing.md), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text("Arquivo anexado ($mediaType)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LoadHistoryCard(loadHistory: List<ExerciseProgressPoint>, isCardio: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Spacing.md), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(text = if (isCardio) "Histórico de Cardio" else "Histórico de Carga", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(Spacing.sm))
            loadHistory.take(3).forEach { entry ->
                val historyText = if (isCardio) {
                    val durMin = entry.totalDurationSeconds?.let { "${it / 60}min" } ?: ""
                    val distKm = entry.totalDistanceMeters?.let { "%.2f km".format(it / 1000.0) } ?: ""
                    "${DateUtils.formatIsoDate(entry.sessionDate)}: $durMin $distKm"
                } else {
                    "${DateUtils.formatIsoDate(entry.sessionDate)}: ${entry.maxWeightKg ?: 0.0} kg"
                }
                Text(text = historyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun SetRow(
    setNumber: Int,
    reps: Int,
    isCardio: Boolean,
    isCurrentSet: Boolean,
    isCompleted: Boolean,
    sessionSet: SessionSet?,
    onComplete: (Double?, Double?, Double?) -> Unit,
    onEdit: () -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var durationInput by remember { mutableStateOf("") }
    var distanceInput by remember { mutableStateOf("") }

    val containerColor = when {
        isCompleted -> IfgGreen.copy(alpha = 0.2f)
        isCurrentSet -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isCurrentSet) 2.dp else 1.dp,
            color = when {
                isCurrentSet -> MaterialTheme.colorScheme.primary
                isCompleted -> IfgGreen.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            // Set number circle
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(
                    when {
                        isCompleted -> IfgGreen
                        isCurrentSet -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = "$setNumber",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isCurrentSet) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            if (isCompleted) {
                // ===== COMPLETED: show registered value + edit button =====
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCardio) "Cardio" else "$reps reps",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val valueText = if (isCardio) {
                        val durMin = sessionSet?.durationSeconds?.let { "${it / 60}min" } ?: ""
                        val distKm = sessionSet?.distanceMeters?.let { "%.2f km".format(it / 1000.0) } ?: ""
                        listOf(durMin, distKm).filter { it.isNotBlank() }.joinToString(" • ")
                    } else {
                        sessionSet?.weightKg?.let { "$it kg" } ?: "Sem registro"
                    }
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar série",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (isCurrentSet) {
                // ===== CURRENT: show input fields + OK button =====
                Text(
                    text = if (isCardio) "Cardio" else "$reps reps",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(0.4f)
                )

                if (isCardio) {
                    OutlinedTextField(value = durationInput, onValueChange = { durationInput = it }, modifier = Modifier.width(72.dp), placeholder = { Text("min", style = MaterialTheme.typography.labelSmall) }, singleLine = true, textStyle = MaterialTheme.typography.bodySmall, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(Spacing.sm))
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    OutlinedTextField(value = distanceInput, onValueChange = { distanceInput = it }, modifier = Modifier.width(72.dp), placeholder = { Text("km", style = MaterialTheme.typography.labelSmall) }, singleLine = true, textStyle = MaterialTheme.typography.bodySmall, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(Spacing.sm))
                } else {
                    OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, modifier = Modifier.width(88.dp), placeholder = { Text("kg", style = MaterialTheme.typography.labelSmall) }, singleLine = true, textStyle = MaterialTheme.typography.bodySmall, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(Spacing.sm))
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                FilledIconButton(
                    onClick = {
                        if (isCardio) onComplete(null, durationInput.toDoubleOrNull(), distanceInput.toDoubleOrNull())
                        else onComplete(weightInput.toDoubleOrNull(), null, null)
                    },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Completar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                }
            } else {
                // ===== FUTURE: just show reps info =====
                Text(
                    text = if (isCardio) "Cardio" else "$reps reps",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RatingDialog(onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var rating by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Avaliar Treino") },
        text = {
            Column {
                Text("Como foi seu treino?", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (star <= rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                OutlinedTextField(value = feedback, onValueChange = { feedback = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Observações (opcional)") }, maxLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary, focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(Spacing.md))
            }
        },
        confirmButton = {
            Button(
                onClick = { if (rating > 0) onConfirm(rating, feedback) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(Spacing.md)
            ) {
                Text("Finalizar", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(Spacing.lg)
    )
}
