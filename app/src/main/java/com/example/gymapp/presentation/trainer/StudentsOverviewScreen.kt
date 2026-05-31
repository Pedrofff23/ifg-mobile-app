package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.background
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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<User?>(null) }
    var selectedTemplateId by remember { mutableStateOf("") }
    var startsAt by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title
        Text("Meus Alunos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text("Gerencie e acompanhe seus alunos", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.searchStudents(it) },
            label = { Text("Buscar aluno...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = IfgGreen),
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
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum aluno encontrado", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
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
                            modifier = Modifier.padding(16.dp),
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
                                Text(student.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

    // Assign dialog
    if (showAssignDialog && selectedStudent != null) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false; selectedTemplateId = ""; startsAt = "" },
            title = { Text("Atribuir Treino") },
            text = {
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
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = IfgGreen),
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
                        onValueChange = { startsAt = it },
                        label = { Text("Data início (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = IfgGreen),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedTemplateId.isNotBlank() && startsAt.isNotBlank()) {
                            viewModel.assignWorkout(AssignWorkoutRequest(selectedStudent!!.id, selectedTemplateId, startsAt))
                            showAssignDialog = false
                            selectedTemplateId = ""
                            startsAt = ""
                        }
                    },
                    enabled = selectedTemplateId.isNotBlank() && startsAt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
                ) { Text("Atribuir") }
            },
            dismissButton = { TextButton(onClick = { showAssignDialog = false; selectedTemplateId = ""; startsAt = "" }) { Text("Cancelar") } }
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
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
