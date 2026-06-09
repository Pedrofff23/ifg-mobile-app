package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gymapp.domain.model.*
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(viewModel: ProfessorViewModel, onBack: () -> Unit = {}) {
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Força") }
    var difficulty by remember { mutableStateOf("Iniciante") }
    var workoutDays by remember { mutableStateOf<List<WorkoutDayData>>(listOf(WorkoutDayData(name = "Treino A", orderIndex = 0))) }
    var showExerciseDialog by remember { mutableStateOf(false) }
    var exerciseSearch by remember { mutableStateOf("") }
    var targetDayIndex by remember { mutableIntStateOf(0) }

    val typeOptions = listOf("Força", "Hipertrofia", "Resistência", "Cardio", "Mobilidade")
    var typeExpanded by remember { mutableStateOf(false) }
    val difficultyOptions = listOf("Iniciante", "Intermediário", "Avançado")
    var difficultyExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadExercises() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            if (it.contains("criado", ignoreCase = true)) {
                kotlinx.coroutines.delay(1200)
                onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = { Text("Criar Novo Treino", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IfgGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            item {
                Column {
                    Text("Criar Novo Treino", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text("Monte um treino com múltiplos dias (Treino A, Treino B, ...)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Form Card: name, type, difficulty
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Treino") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))

                        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                            OutlinedTextField(value = type, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))
                            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                typeOptions.forEach { option ->
                                    DropdownMenuItem(text = { Text(option) }, onClick = { type = option; typeExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(expanded = difficultyExpanded, onExpandedChange = { difficultyExpanded = !difficultyExpanded }) {
                            OutlinedTextField(value = difficulty, onValueChange = {}, readOnly = true, label = { Text("Dificuldade") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))
                            ExposedDropdownMenu(expanded = difficultyExpanded, onDismissRequest = { difficultyExpanded = false }) {
                                difficultyOptions.forEach { option ->
                                    DropdownMenuItem(text = { Text(option) }, onClick = { difficulty = option; difficultyExpanded = false })
                                }
                            }
                        }
                    }
                }
            }

            // Workout Days section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dias de Treino", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    FilledTonalButton(
                        onClick = {
                            val nextLabel = generateDayLabel(workoutDays.size)
                            workoutDays = workoutDays + WorkoutDayData(name = nextLabel, orderIndex = workoutDays.size)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Green100, contentColor = IfgGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar Dia")
                    }
                }
            }

            // Workout day cards
            itemsIndexed(workoutDays) { dayIndex, day ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Day header: name input + delete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = day.name,
                                onValueChange = { newName ->
                                    workoutDays = workoutDays.toMutableList().also { it[dayIndex] = day.copy(name = newName) }
                                },
                                label = { Text("Nome do Dia") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                workoutDays = workoutDays.toMutableList().also {
                                    it.removeAt(dayIndex)
                                    // Re-index
                                    it.forEachIndexed { i, d -> it[i] = d.copy(orderIndex = i) }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover dia", tint = Color(0xFFC62828))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Exercises in this day
                        if (day.exercises.isEmpty()) {
                            Text(
                                "Nenhum exercício neste dia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            day.exercises.forEachIndexed { exIdx, ex ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ex.exerciseName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                            Text("${ex.sets}x${ex.reps} • ${ex.restSeconds}s descanso", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = {
                                            workoutDays = workoutDays.toMutableList().also {
                                                val updatedDay = day.copy(exercises = day.exercises.toMutableList().also { it.removeAt(exIdx) })
                                                it[dayIndex] = updatedDay
                                            }
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Add exercise button
                        OutlinedButton(
                            onClick = {
                                targetDayIndex = dayIndex
                                showExerciseDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Adicionar Exercício")
                        }
                    }
                }
            }

            // Save button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val request = CreateTemplateRequest(
                                name = name,
                                type = type,
                                difficulty = difficulty,
                                workoutDays = workoutDays.map { day ->
                                    WorkoutDayInput(
                                        name = day.name,
                                        orderIndex = day.orderIndex,
                                        exercises = day.exercises.mapIndexed { idx, ex ->
                                            TemplateExerciseInput(
                                                exerciseId = ex.exerciseId,
                                                orderIndex = idx,
                                                defaultSets = ex.sets,
                                                defaultReps = ex.reps,
                                                defaultRestSeconds = ex.restSeconds
                                            )
                                        }
                                    )
                                }
                            )
                            viewModel.createTemplate(request)
                        },
                        enabled = name.isNotBlank() && workoutDays.isNotEmpty() && workoutDays.all { it.exercises.isNotEmpty() } && !isLoading,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Criar Treino", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // Exercise selection dialog
    if (showExerciseDialog) {
        val filteredExercises = exercises.filter { it.name.contains(exerciseSearch, ignoreCase = true) || (it.muscleGroup ?: "").contains(exerciseSearch, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { showExerciseDialog = false; exerciseSearch = "" },
            title = { Text("Selecionar Exercício") },
            text = {
                Column {
                    OutlinedTextField(
                        value = exerciseSearch,
                        onValueChange = { exerciseSearch = it },
                        label = { Text("Buscar exercício...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        items(filteredExercises) { exercise ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exercise.name ?: "Sem nome", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(exercise.muscleGroup ?: "Geral", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = {
                                        workoutDays = workoutDays.toMutableList().also {
                                            val day = it[targetDayIndex]
                                            it[targetDayIndex] = day.copy(
                                                exercises = day.exercises + DayExerciseData(
                                                    exerciseId = exercise.id,
                                                    exerciseName = exercise.name ?: "",
                                                    sets = 3,
                                                    reps = 12,
                                                    restSeconds = 60
                                                )
                                            )
                                        }
                                        showExerciseDialog = false
                                        exerciseSearch = ""
                                    }) {
                                        Icon(Icons.Default.AddCircle, contentDescription = "Adicionar", tint = IfgGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExerciseDialog = false; exerciseSearch = "" }) { Text("Concluído") }
            }
        )
    }
}

// Generates Treino A, Treino B, ..., Treino Z, Treino AA, etc.
fun generateDayLabel(index: Int): String {
    val letter = ('A' + (index % 26)).toString()
    val suffix = if (index >= 26) "${index / 26 + 1}" else ""
    return "Treino $letter$suffix"
}

data class WorkoutDayData(
    val name: String,
    val orderIndex: Int,
    val exercises: List<DayExerciseData> = emptyList()
)

data class DayExerciseData(
    val exerciseId: String,
    val exerciseName: String,
    val sets: Int = 3,
    val reps: Int = 12,
    val restSeconds: Int = 60
)
