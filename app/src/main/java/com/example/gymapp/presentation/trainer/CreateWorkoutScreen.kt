package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var totalSessions by remember { mutableStateOf("8") }
    var selectedExercises by remember { mutableStateOf<List<SelectedExercise>>(emptyList()) }
    var showExerciseDialog by remember { mutableStateOf(false) }
    var exerciseSearch by remember { mutableStateOf("") }

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

    // Auto-navigate back after a successful creation
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
                    Text("Criar Novo Treino", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Monte o treino do zero", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Form Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Treino") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))

                        // Type dropdown
                        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                            OutlinedTextField(value = type, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))
                            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                typeOptions.forEach { option ->
                                    DropdownMenuItem(text = { Text(option) }, onClick = { type = option; typeExpanded = false })
                                }
                            }
                        }

                        // Difficulty dropdown
                        ExposedDropdownMenuBox(expanded = difficultyExpanded, onExpandedChange = { difficultyExpanded = !difficultyExpanded }) {
                            OutlinedTextField(value = difficulty, onValueChange = {}, readOnly = true, label = { Text("Dificuldade") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))
                            ExposedDropdownMenu(expanded = difficultyExpanded, onDismissRequest = { difficultyExpanded = false }) {
                                difficultyOptions.forEach { option ->
                                    DropdownMenuItem(text = { Text(option) }, onClick = { difficulty = option; difficultyExpanded = false })
                                }
                            }
                        }

                        OutlinedTextField(
                            value = totalSessions,
                            onValueChange = { totalSessions = it },
                            label = { Text("Total de Sessões") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }

            // Exercises section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exercícios do Treino", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Button(onClick = { showExerciseDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar")
                    }
                }
            }

            // Selected exercise items
            items(selectedExercises) { selectedEx ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedEx.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                            IconButton(onClick = { selectedExercises = selectedExercises.filter { it.exerciseId != selectedEx.exerciseId } }) {
                                Icon(Icons.Default.Close, contentDescription = "Remover", tint = androidx.compose.ui.graphics.Color.Red)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = selectedEx.sets.toString(),
                                onValueChange = { newVal ->
                                    val n = newVal.toIntOrNull() ?: selectedEx.sets
                                    selectedExercises = selectedExercises.map { if (it.exerciseId == selectedEx.exerciseId) it.copy(sets = n) else it }
                                },
                                label = { Text("Sets") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                            )
                            OutlinedTextField(
                                value = selectedEx.reps.toString(),
                                onValueChange = { newVal ->
                                    val n = newVal.toIntOrNull() ?: selectedEx.reps
                                    selectedExercises = selectedExercises.map { if (it.exerciseId == selectedEx.exerciseId) it.copy(reps = n) else it }
                                },
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                            )
                            OutlinedTextField(
                                value = selectedEx.restSeconds.toString(),
                                onValueChange = { newVal ->
                                    val n = newVal.toIntOrNull() ?: selectedEx.restSeconds
                                    selectedExercises = selectedExercises.map { if (it.exerciseId == selectedEx.exerciseId) it.copy(restSeconds = n) else it }
                                },
                                label = { Text("Descanso(s)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                            )
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
                                totalSessions = totalSessions.toIntOrNull() ?: 8,
                                exercises = selectedExercises.mapIndexed { index, ex ->
                                    TemplateExerciseInput(
                                        exerciseId = ex.exerciseId,
                                        orderIndex = index,
                                        defaultSets = ex.sets,
                                        defaultReps = ex.reps,
                                        defaultRestSeconds = ex.restSeconds
                                    )
                                }
                            )
                            viewModel.createTemplate(request)
                            name = ""
                            selectedExercises = emptyList()
                        },
                        enabled = name.isNotBlank() && selectedExercises.isNotEmpty() && !isLoading,
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
                            val alreadySelected = selectedExercises.any { it.exerciseId == exercise.id }
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = if (alreadySelected) Green100 else MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exercise.name ?: "Sem nome", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(exercise.muscleGroup ?: "Geral", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (alreadySelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selecionado", tint = IfgGreen)
                                    } else {
                                        IconButton(onClick = {
                                            selectedExercises = selectedExercises + SelectedExercise(
                                                exerciseId = exercise.id,
                                                name = exercise.name ?: "",
                                                sets = 3,
                                                reps = 12,
                                                restSeconds = 60
                                            )
                                        }) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "Adicionar", tint = IfgGreen)
                                        }
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

data class SelectedExercise(
    val exerciseId: String,
    val name: String,
    val sets: Int = 3,
    val reps: Int = 12,
    val restSeconds: Int = 60
)
