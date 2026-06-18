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
fun ManageWorkoutsScreen(viewModel: ProfessorViewModel, navController: androidx.navigation.NavHostController? = null) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ManageWorkoutsContent(viewModel = viewModel, navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWorkoutsContent(viewModel: ProfessorViewModel, navController: androidx.navigation.NavHostController? = null) {
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Gerenciar Treinos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text("Edite e atribua treinos aos alunos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

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
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text(template.type ?: "Geral", style = MaterialTheme.typography.labelSmall)
                            }
                            Badge(
                                containerColor = when (template.difficulty) {
                                    "Iniciante" -> Green100
                                    "Intermediário" -> Orange100
                                    else -> Red100
                                },
                                contentColor = when (template.difficulty) {
                                    "Iniciante" -> IfgGreen
                                    "Intermediário" -> Orange600
                                    else -> Red500
                                }
                            ) {
                                Text(template.difficulty ?: "Nível", style = MaterialTheme.typography.labelSmall)
                            }
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
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
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Atribuir", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                            }
                            
                            OutlinedButton(
                                onClick = { navController?.navigate("edit_template/${template.id}") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
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
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
                ) { Text("Atribuir", color = MaterialTheme.colorScheme.onPrimary) }
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir", color = MaterialTheme.colorScheme.onError) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false; templateToDelete = null }) { Text("Cancelar") } }
        )
    }

}
