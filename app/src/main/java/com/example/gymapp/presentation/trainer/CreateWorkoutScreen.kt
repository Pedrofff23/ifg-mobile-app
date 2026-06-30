package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.domain.model.*
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(viewModel: ProfessorViewModel, onBack: () -> Unit = {}) {
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var hasHandledSuccess by remember { mutableStateOf(false) }

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

    // Handle success - navigate back ONCE
    LaunchedEffect(successMessage) {
        val msg = successMessage
        if (msg != null && msg.contains("criado", ignoreCase = true) && !hasHandledSuccess) {
            hasHandledSuccess = true
            viewModel.clearSuccessMessage()
            onBack()
        }
    }

    // Handle error
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = { Text("Criar Novo Treino", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
                                Icon(Icons.Default.Delete, contentDescription = "Remover dia", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Exercises in this day (with drag-and-drop reordering)
                        if (day.exercises.isEmpty()) {
                            Text(
                                "Nenhum exercício neste dia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            day.exercises.forEachIndexed { exIdx, ex ->
                                val dragOffset = remember { mutableStateOf(0f) }
                                val isDragging = remember { mutableStateOf(false) }

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDragging.value) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .offset(y = dragOffset.value.dp)
                                        .pointerInput(exIdx) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { isDragging.value = true },
                                                onDragEnd = {
                                                    isDragging.value = false
                                                    dragOffset.value = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffset.value += dragAmount.y
                                                },
                                                onDragCancel = {
                                                    isDragging.value = false
                                                    dragOffset.value = 0f
                                                }
                                            )
                                        }
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        // Drag handle
                                        Icon(
                                            Icons.Default.DragHandle,
                                            contentDescription = "Arrastar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${exIdx + 1}. ${ex.exerciseName}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                            Text("${ex.sets}x${ex.reps} • ${ex.restSeconds}s descanso", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = {
                                            workoutDays = workoutDays.toMutableList().also {
                                                val updatedDay = day.copy(exercises = day.exercises.toMutableList().also { it.removeAt(exIdx) })
                                                it[dayIndex] = updatedDay
                                            }
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        else Text("Criar Treino", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }

    // Exercise selection dialog with sets/reps/rest configuration
    if (showExerciseDialog) {
        AddExerciseToDayDialogCreate(
            allExercises = exercises,
            onAdd = { entry ->
                workoutDays = workoutDays.toMutableList().also {
                    val day = it[targetDayIndex]
                    it[targetDayIndex] = day.copy(
                        exercises = day.exercises + DayExerciseData(
                            exerciseId = entry.exerciseId,
                            exerciseName = entry.exerciseName,
                            sets = entry.defaultSets,
                            reps = entry.defaultReps,
                            restSeconds = entry.defaultRestSeconds
                        )
                    )
                }
                showExerciseDialog = false
                exerciseSearch = ""
            },
            onDismiss = { showExerciseDialog = false; exerciseSearch = "" }
        )
    }
}

// Exercise selection dialog with sets/reps/rest configuration for CreateWorkoutScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseToDayDialogCreate(
    allExercises: List<Exercise>,
    onAdd: (EditableDayExerciseCreate) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("12") }
    var restSeconds by remember { mutableStateOf("60") }

    val filtered = allExercises.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            (it.muscleGroup ?: "").contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Exercício") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar exercício...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { ex ->
                        val isSelected = selectedExercise?.id == ex.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExercise = ex },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ex.name ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(ex.muscleGroup ?: "Geral", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                if (selectedExercise != null) {
                    HorizontalDivider()
                    Text(
                        "Configurações para: ${selectedExercise!!.name}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it.filter { c -> c.isDigit() } },
                            label = { Text("Séries") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it.filter { c -> c.isDigit() } },
                            label = { Text("Reps") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        OutlinedTextField(
                            value = restSeconds,
                            onValueChange = { restSeconds = it.filter { c -> c.isDigit() } },
                            label = { Text("Descanso(s)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ex = selectedExercise ?: return@Button
                    onAdd(
                        EditableDayExerciseCreate(
                            exerciseId = ex.id,
                            exerciseName = ex.name ?: "",
                            defaultSets = sets.toIntOrNull() ?: 3,
                            defaultReps = reps.toIntOrNull() ?: 12,
                            defaultRestSeconds = restSeconds.toIntOrNull() ?: 60
                        )
                    )
                },
                enabled = selectedExercise != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Adicionar", color = MaterialTheme.colorScheme.onPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private data class EditableDayExerciseCreate(
    val exerciseId: String,
    val exerciseName: String,
    val defaultSets: Int,
    val defaultReps: Int,
    val defaultRestSeconds: Int
)

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
