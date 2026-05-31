package com.example.gymapp.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymapp.domain.model.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.gymapp.ui.theme.*

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

    if (sessionState == WorkoutSessionState.Loading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
    CircularProgressIndicator(color = IfgGreen)
    Spacer(modifier = Modifier.height(16.dp))
    Text("Carregando sessão...", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
    }
    }
    return
    }

    if (sessionState == WorkoutSessionState.Error) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("⚠", fontSize = 36.sp, color = Red500)
    Spacer(modifier = Modifier.height(16.dp))
    Text("Erro ao carregar sessão", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Button(onClick = onBack) { Text("Voltar") }
    }
    }
    return
    }

    // Active or Resumed — show session content
    if (sessionState != WorkoutSessionState.Active && sessionState != WorkoutSessionState.Resumed) return

    val templateExercises = viewModel.templateExercises
    val sessionExercises = viewModel.sessionExercises
    val session = viewModel.session
    val loadHistory by viewModel.loadHistory.collectAsState()
    val totalExercises = maxOf(templateExercises.size, sessionExercises.size)

    // Load exercise history when current exercise changes
    LaunchedEffect(currentExerciseIndex) {
    val sessionEx = sessionExercises.getOrNull(currentExerciseIndex)
    val templateEx = templateExercises.getOrNull(currentExerciseIndex)
    val exerciseId = sessionEx?.exerciseId ?: templateEx?.exerciseId
    if (exerciseId != null) {
    viewModel.loadExerciseHistory(exerciseId)
    }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
        ) {
        IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }
        Column(modifier = Modifier.weight(1f)) {
        Text(
        text = "Sessão de Treino",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
        text = "Sessão ${session?.sessionNumber ?: "..."}",
        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        }
        if (sessionState == WorkoutSessionState.Resumed) {
        Badge(containerColor = Orange100) {
        Text("Retomada", color = Orange600, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.width(8.dp))
        }
        IconButton(onClick = { viewModel.showRating() }) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Finalizar", tint = IfgGreen)
        }
        }

        // Progress Bar
        if (totalExercises > 0) {
            LinearProgressIndicator(
                progress = { (currentExerciseIndex + 1).toFloat() / totalExercises.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = IfgGreen,
                trackColor = LightSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Exercício ${currentExerciseIndex + 1} de $totalExercises",
                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current Exercise Content
        if (currentExerciseIndex < totalExercises && totalExercises > 0) {
            val templateEx = templateExercises.getOrNull(currentExerciseIndex)
            val sessionEx = sessionExercises.getOrNull(currentExerciseIndex)
            val exerciseName = sessionEx?.exerciseName ?: templateEx?.exerciseName ?: "Exercício"
            val defaultSets = templateEx?.defaultSets ?: sessionEx?.sets?.size ?: 3
            val defaultReps = templateEx?.defaultReps ?: 12

            WorkoutExerciseContent(
            exerciseName = exerciseName,
            defaultSets = defaultSets,
            defaultReps = defaultReps,
            currentSet = currentSet,
            currentExerciseIndex = currentExerciseIndex,
            totalExercises = totalExercises,
            loadHistory = loadHistory,
            onCompleteSet = { setNum, weight -> viewModel.completeSet(setNum, weight, defaultReps) },
            onPrevious = { viewModel.previousExercise() },
            onNext = { viewModel.nextExercise() },
            onFinish = { viewModel.showRating() }
            )
        }

        // Rating Dialog
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

@Composable
private fun ColumnScope.WorkoutExerciseContent(
 exerciseName: String,
 defaultSets: Int,
 defaultReps: Int,
 currentSet: Int,
 currentExerciseIndex: Int,
 totalExercises: Int,
 loadHistory: List<ExerciseLoadHistory>,
 onCompleteSet: (Int, Double?) -> Unit,
 onPrevious: () -> Unit,
 onNext: () -> Unit,
 onFinish: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Exercise Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = IfgGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
                )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                text = "${defaultSets}x${defaultReps} reps",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.8f))
                )
                // Load history
                if (loadHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                text = "Histórico de Carga",
                style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
                )
                )
                loadHistory.take(3).forEach { entry ->
                Text(
                text = "${(entry.sessionDate ?: "").take(10)}: ${entry.maxWeightKg} kg",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f))
                )
                }
                }
                }
            }
        }

        // Sets
        items(defaultSets) { setIndex ->
            val setNum = setIndex + 1
            SetRow(
                setNumber = setNum,
                reps = defaultReps,
                isCurrentSet = setNum == currentSet,
                isCompleted = setNum < currentSet,
                onComplete = { weight -> onCompleteSet(setNum, weight) }
            )
        }

        // Navigation buttons
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentExerciseIndex > 0) {
                    OutlinedButton(
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Anterior")
                    }
                }
                if (currentExerciseIndex < totalExercises - 1) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
                    ) {
                        Text("Próximo")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
                    ) {
                        Text("Finalizar")
                    }
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    setNumber: Int,
    reps: Int,
    isCurrentSet: Boolean,
    isCompleted: Boolean,
    onComplete: (Double?) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentSet) 4.dp else 1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentSet) Green100 else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) IfgGreen else if (isCurrentSet) IfgGreenLight else LightSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = "$setNumber",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentSet) Color.White else TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$reps reps",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )

            if (isCurrentSet) {
                OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                modifier = Modifier.width(80.dp),
                placeholder = { Text("kg", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = IfgGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onComplete(weightInput.toDoubleOrNull()) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = IfgGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Completar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RatingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Avaliar Treino") },
        text = {
            Column {
                Text("Como foi seu treino?", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (star <= rating) Color(0xFFFFC107) else TextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Observações (opcional)") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = IfgGreen)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (rating > 0) onConfirm(rating, feedback) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) {
                Text("Finalizar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
