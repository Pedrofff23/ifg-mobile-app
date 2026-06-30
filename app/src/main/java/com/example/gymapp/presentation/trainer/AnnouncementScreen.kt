package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.gymapp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAnnouncementScreen(viewModel: ProfessorViewModel) {
    val announcements by viewModel.announcements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val typeFilter by viewModel.announcementTypeFilter.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("aviso") }
    var showCreateForm by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var announcementToDelete by remember { mutableStateOf<Announcement?>(null) }
    var showEditDialog by remember { mutableStateOf<Announcement?>(null) }

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

    LaunchedEffect(Unit) { viewModel.loadAnnouncements() }

    val typeOptions = listOf(
        "noticia" to "Notícia",
        "aviso" to "Aviso",
        "instrucoes" to "Instruções"
    )

    @Composable
    fun typeColor(type: String?): Color = when (type) {
        "noticia" -> MaterialTheme.colorScheme.primary
        "aviso" -> Orange600
        "instrucoes" -> IfgGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    @Composable
    fun typeBgColor(type: String?): Color = when (type) {
        "noticia" -> MaterialTheme.colorScheme.primaryContainer
        "aviso" -> Orange100
        "instrucoes" -> Green100
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!showCreateForm) {
                FloatingActionButton(
                    onClick = { showCreateForm = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Aviso")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (showCreateForm) {
                // Create form
                Text("Novo Aviso", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("Crie um aviso para os alunos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                        )
                        OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Conteúdo") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                        )

                        // Type selector
                        Text("Tipo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            typeOptions.forEach { (value, label) ->
                                FilterChip(
                                    selected = selectedType == value,
                                    onClick = { selectedType = value },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = typeBgColor(value),
                                        selectedLabelColor = typeColor(value)
                                    )
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showCreateForm = false; title = ""; content = ""; selectedType = "aviso" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Cancelar") }
                                Button(
                                onClick = {
                                    if (title.isNotBlank() && content.isNotBlank()) {
                                        viewModel.createAnnouncement(CreateAnnouncementRequest(title, content, selectedType))
                                        title = ""
                                        content = ""
                                        selectedType = "aviso"
                                        showCreateForm = false
                                    }
                                },
                                enabled = title.isNotBlank() && content.isNotBlank() && !isLoading,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Publicar", color = MaterialTheme.colorScheme.onPrimary) }
                        }
                    }
                }
            } else {
                // Announcements list
                Text("Mural de Avisos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("Gerencie os avisos da academia", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Type filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = typeFilter == null,
                        onClick = { viewModel.setAnnouncementTypeFilter(null) },
                        label = { Text("Todos") }
                    )
                    typeOptions.forEach { (value, label) ->
                        FilterChip(
                            selected = typeFilter == value,
                            onClick = { viewModel.setAnnouncementTypeFilter(value) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = typeBgColor(value),
                                selectedLabelColor = typeColor(value)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IfgGreen)
                    }
                } else if (announcements.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nenhum aviso publicado", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(announcements) { announcement ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            when (announcement.type) {
                                                "noticia" -> Icons.Default.Article
                                                "instrucoes" -> Icons.Default.MenuBook
                                                else -> Icons.Default.Notifications
                                            },
                                            contentDescription = null,
                                            tint = typeColor(announcement.type),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(announcement.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                                        // Type badge
                                        Badge(
                                            containerColor = typeBgColor(announcement.type),
                                            contentColor = typeColor(announcement.type)
                                        ) {
                                            Text(
                                                when (announcement.type) {
                                                    "noticia" -> "Notícia"
                                                    "instrucoes" -> "Instruções"
                                                    else -> "Aviso"
                                                },
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        IconButton(
                                            onClick = { showEditDialog = announcement },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { announcementToDelete = announcement; showDeleteDialog = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(announcement.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        if (announcement.authorName != null) {
                                            Text("Por: ${announcement.authorName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }
                                        if (announcement.publishedAt != null) {
                                            Text(DateUtils.formatIsoDate(announcement.publishedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && announcementToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; announcementToDelete = null },
            title = { Text("Excluir Aviso") },
            text = { Text("Tem certeza que deseja excluir o aviso \"${announcementToDelete!!.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAnnouncement(announcementToDelete!!.id)
                        showDeleteDialog = false
                        announcementToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir", color = MaterialTheme.colorScheme.onError) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false; announcementToDelete = null }) { Text("Cancelar") } }
        )
    }

    // Edit dialog
    if (showEditDialog != null) {
        val editAnn = showEditDialog!!
        AnnouncementEditDialog(
            announcement = editAnn,
            onDismiss = { showEditDialog = null },
            onSave = { title, content, type ->
                viewModel.updateAnnouncement(
                    editAnn.id,
                    UpdateAnnouncementRequest(
                        title = title.takeIf { it.isNotBlank() },
                        content = content.takeIf { it.isNotBlank() },
                        type = type
                    )
                )
                showEditDialog = null
            }
        )
    }
}

@Composable
private fun AnnouncementEditDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, type: String) -> Unit
) {
    var title by remember { mutableStateOf(announcement.title) }
    var content by remember { mutableStateOf(announcement.content) }
    var selectedType by remember { mutableStateOf(announcement.type ?: "aviso") }

    val typeOptions = listOf(
        "noticia" to "Notícia",
        "aviso" to "Aviso",
        "instrucoes" to "Instruções"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Aviso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Conteúdo") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("Tipo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    typeOptions.forEach { (value, label) ->
                        FilterChip(
                            selected = selectedType == value,
                            onClick = { selectedType = value },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, selectedType) },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Salvar", color = MaterialTheme.colorScheme.onPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
