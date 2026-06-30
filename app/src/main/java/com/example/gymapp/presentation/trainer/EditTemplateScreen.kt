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
import com.example.gymapp.domain.model.*
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateScreen(
    templateId: String,
    viewModel: ProfessorViewModel,
    onBack: () -> Unit = {}
) {
    val templates by viewModel.templates.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Track whether we've already handled the success message to prevent loop
    var hasHandledSuccess by remember { mutableStateOf(false) }

    val template = templates.find { it.id == templateId }

    var name by remember(template) { mutableStateOf(template?.name ?: "") }
    var type by remember(template) { mutableStateOf(template?.type ?: "Força") }
    var difficulty by remember(template) { mutableStateOf(template?.difficulty ?: "Iniciante") }

    // Build editable workout days from existing template
    var workoutDays by remember(template) {
        mutableStateOf<List<EditableWorkoutDay>>(
            (template?.workoutDays ?: emptyList()).sortedBy { it.orderIndex }.map { day ->
                EditableWorkoutDay(
                    name = day.name,
                    orderIndex = day.orderIndex ?: 0,
                    exercises = (day.exercises ?: emptyList()).sortedBy { it.orderIndex }.map { ex ->
                        EditableDayExercise(
                            exerciseId = ex.exerciseId,
                            exerciseName = ex.exerciseName ?: "",
                            orderIndex = ex.orderIndex ?: 0,
                            defaultSets = ex.defaultSets ?: 3,
                            defaultReps = ex.defaultReps ?: 12,
                            defaultRestSeconds = ex.defaultRestSeconds ?: 60
                        )
                    }
                )
            }.ifEmpty { listOf(EditableWorkoutDay(name = "Treino A", orderIndex = 0)) }
        )
    }

    var showAddExercise by remember { mutableStateOf(false) }
    var targetDayIndex by remember { mutableIntStateOf(0) }

    val typeOptions = listOf("Força", "Hipertrofia", "Resistência", "Funcional")
    var typeExpanded by remember { mutableStateOf(false) }
    val diffOptions = listOf("Iniciante", "Intermediário", "Avançado")
    var diffExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadExercises()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle success message - show snackbar and navigate back ONCE
    LaunchedEffect(successMessage) {
        val msg = successMessage
        if (msg != null && msg.contains("atualizado", ignoreCase = true) && !hasHandledSuccess) {
            hasHandledSuccess = true
            viewModel.clearSuccessMessage()
            onBack()
        }
    }

    // Handle error message
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = { Text("Editar Treino", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            item {
                Column {
                    Text(
                        "Editar Treino",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Configure nome, tipo, dias e exercícios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome do Treino") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                            OutlinedTextField(
                                value = type,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                typeOptions.forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = !diffExpanded }) {
                            OutlinedTextField(
                                value = difficulty,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Dificuldade") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenu(expanded = diffExpanded, onDismissRequest = { diffExpanded = false }) {
                                diffOptions.forEach { d ->
                                    DropdownMenuItem(text = { Text(d) }, onClick = { difficulty = d; diffExpanded = false })
                                }
                            }
                        }
                    }
                }
            }

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
                            workoutDays = workoutDays + EditableWorkoutDay(name = nextLabel, orderIndex = workoutDays.size)
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

            itemsIndexed(workoutDays) { dayIndex, day ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = day.name,
                                onValueChange = { newName ->
                                    workoutDays = workoutDays.toMutableList().also { it[dayIndex] = day.copy(name = newName) }
                                },
                                label = { Text("Nome do Dia") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                workoutDays = workoutDays.toMutableList().also {
                                    it.removeAt(dayIndex)
                                    it.forEachIndexed { i, d -> it[i] = d.copy(orderIndex = i) }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover dia", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (day.exercises.isEmpty()) {
                            Text(
                                "Nenhum exercício neste dia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            day.exercises.sortedBy { it.orderIndex }.forEachIndexed { exIdx, ex ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${exIdx + 1}. ${ex.exerciseName}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = {
                                                workoutDays = workoutDays.toMutableList().also {
                                                    val updatedDay = day.copy(exercises = day.exercises.toMutableList().also { it.removeAt(exIdx) })
                                                    it[dayIndex] = updatedDay
                                                }
                                            }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Close, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(
                                            "${ex.defaultSets}x${ex.defaultReps} • ${ex.defaultRestSeconds}s descanso",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = {
                                targetDayIndex = dayIndex
                                showAddExercise = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Adicionar Exercício")
                        }
                    }
                }
            }

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
                                        exercises = day.exercises.sortedBy { it.orderIndex }.mapIndexed { idx, ex ->
                                            TemplateExerciseInput(
                                                exerciseId = ex.exerciseId,
                                                orderIndex = idx,
                                                defaultSets = ex.defaultSets,
                                                defaultReps = ex.defaultReps,
                                                defaultRestSeconds = ex.defaultRestSeconds
                                            )
                                        }
                                    )
                                }
                            )
                            viewModel.updateTemplate(templateId, request)
                        },
                        enabled = name.isNotBlank() && workoutDays.isNotEmpty() && workoutDays.all { it.exercises.isNotEmpty() } && !isLoading,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        else Text("Salvar", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }

    if (showAddExercise) {
        AddExerciseToDayDialog(
            allExercises = exercises,
            existingIds = workoutDays[targetDayIndex].exercises.map { it.exerciseId }.toSet(),
            onAdd = { entry ->
                workoutDays = workoutDays.toMutableList().also {
                    val day = it[targetDayIndex]
                    it[targetDayIndex] = day.copy(exercises = day.exercises + entry)
                }
                showAddExercise = false
            },
            onDismiss = { showAddExercise = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseToDayDialog(
    allExercises: List<Exercise>,
    existingIds: Set<String>,
    onAdd: (EditableDayExercise) -> Unit,
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
                        val alreadyAdded = ex.id in existingIds
                        val isSelected = selectedExercise?.id == ex.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !alreadyAdded) {
                                    selectedExercise = ex
                                    sets = "3"
                                    reps = "12"
                                    restSeconds = "60"
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    alreadyAdded -> Color.LightGray
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ex.name ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(ex.muscleGroup ?: "Geral", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (alreadyAdded) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                else if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                        EditableDayExercise(
                            exerciseId = ex.id,
                            exerciseName = ex.name ?: "",
                            orderIndex = 0,
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

private data class EditableWorkoutDay(
    val name: String,
    val orderIndex: Int,
    val exercises: List<EditableDayExercise> = emptyList()
)

private data class EditableDayExercise(
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
    val defaultSets: Int,
    val defaultReps: Int,
    val defaultRestSeconds: Int
)

fun generateDayLabel(index: Int): String {
    val letter = ('A' + (index % 26)).toString()
    val suffix = if (index >= 26) "${index / 26 + 1}" else ""
    return "Treino $letter$suffix"
}
