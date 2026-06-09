package com.example.gymapp.presentation.trainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
fun ManageGroupsScreen(viewModel: ProfessorViewModel) {
    val groups by viewModel.groups.collectAsState()
    val students by viewModel.students.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAssignWorkoutDialog by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<StudentGroup?>(null) }
    var expandedGroupId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
        viewModel.loadStudents()
        viewModel.loadTemplates()
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = IfgGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar Grupo")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Grupos",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Gerencie grupos de alunos e treinos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IfgGreen)
                }
            } else if (groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum grupo encontrado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Toque no + para criar um novo grupo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(groups, key = { it.id }) { group ->
                        GroupCard(
                            group = group,
                            isExpanded = expandedGroupId == group.id,
                            onToggleExpand = {
                                expandedGroupId = if (expandedGroupId == group.id) null else group.id
                            },
                            onEdit = {
                                selectedGroup = group
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedGroup = group
                                showDeleteDialog = true
                            },
                            onAddMember = {
                                selectedGroup = group
                                showAddMemberDialog = true
                            },
                            onRemoveMember = { userId ->
                                viewModel.removeGroupMember(group.id, userId)
                            },
                            onAssignWorkout = {
                                selectedGroup = group
                                showAssignWorkoutDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // ---- Create Group Dialog ----
    if (showCreateDialog) {
        GroupNameDialog(
            title = "Criar Grupo",
            confirmLabel = "Criar",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                viewModel.createGroup(name, description.ifBlank { null })
                showCreateDialog = false
            }
        )
    }

    // ---- Edit Group Dialog ----
    if (showEditDialog && selectedGroup != null) {
        GroupNameDialog(
            title = "Editar Grupo",
            confirmLabel = "Salvar",
            initialName = selectedGroup!!.name,
            initialDescription = selectedGroup!!.description ?: "",
            onDismiss = { showEditDialog = false; selectedGroup = null },
            onConfirm = { name, description ->
                viewModel.updateGroup(selectedGroup!!.id, name, description.ifBlank { null })
                showEditDialog = false
                selectedGroup = null
            }
        )
    }

    // ---- Delete Confirmation Dialog ----
    if (showDeleteDialog && selectedGroup != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; selectedGroup = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            title = { Text("Excluir Grupo") },
            text = { Text("Tem certeza que deseja excluir o grupo \"${selectedGroup!!.name}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(selectedGroup!!.id)
                        showDeleteDialog = false
                        selectedGroup = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4183D))
                ) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false; selectedGroup = null }) { Text("Cancelar") } }
        )
    }

    // ---- Add Member Dialog ----
    if (showAddMemberDialog && selectedGroup != null) {
        AddMemberDialog(
            group = selectedGroup!!,
            students = students,
            onDismiss = { showAddMemberDialog = false; selectedGroup = null },
            onAddMember = { userId ->
                viewModel.addGroupMember(selectedGroup!!.id, userId)
            }
        )
    }

    // ---- Assign Workout Dialog ----
    if (showAssignWorkoutDialog && selectedGroup != null) {
        AssignGroupWorkoutDialog(
            group = selectedGroup!!,
            templates = templates,
            onDismiss = { showAssignWorkoutDialog = false; selectedGroup = null },
            onAssign = { groupId, templateId, startsAt ->
                viewModel.assignWorkoutToGroup(AssignGroupWorkoutRequest(groupId, templateId, startsAt))
                showAssignWorkoutDialog = false
                selectedGroup = null
            }
        )
    }
}

// ============================================================================
// Group Card (expandable)
// ============================================================================

