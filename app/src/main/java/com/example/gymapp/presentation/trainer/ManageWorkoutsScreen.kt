package com.example.gymapp.presentation.trainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Gerenciar Treinos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
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
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IfgGreen)
                }
            }
        } else if (filteredTemplates.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum treino encontrado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredTemplates) { template ->
                val isExpanded = expandedTemplateId == template.id
                val workoutDays = template.workoutDays ?: emptyList()
                val totalExercises = workoutDays.sumOf { it.exercises?.size ?: 0 }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = IfgGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                template.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
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

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Badge(containerColor = Green100, contentColor = IfgGreen) {
                                Text(template.type ?: "Geral", style = MaterialTheme.typography.labelSmall)
                            }
                            Badge(
                                containerColor = when (template.difficulty) {
                                    "Iniciante" -> Green100
                                    "Intermediário" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFFFEBEE)
                                },
                                contentColor = when (template.difficulty) {
                                    "Iniciante" -> IfgGreen
                                    "Intermediário" -> Color(0xFFE65100)
                                    else -> Color(0xFFC62828)
                                }
                            ) {
                                Text(template.difficulty ?: "Nível", style = MaterialTheme.typography.labelSmall)
                            }
                            Badge(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1565C0)) {
                                Text("${workoutDays.size} dia(s) • $totalExercises ex.", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                if (workoutDays.isEmpty()) {
                                    Text("Nenhum dia de treino neste template", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    workoutDays.sortedBy { it.orderIndex }.forEach { day ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    day.name,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = IfgGreen
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (day.exercises.isNullOrEmpty()) {
                                                    Text("Sem exercícios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                } else {
                                                    day.exercises.sortedBy { it.orderIndex }.forEachIndexed { idx, ex ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                "${idx + 1}. ${ex.exerciseName ?: "Exercício"}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Text(
                                                                "${ex.defaultSets}x${ex.defaultReps}",
                                                                style = MaterialTheme.typography.labelSmall,
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { selectedTemplate = template; showAssignDialog = true },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Atribuir", style = MaterialTheme.typography.labelLarge)
                            }
                            
                            OutlinedButton(
                                onClick = { templateToEdit = template; showEditDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar", style = MaterialTheme.typography.labelLarge)
                            }

                            OutlinedIconButton(
                                onClick = { templateToDelete = template; showDeleteDialog = true },
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
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
            onDismissRequest = {
                showAssignDialog = false
                selectedTemplate = null
                selectedAlunoId = ""
                startsAt = ""
            },
            title = { Text("Atribuir Treino") },
            text = {
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState()
                Column {
                    Text("Atribuir \"${selectedTemplate!!.name}\" a um aluno", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    var alunoExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = alunoExpanded, onExpandedChange = { alunoExpanded = !alunoExpanded }) {
                        OutlinedTextField(
                            value = students.find { it.id == selectedAlunoId }?.fullName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecionar Aluno") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alunoExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                        )
                        ExposedDropdownMenu(expanded = alunoExpanded, onDismissRequest = { alunoExpanded = false }) {
                            students.forEach { student ->
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
                            viewModel.assignWorkout(AssignWorkoutRequest(selectedAlunoId, selectedTemplate!!.id, startsAt))
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
            dismissButton = {
                TextButton(onClick = { 
                    showAssignDialog = false
                    selectedTemplate = null
                    selectedAlunoId = ""
                    startsAt = ""
                }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteDialog && templateToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; templateToDelete = null },
            title = { Text("Excluir Treino") },
            text = { Text("Tem certeza que deseja excluir o treino \"${templateToDelete!!.name}\"?") },
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

    // Build editable workout days from existing template
    var workoutDays by remember {
        mutableStateOf<List<EditableWorkoutDay>>(
            (template.workoutDays ?: emptyList()).sortedBy { it.orderIndex }.map { day ->
                EditableWorkoutDay(
                    name = day.name,
                    orderIndex = day.orderIndex ?: 0,
                    exercises = (day.exercises ?: emptyList()).sortedBy { it.orderIndex }.map { ex ->
                        EditableDayExercise(
                            exerciseId = ex.exerciseId,
                            exerciseName = ex.exerciseName ?: "",
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

    val typeOptions = listOf("Força", "Hipertrofia", "Resistência", "Funcional", "Mobilidade")
    val diffOptions = listOf("Iniciante", "Intermediário", "Avançado")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Treino") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(
                        value = type ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        typeOptions.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                        }
                    }
                }

                var diffExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = !diffExpanded }) {
                    OutlinedTextField(
                        value = difficulty ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dificuldade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = diffExpanded, onDismissRequest = { diffExpanded = false }) {
                        diffOptions.forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { difficulty = d; diffExpanded = false })
                        }
                    }
                }

                HorizontalDivider()
                Text("Dias de Treino", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))

                workoutDays.forEachIndexed { dayIndex, day ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = day.name,
                                    onValueChange = { newName ->
                                        workoutDays = workoutDays.toMutableList().also { it[dayIndex] = day.copy(name = newName) }
                                    },
                                    label = { Text("Nome do Dia") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                                )
                                IconButton(onClick = {
                                    workoutDays = workoutDays.toMutableList().also {
                                        it.removeAt(dayIndex)
                                        it.forEachIndexed { i, d -> it[i] = d.copy(orderIndex = i) }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover dia", tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                }
                            }
                            day.exercises.forEachIndexed { exIdx, ex ->
                                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${exIdx + 1}. ${ex.exerciseName}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("${ex.defaultSets}x${ex.defaultReps}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    IconButton(onClick = {
                                        workoutDays = workoutDays.toMutableList().also {
                                            val updatedDay = day.copy(exercises = day.exercises.toMutableList().also { it.removeAt(exIdx) })
                                            it[dayIndex] = updatedDay
                                        }
                                    }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            TextButton(onClick = { targetDayIndex = dayIndex; showAddExercise = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Adicionar Exercício", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        val nextLabel = ('A' + (workoutDays.size % 26)).let { letter ->
                            if (workoutDays.size >= 26) "Treino $letter${workoutDays.size / 26 + 1}" else "Treino $letter"
                        }
                        workoutDays = workoutDays + EditableWorkoutDay(name = nextLabel, orderIndex = workoutDays.size)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Adicionar Dia")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val request = CreateTemplateRequest(
                        name = name,
                        type = type ?: "Força",
                        difficulty = difficulty ?: "Iniciante",
                        workoutDays = workoutDays.map { day ->
                            WorkoutDayInput(
                                name = day.name,
                                orderIndex = day.orderIndex,
                                exercises = day.exercises.mapIndexed { idx, ex ->
                                    TemplateExerciseInput(ex.exerciseId, idx, ex.defaultSets, ex.defaultReps, ex.defaultRestSeconds)
                                }
                            )
                        }
                    )
                    onSave(template.id, request)
                },
                enabled = name.isNotBlank() && workoutDays.isNotEmpty() && workoutDays.all { it.exercises.isNotEmpty() },
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showAddExercise) {
        AddExerciseToDayDialog(
            allExercises = allExercises,
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
    val filtered = allExercises.filter { it.name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Exercício") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Buscar...") }, modifier = Modifier.fillMaxWidth())
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filtered) { ex ->
                        val alreadyAdded = ex.id in existingIds
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = !alreadyAdded) {
                                onAdd(EditableDayExercise(ex.id, ex.name ?: "", 3, 10, 60))
                                onDismiss()
                            },
                            colors = CardDefaults.cardColors(containerColor = if (alreadyAdded) Color.LightGray else MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(ex.name ?: "", modifier = Modifier.weight(1f))
                                if (alreadyAdded) Icon(Icons.Default.Check, contentDescription = null, tint = IfgGreen)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
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
    val defaultSets: Int,
    val defaultReps: Int,
    val defaultRestSeconds: Int
)
