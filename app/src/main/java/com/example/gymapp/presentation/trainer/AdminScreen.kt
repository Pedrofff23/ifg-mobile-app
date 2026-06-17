package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapp.domain.model.AuditLogEntry
import com.example.gymapp.domain.model.BackgroundJob
import com.example.gymapp.domain.model.User
import com.example.gymapp.ui.theme.*
import com.example.gymapp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: ProfessorViewModel,
    onNavigateToInstitutos: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Usuários", "Auditoria", "Tarefas")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.People
                                    1 -> Icons.Default.Assignment
                                    else -> Icons.Default.WorkHistory
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when (selectedTabIndex) {
            0 -> AdminUsersTab(viewModel = viewModel, onNavigateToInstitutos = onNavigateToInstitutos)
            1 -> AdminAuditTab(viewModel = viewModel)
            2 -> AdminJobsTab(viewModel = viewModel)
        }
    }
}

@Composable
private fun AdminUsersTab(
    viewModel: ProfessorViewModel,
    onNavigateToInstitutos: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf<String?>(null) }
    var showRoleDialog by remember { mutableStateOf<User?>(null) }
    var showStatusDialog by remember { mutableStateOf<User?>(null) }

    val filteredUsers = allUsers.filter { user ->
        val name = user.fullName ?: ""
        val email = user.email ?: ""
        (searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true) || email.contains(searchQuery, ignoreCase = true)) &&
        (roleFilter == null || user.role.equals(roleFilter, ignoreCase = true))
    }

    LaunchedEffect(Unit) { viewModel.loadAllUsers() }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Gerenciamento de Usuários",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Promova, bloqueie ou desbloqueie usuários",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Manage Institutos button
            OutlinedButton(
                onClick = onNavigateToInstitutos,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IfgGreen)
            ) {
                Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gerenciar Institutos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar usuário...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Role filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = roleFilter == null,
                    onClick = { roleFilter = null },
                    label = { Text("Todos") }
                )
                listOf("aluno", "professor", "admin").forEach { role ->
                    FilterChip(
                        selected = roleFilter.equals(role, ignoreCase = true),
                        onClick = { roleFilter = if (roleFilter == role) null else role },
                        label = { Text(role.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip("Total", allUsers.size, IfgGreen, Green100)
                StatChip("Alunos", allUsers.count { it.role.equals("aluno", ignoreCase = true) }, Color(0xFF1565C0), Blue100)
                StatChip("Professores", allUsers.count { it.role.equals("professor", ignoreCase = true) }, Color(0xFFE65100), Orange100)
                StatChip("Admins", allUsers.count { it.role.equals("admin", ignoreCase = true) }, Color(0xFF6B21A8), Purple100)
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IfgGreen)
                }
            } else if (filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum usuário encontrado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onRoleClick = { showRoleDialog = user },
                            onStatusClick = { showStatusDialog = user }
                        )
                    }
                }
            }
        }
    }

    // Role change dialog
    if (showRoleDialog != null) {
        RoleChangeDialog(
            user = showRoleDialog!!,
            onDismiss = { showRoleDialog = null },
            onConfirm = { newRole ->
                viewModel.updateUserRole(showRoleDialog!!.id, newRole)
                showRoleDialog = null
            }
        )
    }

    // Status change dialog
    if (showStatusDialog != null) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = null },
            title = { Text("Alterar Bloqueio") },
            text = {
                Text(
                    if (showStatusDialog!!.isBlocked)
                        "Desbloquear o usuário \"${showStatusDialog!!.fullName ?: ""}\"?"
                    else
                        "Bloquear o usuário \"${showStatusDialog!!.fullName ?: ""}\"? Ele não poderá acessar o sistema."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserBlocked(showStatusDialog!!.id, !showStatusDialog!!.isBlocked)
                        showStatusDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showStatusDialog!!.isBlocked) Color(0xFFC62828) else IfgGreen
                    )
                ) {
                    Text(if (showStatusDialog!!.isBlocked) "Desbloquear" else "Bloquear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AdminAuditTab(viewModel: ProfessorViewModel) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAuditLogs() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Logs de Auditoria",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Registro de todas as operações no sistema",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IfgGreen)
                }
            } else if (auditLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum log de auditoria encontrado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(auditLogs) { entry ->
                        AuditLogCard(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(entry: AuditLogEntry) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (entry.action) {
                        "POST" -> Icons.Default.Add
                        "PUT", "PATCH" -> Icons.Default.Edit
                        "DELETE" -> Icons.Default.Delete
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (entry.action) {
                        "POST" -> IfgGreen
                        "PUT", "PATCH" -> Color(0xFF1565C0)
                        "DELETE" -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    entry.action,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    entry.resource,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                if (entry.ipAddress != null) {
                    Text("IP: ${entry.ipAddress}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    DateUtils.formatIsoDate(entry.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdminJobsTab(viewModel: ProfessorViewModel) {
    val jobs by viewModel.backgroundJobs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadBackgroundJobs() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Tarefas em Segundo Plano",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Status dos jobs em execução",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IfgGreen)
                }
            } else if (jobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WorkHistory, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhuma tarefa encontrada", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(jobs) { job ->
                        BackgroundJobCard(job = job)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundJobCard(job: BackgroundJob) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = when (job.status) {
                    "done" -> IfgGreen
                    "processing" -> Color(0xFF1565C0)
                    "failed" -> Color(0xFFC62828)
                    else -> Color(0xFFE65100)
                }
                val statusLabel = when (job.status) {
                    "done" -> "Concluído"
                    "processing" -> "Processando"
                    "failed" -> "Falhou"
                    else -> "Pendente"
                }
                Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    job.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text("Tentativas: ${job.retries}/${job.maxRetries}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (job.createdAt != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(DateUtils.formatIsoDate(job.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!job.error.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(job.error, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
            }
        }
    }
}

@Composable
private fun StatChip(label: String, count: Int, textColor: Color, bgColor: Color) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onRoleClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            user.role.equals("admin", ignoreCase = true) -> Color(0xFF6B21A8)
                            user.role.equals("professor", ignoreCase = true) -> Color(0xFFE65100)
                            else -> IfgGreen
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (user.fullName ?: "").take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.fullName ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Role badge
            Badge(
                containerColor = when {
                    user.role.equals("admin", ignoreCase = true) -> Purple100
                    user.role.equals("professor", ignoreCase = true) -> Orange100
                    else -> Green100
                }
            ) {
                Text(
                    user.role.replaceFirstChar { it.uppercase() },
                    color = when {
                        user.role.equals("admin", ignoreCase = true) -> Color(0xFF6B21A8)
                        user.role.equals("professor", ignoreCase = true) -> Color(0xFFE65100)
                        else -> IfgGreen
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Blocked/active indicator
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                if (user.isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                contentDescription = if (user.isBlocked) "Bloqueado" else "Ativo",
                tint = if (user.isBlocked) Color(0xFFC62828) else IfgGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRoleClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Papel")
            }
            OutlinedButton(
                onClick = onStatusClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (!user.isBlocked) Color(0xFFC62828) else IfgGreen
                )
            ) {
                Icon(
                    if (user.isBlocked) Icons.Default.Person else Icons.Default.PersonOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (user.isBlocked) "Desbloquear" else "Bloquear")
            }
        }
    }
}

@Composable
private fun RoleChangeDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(user.role) }
    val roles = listOf("aluno", "professor", "admin")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alterar Papel") },
        text = {
            Column {
                Text(
                    "Alterar papel de \"${user.fullName ?: ""}\"",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Papel atual: ${user.role.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                roles.forEach { role ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                        Text(role.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRole) },
                enabled = selectedRole != user.role,
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) { Text("Salvar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