@Composable
private fun GroupCard(
    group: StudentGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddMember: () -> Unit,
    onRemoveMember: (userId: String) -> Unit,
    onAssignWorkout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(IfgGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        group.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!group.description.isNullOrBlank()) {
                        Text(
                            group.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val memberCount = group.members?.size ?: 0
                        Badge(containerColor = Green100) {
                            Text("$memberCount membro(s)", color = IfgGreen, style = MaterialTheme.typography.labelSmall)
                        }
                        // Show assigned workout badge
                        group.assignedWorkout?.let { workout ->
                            Badge(containerColor = Color(0xFFE3F2FD)) {
                                Text(
                                    "Treino: ${workout.templateName ?: "Atribuído"}",
                                    color = Color(0xFF1565C0),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = IfgGreen)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFD4183D))
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded content: members list + action buttons
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onAddMember,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IfgGreen)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar Membro", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = onAssignWorkout,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atribuir Treino", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Members list
                    Text(
                        "Membros",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val members = group.members ?: emptyList()
                    if (members.isEmpty()) {
                        Text(
                            "Nenhum membro neste grupo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(IfgGreenDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = (member.fullName ?: "")
                                        .split(" ")
                                        .filter { it.isNotBlank() }
                                        .map { it.firstOrNull() ?: '?' }
                                        .take(2)
                                        .joinToString("")
                                    Text(
                                        initials.ifBlank { "?" },
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        member.fullName ?: "Desconhecido",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!member.joinedAt.isNullOrBlank()) {
                                        Text(
                                            "Entrou em ${member.joinedAt}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = { onRemoveMember(member.userId) }) {
                                    Icon(
                                        Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Remover",
                                        tint = Color(0xFFD4183D),
                                        modifier = Modifier.size(22.dp)
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

// ============================================================================
// Create / Edit Group Dialog
// ============================================================================

@Composable
private fun GroupNameDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nome do Grupo") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Descrição (opcional)") },
                    maxLines = 3, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), description.trim()) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ============================================================================
// Add Member Dialog
// ============================================================================

@Composable
private fun AddMemberDialog(
    group: StudentGroup,
    students: List<User>,
    onDismiss: () -> Unit,
    onAddMember: (userId: String) -> Unit
) {
    val existingMemberIds = (group.members ?: emptyList()).map { it.userId }.toSet()
    val availableStudents = students.filter { it.id !in existingMemberIds }
    var searchQuery by remember { mutableStateOf("") }
    val filteredStudents = if (searchQuery.isBlank()) availableStudents
    else availableStudents.filter {
        (it.fullName ?: "").lowercase().contains(searchQuery.lowercase()) ||
        it.email.lowercase().contains(searchQuery.lowercase())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Membro") },
        text = {
            Column {
                Text("Grupo: ${group.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    label = { Text("Buscar aluno...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (filteredStudents.isEmpty()) {
                    Text(
                        if (availableStudents.isEmpty()) "Todos os alunos já estão neste grupo" else "Nenhum aluno encontrado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filteredStudents, key = { it.id }) { student ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(IfgGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initials = (student.fullName ?: "").split(" ").filter { it.isNotBlank() }.map { it.firstOrNull() ?: 'A' }.take(2).joinToString("")
                                        Text(initials, color = Color.White, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.fullName ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(student.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(
                                        onClick = {
                                            onAddMember(student.id)
                                            onDismiss()
                                        },
                                        modifier = Modifier.size(36.dp),
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = IfgGreen, contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Adicionar", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

// ============================================================================
// Assign Workout to Group Dialog
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignGroupWorkoutDialog(
    group: StudentGroup,
    templates: List<WorkoutTemplate>,
    onDismiss: () -> Unit,
    onAssign: (groupId: String, templateId: String, startsAt: String) -> Unit
) {
    var selectedTemplateId by remember { mutableStateOf("") }
    var startsAt by remember { mutableStateOf("") }
    var templateExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atribuir Treino ao Grupo") },
        text = {
            Column {
                Text("Grupo: ${group.name} (${group.members?.size ?: 0} membros)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = templateExpanded, onExpandedChange = { templateExpanded = !templateExpanded }) {
                    OutlinedTextField(
                        value = templates.find { it.id == selectedTemplateId }?.name ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Selecionar Treino") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
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
                    value = startsAt, onValueChange = { }, readOnly = true,
                    label = { Text("Data início") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar Data") } },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
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
                onClick = { if (selectedTemplateId.isNotBlank() && startsAt.isNotBlank()) onAssign(group.id, selectedTemplateId, startsAt) },
                enabled = selectedTemplateId.isNotBlank() && startsAt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) { Text("Atribuir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ============================================================================
// Stat Card
// ============================================================================

@Composable
private fun StatCardGroup(
    label: String, value: String, iconTint: Color, bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = iconTint)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
