package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerProfileScreen(
    viewModel: ProfessorViewModel,
    themeManager: ThemeManager,
    onLogout: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null
) {
    val userName by viewModel.userName.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val error by viewModel.error.collectAsState()

    val currentTheme by themeManager.themeScheme.collectAsState(initial = AppThemeScheme.DARK_ANTIGRAVITY)

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val internalSnackbarHostState = remember { SnackbarHostState() }
    val effectiveSnackbarHostState = snackbarHostState ?: internalSnackbarHostState

    LaunchedEffect(successMessage, error) {
        val msg = successMessage ?: error
        if (msg != null) {
            effectiveSnackbarHostState.showSnackbar(msg)
            viewModel.clearSuccessMessage()
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                top = 0.dp,
                end = Spacing.lg,
                bottom = Spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Profile Header Card
            item {
                ProfileHeaderCard(
                    userName = userName,
                    isAdmin = isAdmin,
                    onEdit = { showEditProfileDialog = true }
                )
            }

            // Personal Information
            item {
                PersonalInfoCard(
                    userName = userName,
                    isAdmin = isAdmin
                )
            }

            // Theme Selection Preference
            item {
                PreferenceCard(
                    icon = Icons.Default.Palette,
                    title = "Tema do App",
                    subtitle = currentTheme.displayName,
                    onClick = { showThemeDialog = true }
                )
            }

            // Logout Button
            item {
                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(Spacing.md)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text("Sair da Conta", color = Color.White)
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }

        if (snackbarHostState == null) {
            SnackbarHost(
                hostState = internalSnackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.lg)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }

    // Edit Name Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = userName ?: "",
            isUpdating = isLoading,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName ->
                viewModel.updateUserName(fullName = newName, instituto = null)
                showEditProfileDialog = false
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { scheme ->
                themeManager.setThemeSchemeAsync(scheme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Sair da Conta") },
            text = { Text("Tem certeza que deseja desconectar? Você precisará fazer login novamente.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(Spacing.md)
                ) {
                    Text("Sair", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(Spacing.lg)
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    userName: String?,
    isAdmin: Boolean,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                val initials = (userName ?: "")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                Text(
                    text = initials.ifBlank { "?" },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = (userName ?: "").ifBlank { "Professor" },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Surface(
                    shape = RoundedCornerShape(Spacing.sm),
                    color = Green100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = IfgGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isAdmin) "Admin" else "Professor",
                            style = MaterialTheme.typography.labelSmall,
                            color = IfgGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(Spacing.sm),
                    color = Blue100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Blue600, modifier = Modifier.size(14.dp))
                        Text("Ativo", style = MaterialTheme.typography.labelSmall, color = Blue600, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            FilledTonalIconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar perfil",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PersonalInfoCard(
    userName: String?,
    isAdmin: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "Informações Pessoais",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = Spacing.md)
            )
            ProfileInfoRow(Icons.Default.Person, "Nome", (userName ?: "").ifBlank { "N/A" })
            ProfileInfoRow(
                Icons.Default.AdminPanelSettings,
                "Papel",
                if (isAdmin) "Administrador" else "Professor"
            )
            ProfileInfoRow(Icons.Default.CheckCircle, "Status", "Ativo")
        }
    }
}

@Composable
private fun PreferenceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.md))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit
) {
    var nameText by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nome completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(Spacing.md)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(nameText.trim())
                },
                enabled = !isUpdating && nameText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(Spacing.md)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Salvar", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(Spacing.lg)
    )
}

