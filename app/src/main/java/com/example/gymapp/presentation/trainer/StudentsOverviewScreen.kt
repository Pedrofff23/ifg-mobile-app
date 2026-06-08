package com.example.gymapp.presentation.trainer

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
    var showAssignDialog by remember { mutableStateOf(false) }
    var showGroupAssignDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<User?>(null) }
    var selectedTemplateId by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf("") }
    var startsAt by remember { mutableStateOf("") }
    // State for navigating to the student detail screen
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
 floatingActionButton = {
 FloatingActionButton(
 onClick = { showGroupAssignDialog = true },
 containerColor = IfgGreen
 ) {
 Icon(Icons.Default.Group, contentDescription = "Atribuir Treino a Grupo")
 }
 },
 snackbarHost = { SnackbarHost(snackbarHostState) }
 ) { padding ->
 Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        // Title
        Text("Meus Alunos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text("Gerencie e acompanhe seus alunos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.searchStudents(it) },
            label = { Text("Buscar aluno...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCardSmall("Total", students.size.toString(), IfgGreen, Green100, Icons.Default.People, modifier = Modifier.weight(1f))
            StatCardSmall("Ativos", students.count { it.isActive }.toString(), Color(0xFF1565C0), Color(0xFFE3F2FD), Icons.Default.CheckCircle, modifier = Modifier.weight(1f))
            StatCardSmall("Novos", "0", Color(0xFF6A1B9A), Color(0xFFF3E5F5), Icons.Default.PersonAdd, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IfgGreen)
            }
        } else if (students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum aluno encontrado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(students) { student ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable {
                                selectedDetailStudentId = student.id
                                showStudentDetail = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar circle with initials
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(IfgGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = (student.fullName ?: "").split(" ").filter { it.isNotBlank() }.map { it.firstOrNull() ?: 'A' }.take(2).joinToString("")
                                Text(initials, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.fullName ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(student.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (student.isActive) {
                                        Badge(containerColor = Green100) { Text("Ativo", color = IfgGreen, style = MaterialTheme.typography.labelSmall) }
                                    }
                                    Badge(containerColor = Color(0xFFE3F2FD)) { Text(student.role, color = Color(0xFF1565C0), style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                            IconButton(onClick = { selectedStudent = student; showAssignDialog = true }) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = "Atribuir Treino", tint = IfgGreen)
                            }
                        }
                    }
                }
            }
        }
    }

    // Assign dialog (individual)
    if (showAssignDialog && (selectedStudent != null)) {
    AlertDialog(
    // Reset all dialog‑related state, including the selected student, when dismissed
    onDismissRequest = {
        showAssignDialog = false
        selectedStudent = null
        selectedTemplateId = ""
        startsAt = ""
    },
    title = { Text("Atribuir Treino") },
    text = {
        // Date picker state is scoped to this dialog to avoid leaking when the dialog closes
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
    modifier = Modifier.menuAnchor(),
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
    if (selectedTemplateId.isNotBlank() && startsAt.isNotBlank()) {
        viewModel.assignWorkout(
            AssignWorkoutRequest(
                selectedStudent!!.id,
                selectedTemplateId,
                startsAt
            )
        )
        // Reset dialog state after successful assignment
        showAssignDialog = false
        selectedStudent = null
        selectedTemplateId = ""
        startsAt = ""
    }
    },
    enabled = selectedTemplateId.isNotBlank() && startsAt.isNotBlank(),
    colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
    ) { Text("Atribuir") }
    },
    dismissButton = { TextButton(onClick = {
        showAssignDialog = false
        selectedStudent = null
        selectedTemplateId = ""
        startsAt = ""
    }) { Text("Cancelar") } }
    )
    }

    // Assign dialog (group)
    if (showGroupAssignDialog) {
    AlertDialog(
    onDismissRequest = {
    showGroupAssignDialog = false
    selectedGroupId = ""
    selectedTemplateId = ""
    startsAt = ""
    },
    title = { Text("Atribuir Treino a Grupo") },
    text = {
        // Date picker state is scoped to this dialog to avoid leaking when the dialog closes
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
    modifier = Modifier.menuAnchor(),
    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
    )
    ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
    groups.forEach { group ->
    DropdownMenuItem(
    text = { Text(group.name) },
    onClick = { selectedGroupId = group.id; groupExpanded = false }
    )
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
    modifier = Modifier.menuAnchor(),
    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary),
    )
    ExposedDropdownMenu(expanded = groupTemplateExpanded, onDismissRequest = { groupTemplateExpanded = false }) {
    templates.forEach { template ->
    DropdownMenuItem(
    text = { Text("${template.name} (${template.type})") },
    onClick = { selectedTemplateId = template.id; groupTemplateExpanded = false }
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
    if (selectedGroupId.isNotBlank() && selectedTemplateId.isNotBlank() && startsAt.isNotBlank()) {
    viewModel.assignWorkoutToGroup(AssignGroupWorkoutRequest(selectedGroupId, selectedTemplateId, startsAt))
    showGroupAssignDialog = false
    selectedGroupId = ""
    selectedTemplateId = ""
    startsAt = ""
    }
    },
    enabled = selectedGroupId.isNotBlank() && selectedTemplateId.isNotBlank() && startsAt.isNotBlank(),
    colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
    ) { Text("Atribuir") }
    },
    dismissButton = {
    TextButton(onClick = {
    showGroupAssignDialog = false
    selectedGroupId = ""
    selectedTemplateId = ""
    startsAt = ""
    }) { Text("Cancelar") }
    }
    )
    }
    }
    }

    // Student detail overlay
    if (showStudentDetail && selectedDetailStudentId.isNotBlank()) {
        StudentDetailScreen(
            studentId = selectedDetailStudentId,
            viewModel = viewModel,
            onBack = { showStudentDetail = false }
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
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = iconTint)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
