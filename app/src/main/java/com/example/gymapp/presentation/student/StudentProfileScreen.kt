package com.example.gymapp.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapp.domain.model.AlunoProfile
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentViewModel,
    themeManager: ThemeManager,
    onLogout: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    val currentTheme by themeManager.themeScheme.collectAsState(initial = AppThemeScheme.DARK_ANTIGRAVITY)

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogWeightDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val userEmail by viewModel.userEmail.collectAsState()

    LaunchedEffect(Unit) {
    viewModel.loadProfile()
    }

    // Show snackbar for success/error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(updateSuccess, error) {
        val msg = updateSuccess ?: error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUpdateStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(IfgGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = (userName ?: "").split(" ").filter { it.isNotBlank() }.mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                            Text(
                                text = initials.ifBlank { "?" },
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (userName ?: "").ifBlank { "Estudante" },
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Badge(containerColor = Green100, contentColor = IfgGreen) { Text("Estudante") }
                            Badge(containerColor = Blue100, contentColor = Color(0xFF2563EB)) { Text("Ativo") }
                        }
                    }
                }
            }

            // Personal Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informações Pessoais",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        ProfileInfoRow(icon = Icons.Default.Person, label = "Nome", value = (userName ?: "").ifBlank { "N/A" })
                        ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = (userEmail ?: "").ifBlank { "N/A" })
                        ProfileInfoRow(icon = Icons.Default.CalendarToday, label = "Membro desde", value = profile?.createdAt?.take(10) ?: "N/A")
                    }
                }
            }

            // Physical Data Card with Edit button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dados Físicos",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            IconButton(onClick = { showEditProfileDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = IfgGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.MonitorWeight,
                            label = "Peso",
                            value = profile?.currentWeightKg?.let { "${it} kg" } ?: "N/A"
                        )
                        ProfileInfoRow(
                            icon = Icons.Default.Height,
                            label = "Altura",
                            value = profile?.heightCm?.let { "${it} cm" } ?: "N/A"
                        )
                        val bmi = if (profile != null && (profile?.currentWeightKg ?: 0.0) > 0.0 && (profile?.heightCm ?: 0.0) > 0.0) {
                            val h = (profile?.heightCm ?: 0.0) / 100.0
                            String.format("%.1f", (profile?.currentWeightKg ?: 0.0) / (h * h))
                        } else "N/A"
                        ProfileInfoRow(icon = Icons.Default.Analytics, label = "IMC", value = bmi)
                        if (!profile?.injuryHistory.isNullOrBlank()) {
                            ProfileInfoRow(
                                icon = Icons.Default.Healing,
                                label = "Histórico de Lesões",
                                value = profile?.injuryHistory ?: ""
                            )
                        }
                    }
                }
            }

            // Log Weight Button
            item {
                OutlinedButton(
                    onClick = { showLogWeightDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IfgGreen)
                ) {
                    Icon(Icons.Default.MonitorWeight, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Peso")
                }
            }

            // Theme selection button
            item {
                OutlinedButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tema do App")
                        Text(
                            text = currentTheme.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Recent Measurements
            if (measurements.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Medições Recentes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            measurements.take(5).forEach { m ->
                                ProfileInfoRow(
                                    icon = Icons.Default.Straighten,
                                    label = m.measuredAt?.take(10) ?: "Medição",
                                    value = "${m.weightKg} kg"
                                )
                            }
                        }
                    }
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Red500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da Conta", color = Color.White)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            profile = profile,
            isUpdating = isUpdating,
            onDismiss = { showEditProfileDialog = false },
            onSave = { weight, height, injuries ->
                viewModel.updateProfile(heightCm = height, currentWeightKg = weight, injuryHistory = injuries)
                showEditProfileDialog = false
            }
        )
    }

    // Log Weight Dialog
    if (showLogWeightDialog) {
        LogWeightDialog(
            isUpdating = isUpdating,
            onDismiss = { showLogWeightDialog = false },
            onSave = { weightKg, notes ->
                viewModel.addMeasurement(weightKg = weightKg, notes = notes)
                showLogWeightDialog = false
            }
        )
    }

    // Theme selection dialog
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
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppThemeScheme,
    onThemeSelected: (AppThemeScheme) -> Unit,
    onDismiss: () -> Unit
) {
    val schemes = AppThemeScheme.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Tema") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Claro",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.filter { !it.isDark }.forEach { scheme ->
                    ThemeOptionRow(
                        scheme = scheme,
                        isSelected = currentTheme == scheme,
                        onClick = { onThemeSelected(scheme) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Escuro",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                schemes.filter { it.isDark }.forEach { scheme ->
                    ThemeOptionRow(
                        scheme = scheme,
                        isSelected = currentTheme == scheme,
                        onClick = { onThemeSelected(scheme) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Concluído") }
        }
    )
}

@Composable
private fun ThemeOptionRow(
    scheme: AppThemeScheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when (scheme) {
        AppThemeScheme.LIGHT_FOREST -> LightForestBg
        AppThemeScheme.LIGHT_OCEAN -> LightOceanBg
        AppThemeScheme.LIGHT_SAND -> LightSandBg
        AppThemeScheme.DARK_ANTIGRAVITY -> DarkAntigravityBg
        AppThemeScheme.DARK_MIDNIGHT -> DarkMidnightBg
        AppThemeScheme.DARK_OBSIDIAN -> DarkObsidianBg
    }
    val accentColor = when (scheme) {
        AppThemeScheme.LIGHT_FOREST -> LightForestPrimary
        AppThemeScheme.LIGHT_OCEAN -> LightOceanPrimary
        AppThemeScheme.LIGHT_SAND -> LightSandPrimary
        AppThemeScheme.DARK_ANTIGRAVITY -> DarkAntigravityPrimary
        AppThemeScheme.DARK_MIDNIGHT -> DarkMidnightPrimary
        AppThemeScheme.DARK_OBSIDIAN -> DarkObsidianPrimary
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview swatch
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .align(Alignment.BottomEnd)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = scheme.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: AlunoProfile?,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (weight: Double, height: Double, injuries: String?) -> Unit
) {
    var weightText by remember { mutableStateOf(profile?.currentWeightKg?.toString() ?: "") }
    var heightText by remember { mutableStateOf(profile?.heightCm?.toString() ?: "") }
    var injuriesText by remember { mutableStateOf(profile?.injuryHistory ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Dados Físicos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Peso (kg)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Altura (cm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                value = injuriesText,
                onValueChange = { injuriesText = it },
                label = { Text("Histórico de Lesões") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull() ?: return@Button
                    val h = heightText.toDoubleOrNull() ?: return@Button
                    onSave(w, h, injuriesText.ifBlank { null })
                },
                enabled = !isUpdating && weightText.toDoubleOrNull() != null && heightText.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) {
                if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Salvar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun LogWeightDialog(
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (weightKg: Double, notes: String?) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Peso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Peso atual (kg)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Observações (opcional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull() ?: return@Button
                    onSave(w, notesText.ifBlank { null })
                },
                enabled = !isUpdating && weightText.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) {
                if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Registrar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
