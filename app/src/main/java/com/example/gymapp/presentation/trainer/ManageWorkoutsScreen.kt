package com.example.gymapp.presentation.trainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapp.domain.model.*
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWorkoutsScreen(viewModel: ProfessorViewModel) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ManageWorkoutsContent(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWorkoutsContent(viewModel: ProfessorViewModel) {
    val templates by viewModel.templates.collectAsState()
    val students by viewModel.students.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var selectedAlunoId by remember { mutableStateOf("") }
    var startsAt by remember { mutableStateOf("") }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var templateToEdit by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var expandedTemplateId by remember { mutableStateOf<String?>(null) }

    val filteredTemplates = templates.filter {
        it.name.contains(searchQuery, ignoreCase = true) || (it.type ?: "").contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        viewModel.loadTemplates()
        viewModel.loadStudents()
        viewModel.loadExercises()
    }

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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gerenciar Treinos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text("Edite e atribua treinos aos alunos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar treino...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IfgGreen)
            }
        } else if (filteredTemplates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum treino encontrado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredTemplates) { template ->
                    val isExpanded = expandedTemplateId == template.id

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header row with expand toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = IfgGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    template.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    expandedTemplateId = if (isExpanded) null else template.id
                                }) {
                                    Icon(
                                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badges
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Badge(containerColor = Green100) { Text(template.type ?: "Geral", color = IfgGreen, style = MaterialTheme.typography.labelSmall) }
                                Badge(containerColor = when (template.difficulty) {
                                    "Iniciante" -> Green100
                                    "Intermediário" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFFFEBEE)
                                }) {
                                    Text(template.difficulty ?: "Nível", color = when (template.difficulty) {
                                        "Iniciante" -> IfgGreen
                                        "Intermediário" -> Color(0xFFE65100)
                                        else -> Color(0xFFC62828)
                                    }, style = MaterialTheme.typography.labelSmall)
                                }
                                Badge(containerColor = Color(0xFFE3F2FD)) {
                                    Text("${template.exercises?.size ?: 0} exercícios", color = Color(0xFF1565C0), style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${template.totalSessions} sessões", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            // Expandable exercises list
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    if (template.exercises.isNullOrEmpty()) {
                                        Text("Nenhum exercício neste treino", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        template.exercises.sortedBy { it.orderIndex }.forEachIndexed { idx, ex ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Number badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Green100),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("${idx + 1}", style = MaterialTheme.typography.labelSmall.copy(color = IfgGreen, fontWeight = FontWeight.Bold))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        ex.exerciseName ?: "Exercício",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                                    )
                                                    Text(
                                                        "${ex.defaultSets}x${ex.defaultReps} reps · ${ex.defaultRestSeconds}s descanso",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { selectedTemplate = template; showAssignDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IfgGreen)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Atribuir")
                                }
                                OutlinedButton(
                                    onClick = { templateToEdit = template; showEditDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar")
                                }
                                OutlinedButton(
                                    onClick = { templateToDelete = template; showDeleteDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Assign dialog
    if (showAssignDialog && selectedTemplate != null) {
    AlertDialog(
        // Reset all state, including the selected template, when the dialog is dismissed
        onDismissRequest = {
            showAssignDialog = false
            selectedTemplate = null
            selectedAlunoId = ""
            startsAt = ""
        },
        title = { Text("Atribuir Treino") },
        text = {
            // Date picker state is scoped to the dialog so it does not leak when the dialog closes
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState()
            Column {
                    Text("Atribuir \"${selectedTemplate!!.name}\" a um aluno", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    var alunoExpanded by remember { mutableStateOf(false) }
                    val alunoOptions = students
                    ExposedDropdownMenuBox(expanded = alunoExpanded, onExpandedChange = { alunoExpanded = !alunoExpanded }) {
                        OutlinedTextField(
                            value = alunoOptions.find { it.id == selectedAlunoId }?.fullName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecionar Aluno") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alunoExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                        )
                        ExposedDropdownMenu(expanded = alunoExpanded, onDismissRequest = { alunoExpanded = false }) {
                            alunoOptions.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text(student.fullName ?: "") },
                                    onClick = { selectedAlunoId = student.id; alunoExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = startsAt,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Data início") },
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar Data")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val selectedDate = datePickerState.selectedDateMillis?.let {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        sdf.format(java.util.Date(it))
                                    }
                                    if (selectedDate != null) {
                                        startsAt = selectedDate
                                    }
                                    showDatePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedAlunoId.isNotBlank() && startsAt.isNotBlank()) {
                            viewModel.assignWorkout(
                                AssignWorkoutRequest(
                                    selectedAlunoId,
                                    selectedTemplate!!.id,
                                    startsAt
                                )
                            )
                            // Reset dialog state after successful assignment
                            showAssignDialog = false
                            selectedTemplate = null
                            selectedAlunoId = ""
                            startsAt = ""
                        }
                    },
                    enabled = selectedAlunoId.isNotBlank() && startsAt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
                ) { Text("Atribuir") }
            },
            dismissButton = { TextButton(onClick = { 
                showAssignDialog = false
                selectedTemplate = null
                selectedAlunoId = ""
                startsAt = ""
            }) { Text("Cancelar") } }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && templateToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; templateToDelete = null },
            title = { Text("Excluir Treino") },
            text = { Text("Tem certeza que deseja excluir o treino \"${templateToDelete!!.name}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTemplate(templateToDelete!!.id)
                        showDeleteDialog = false
                        templateToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false; templateToDelete = null }) { Text("Cancelar") } }
        )
    }

    // Edit template dialog
    if (showEditDialog && templateToEdit != null) {
        EditTemplateDialog(
            template = templateToEdit!!,
            allExercises = exercises,
            onDismiss = { showEditDialog = false; templateToEdit = null },
            onSave = { id, request ->
                viewModel.updateTemplate(id, request)
                showEditDialog = false
                templateToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTemplateDialog(
    template: WorkoutTemplate,
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSave: (id: String, request: CreateTemplateRequest) -> Unit
) {
    var name by remember { mutableStateOf(template.name) }
    var type by remember { mutableStateOf(template.type) }
    var difficulty by remember { mutableStateOf(template.difficulty) }
    var totalSessions by remember { mutableStateOf(template.totalSessions.toString()) }

    // Mutable list of exercise entries for editing
    var exerciseEntries by remember {
        mutableStateOf<List<ExerciseEntry>>(
            template.exercises?.sortedBy { it.orderIndex ?: 0 }?.map { ex ->
                ExerciseEntry(
                    exerciseId = ex.exerciseId,
                    exerciseName = ex.exerciseName ?: "",
                    defaultSets = ex.defaultSets ?: 3,
                    defaultReps = ex.defaultReps ?: 12,
                    defaultRestSeconds = ex.defaultRestSeconds ?: 60
                )
            } ?: emptyList()
        )
    }
    var showAddExercise by remember { mutableStateOf(false) }

    val typeOptions = listOf("Força", "Hipertrofia", "Resistência", "Funcional", "Mobilidade")
    val diffOptions = listOf("Iniciante", "Intermediário", "Avançado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Treino") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))

                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type ?: "",
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
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = { type = t; typeExpanded = false }
                            )
                        }
                    }
                }

                var diffExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = diffExpanded,
                    onExpandedChange = { diffExpanded = !diffExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = difficulty ?: "",
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
                    ExposedDropdownMenu(
                        expanded = diffExpanded,
                        onDismissRequest = { diffExpanded = false }
                    ) {
                        diffOptions.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = { difficulty = d; diffExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(value = totalSessions, onValueChange = { totalSessions = it }, label = { Text("Total de Sessões") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary))

                // Exercise list
                Text("Exercícios (${exerciseEntries.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))

                exerciseEntries.forEachIndexed { idx, entry ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.exerciseName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Text("${entry.defaultSets}x${entry.defaultReps} · ${entry.defaultRestSeconds}s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                exerciseEntries = exerciseEntries.toMutableList().also { it.removeAt(idx) }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showAddExercise = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Exercício")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sessions = totalSessions.toIntOrNull() ?: 0
                    val request = CreateTemplateRequest(
                        name = name,
                        type = type ?: "Força",
                        difficulty = difficulty ?: "Iniciante",
                        totalSessions = sessions,
                        exercises = exerciseEntries.mapIndexed { idx, entry ->
                            TemplateExerciseInput(
                                exerciseId = entry.exerciseId,
                                orderIndex = idx,
                                defaultSets = entry.defaultSets,
                                defaultReps = entry.defaultReps,
                                defaultRestSeconds = entry.defaultRestSeconds
                            )
                        }
                    )
                    onSave(template.id, request)
                },
                enabled = name.isNotBlank() && exerciseEntries.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) { Text("Salvar", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    // Add exercise sub-dialog
    if (showAddExercise) {
        AddExerciseToTemplateDialog(
            allExercises = allExercises,
            existingIds = exerciseEntries.map { it.exerciseId }.toSet(),
            onAdd = { entry ->
                exerciseEntries = exerciseEntries + entry
                showAddExercise = false
            },
            onDismiss = { showAddExercise = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseToTemplateDialog(
    allExercises: List<Exercise>,
    existingIds: Set<String>,
    onAdd: (ExerciseEntry) -> Unit,
    onDismiss: () -> Unit
) {
    // State for the search query used to filter the exercise list.
    var searchQuery by remember { mutableStateOf("") }

    // Filtered list based on the search term (keeps already‑selected exercises so we can show them disabled).
    val filtered = allExercises.filter { searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Exercício") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search field at the top
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar exercício...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Scrollable list of exercises
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { ex ->
                        val alreadyAdded = ex.id in existingIds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (!alreadyAdded) Modifier.clickable {
                                        // Immediately add with default values and dismiss
                                        onAdd(
                                            ExerciseEntry(
                                                exerciseId = ex.id,
                                                exerciseName = ex.name,
                                                defaultSets = 3,
                                                defaultReps = 10,
                                                defaultRestSeconds = 60
                                            )
                                        )
                                        onDismiss()
                                    } else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (alreadyAdded) Color.LightGray else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (alreadyAdded) 0.dp else 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = ex.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = ex.muscleGroup ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (alreadyAdded) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        // No explicit confirm button – selection happens on tap.
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private data class ExerciseEntry(
    val exerciseId: String,
    val exerciseName: String,
    val defaultSets: Int,
    val defaultReps: Int,
    val defaultRestSeconds: Int
)
