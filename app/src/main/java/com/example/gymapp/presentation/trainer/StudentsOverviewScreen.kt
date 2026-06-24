package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymapp.domain.model.*
import com.example.gymapp.ui.theme.*

enum class StudentFilter { ALL, ACTIVE, BLOCKED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsOverviewScreen(viewModel: ProfessorViewModel) {
    val students by viewModel.students.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(StudentFilter.ALL) }
    val filteredStudents = remember(students, filter) {
        students.filter { student ->
            when (filter) {
                StudentFilter.ALL -> true
                StudentFilter.ACTIVE -> student.isActive
                StudentFilter.BLOCKED -> student.isBlocked
            }
        }
    }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showGroupAssignDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<User?>(null) }
    var selectedTemplateId by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf("") }
    var startsAt by remember { mutableStateOf("") }
    
    var showStudentDetail by remember { mutableStateOf(false) }
    var selectedDetailStudentId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadStudents()
        viewModel.loadTemplates()
        viewModel.loadGroups()
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        Text("Meus Alunos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        Text("Gerencie e acompanhe seus alunos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; viewModel.searchStudents(it) },
                            label = { Text("Buscar aluno...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCardSmall(
                                label = "Total",
                                value = students.size.toString(),
                                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                bgColor = MaterialTheme.colorScheme.primaryContainer,
                                icon = Icons.Default.People,
                                isSelected = filter == StudentFilter.ALL,
                                onClick = { filter = StudentFilter.ALL },
                                modifier = Modifier.weight(1f)
                            )
                            StatCardSmall(
                                label = "Ativos",
                                value = students.count { it.isActive }.toString(),
                                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                bgColor = MaterialTheme.colorScheme.secondaryContainer,
                                icon = Icons.Default.CheckCircle,
                                isSelected = filter == StudentFilter.ACTIVE,
                                onClick = { filter = StudentFilter.ACTIVE },
                                modifier = Modifier.weight(1f)
                            )
                            StatCardSmall(
                                label = "Bloqueados",
                                value = students.count { it.isBlocked }.toString(),
                                iconTint = MaterialTheme.colorScheme.onErrorContainer,
                                bgColor = MaterialTheme.colorScheme.errorContainer,
                                icon = Icons.Default.Block,
                                isSelected = filter == StudentFilter.BLOCKED,
                                onClick = { filter = StudentFilter.BLOCKED },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (filteredStudents.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Nenhum aluno encontrado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(filteredStudents) { student ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedDetailStudentId = student.id
                                showStudentDetail = true
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initials = (student.fullName ?: "").split(" ").filter { it.isNotBlank() }.map { it.firstOrNull() ?: 'A' }.take(2).joinToString("")
                                        Text(initials, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.fullName ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(student.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    
                                    val activeAssignments by viewModel.studentActiveAssignments.collectAsState()
                                    val currentWorkout = activeAssignments[student.id]
                                    if (currentWorkout != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Treino: ${currentWorkout.templateName ?: "Carregando"}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (student.isActive) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            ) {
                                                Text(
                                                    "Ativo",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (student.isBlocked) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.errorContainer
                                            ) {
                                                Text(
                                                    "Bloqueado",
                                                    color = MaterialTheme.colorScheme.error,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                student.role,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = { selectedStudent = student; showAssignDialog = true }) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = "Atribuir Treino", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (showStudentDetail && selectedDetailStudentId.isNotBlank()) {
            StudentDetailScreen(
                studentId = selectedDetailStudentId,
                viewModel = viewModel,
                onBack = { showStudentDetail = false }
            )
        }
    }

    // Dialogs
    if (showAssignDialog && selectedStudent != null) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false; selectedStudent = null; selectedTemplateId = ""; startsAt = "" },
            title = { Text("Atribuir Treino") },
            text = {
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState()
                Column {
                    Text("Atribuir treino a ${selectedStudent!!.fullName ?: ""}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    var templateExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = templateExpanded, onExpandedChange = { templateExpanded = !templateExpanded }) {
                        OutlinedTextField(
                            value = templates.find { it.id == selectedTemplateId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecionar Treino") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                        )
                        ExposedDropdownMenu(expanded = templateExpanded, onDismissRequest = { templateExpanded = false }) {
                            templates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text("${template.name} (${template.type})") },
                                    onClick = { selectedTemplateId = template.id; templateExpanded = false }
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
                                    if (selectedDate != null) startsAt = selectedDate
                                    showDatePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
                        ) { DatePicker(state = datePickerState) }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedTemplateId.isNotBlank() && startsAt.isNotBlank()) {
                            viewModel.assignWorkout(AssignWorkoutRequest(selectedStudent!!.id, selectedTemplateId, startsAt))
                            showAssignDialog = false; selectedStudent = null; selectedTemplateId = ""; startsAt = ""
                        }
                    },
                    enabled = selectedTemplateId.isNotBlank() && startsAt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Atribuir", color = MaterialTheme.colorScheme.onPrimary) }
            },
            dismissButton = { TextButton(onClick = { showAssignDialog = false; selectedStudent = null; selectedTemplateId = ""; startsAt = "" }) { Text("Cancelar") } }
        )
    }

    if (showGroupAssignDialog) {
        AlertDialog(
            onDismissRequest = { showGroupAssignDialog = false; selectedGroupId = ""; selectedTemplateId = ""; startsAt = "" },
            title = { Text("Atribuir Treino a Grupo") },
            text = {
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState()
                Column {
                    var groupExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = !groupExpanded }) {
                        OutlinedTextField(
                            value = groups.find { it.id == selectedGroupId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecionar Grupo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                        )
                        ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                            groups.forEach { group ->
                                DropdownMenuItem(text = { Text(group.name) }, onClick = { selectedGroupId = group.id; groupExpanded = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    var groupTemplateExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = groupTemplateExpanded, onExpandedChange = { groupTemplateExpanded = !groupTemplateExpanded }) {
                        OutlinedTextField(
                            value = templates.find { it.id == selectedTemplateId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecionar Treino") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupTemplateExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
                        )
                        ExposedDropdownMenu(expanded = groupTemplateExpanded, onDismissRequest = { groupTemplateExpanded = false }) {
                            templates.forEach { template ->
                                DropdownMenuItem(text = { Text("${template.name} (${template.type})") }, onClick = { selectedTemplateId = template.id; groupTemplateExpanded = false })
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
                            IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar Data") }
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
                                    if (selectedDate != null) startsAt = selectedDate
                                    showDatePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
                        ) { DatePicker(state = datePickerState) }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedGroupId.isNotBlank() && selectedTemplateId.isNotBlank() && startsAt.isNotBlank()) {
                            viewModel.assignWorkoutToGroup(AssignGroupWorkoutRequest(selectedGroupId, selectedTemplateId, startsAt))
                            showGroupAssignDialog = false; selectedGroupId = ""; selectedTemplateId = ""; startsAt = ""
                        }
                    },
                    enabled = selectedGroupId.isNotBlank() && selectedTemplateId.isNotBlank() && startsAt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Atribuir", color = MaterialTheme.colorScheme.onPrimary) }
            },
            dismissButton = { TextButton(onClick = { showGroupAssignDialog = false; selectedGroupId = ""; selectedTemplateId = ""; startsAt = "" }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun StatCardSmall(
    label: String,
    value: String,
    iconTint: Color,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (isSelected) BorderStroke(2.dp, iconTint) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = iconTint)
            Text(label, style = MaterialTheme.typography.labelSmall, color = iconTint.copy(alpha = 0.8f))
        }
    }
}
