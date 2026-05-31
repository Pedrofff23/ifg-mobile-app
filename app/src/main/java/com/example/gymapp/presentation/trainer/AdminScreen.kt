package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
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
import com.example.gymapp.domain.model.User
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: ProfessorViewModel) {
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
                "Promova usuários e altere status",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                modifier = Modifier.fillMaxWidth(),
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
            title = { Text("Alterar Status") },
            text = {
                Text(
                    if (showStatusDialog!!.isActive)
                        "Desativar o usuário \"${showStatusDialog!!.fullName ?: ""}\"? Ele não poderá fazer login."
                    else
                        "Reativar o usuário \"${showStatusDialog!!.fullName ?: ""}\"?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserStatus(showStatusDialog!!.id, !showStatusDialog!!.isActive)
                        showStatusDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showStatusDialog!!.isActive) Color(0xFFC62828) else IfgGreen
                    )
                ) {
                    Text(if (showStatusDialog!!.isActive) "Desativar" else "Reativar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = null }) { Text("Cancelar") }
            }
        )
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

            // Active/inactive indicator
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                if (user.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = if (user.isActive) "Ativo" else "Inativo",
                tint = if (user.isActive) IfgGreen else Color(0xFFC62828),
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
                    contentColor = if (user.isActive) Color(0xFFC62828) else IfgGreen
                )
            ) {
                Icon(
                    if (user.isActive) Icons.Default.PersonOff else Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (user.isActive) "Desativar" else "Reativar")
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
