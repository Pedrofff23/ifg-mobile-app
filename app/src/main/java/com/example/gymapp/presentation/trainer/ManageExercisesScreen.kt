package com.example.gymapp.presentation.trainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build
import com.example.gymapp.domain.model.*
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageExercisesScreen(viewModel: ProfessorViewModel) {
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var muscleGroupFilter by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Exercise?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Exercise?>(null) }
    var expandedExerciseId by remember { mutableStateOf<String?>(null) }

    val muscleGroups = listOf("Peito", "Costas", "Ombros", "Bíceps", "Tríceps", "Pernas", "Glúteos", "Core", "Cardio")

    val filteredExercises = exercises.filter { ex ->
        (searchQuery.isBlank() || ex.name.contains(searchQuery, ignoreCase = true)) &&
        (muscleGroupFilter == null || ex.muscleGroup.equals(muscleGroupFilter, ignoreCase = true))
    }

    LaunchedEffect(Unit) { viewModel.loadExercises() }

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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = IfgGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Exercício")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Exercícios",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Gerencie o catálogo de exercícios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar exercício...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Muscle group filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = muscleGroupFilter == null,
                    onClick = { muscleGroupFilter = null },
                    label = { Text("Todos") }
                )
                muscleGroups.forEach { group ->
                    FilterChip(
                        selected = muscleGroupFilter.equals(group, ignoreCase = true),
                        onClick = { muscleGroupFilter = if (muscleGroupFilter == group) null else group },
                        label = { Text(group) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IfgGreen)
                }
            } else if (filteredExercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhum exercício encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredExercises) { exercise ->
                        val isExpanded = expandedExerciseId == exercise.id

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Header row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = IfgGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Muscle group badge
                                    Badge(
                                        containerColor = Green100
                                    ) {
                                        Text(
                                            exercise.muscleGroup,
                                            color = IfgGreen,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    IconButton(onClick = {
                                        expandedExerciseId = if (isExpanded) null else exercise.id
                                    }) {
                                        Icon(
                                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Recolher" else "Expandir",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Expanded details
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        if (exercise.description.isNotBlank()) {
                                            Text(
                                                exercise.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Badge(containerColor = if (exercise.usesWeight) Blue100 else Orange100) {
                                                Text(
                                                    if (exercise.usesWeight) "Com peso" else "Sem peso",
                                                    color = if (exercise.usesWeight) Color(0xFF1565C0) else Orange600,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Mídia
                                        val context = LocalContext.current
                                        if (exercise.mediaPath != null) {
                                            if (exercise.mediaType == "image" || exercise.mediaType == "gif") {
                                                val mediaUrl = "http://192.168.240.1:8000/storage/v1/object/public/exercises/${exercise.mediaPath}"
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(mediaUrl)
                                                        .decoderFactory(
                                                            if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory()
                                                            else GifDecoder.Factory()
                                                        )
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Mídia do Exercício",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color.Black.copy(alpha = 0.05f))
                                                )
                                            } else if (exercise.mediaType == "video") {
                                                val videoUrl = "http://192.168.240.1:8000/storage/v1/object/public/exercises/${exercise.mediaPath}"
                                                OutlinedButton(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                                        context.startActivity(intent)
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Assistir Vídeo Anexado")
                                                }
                                            }
                                        } else if (exercise.videoUrl != null) {
                                            OutlinedButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(exercise.videoUrl))
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Link, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Assistir Vídeo (Link Externo)")
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { showEditDialog = exercise },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0))
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Editar")
                                            }
                                            OutlinedButton(
                                                onClick = { showDeleteDialog = exercise },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Excluir")
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
    }

    val context = LocalContext.current

    // Create exercise dialog
    if (showCreateDialog) {
        ExerciseFormDialog(
            title = "Novo Exercício",
            initialExercise = null,
            onDismiss = { showCreateDialog = false },
            onSave = { name, desc, muscle, weight, video, fileUri ->
                viewModel.createExercise(context, name, desc, muscle, weight, video, fileUri)
                showCreateDialog = false
            }
        )
    }

    // Edit exercise dialog
    if (showEditDialog != null) {
        ExerciseFormDialog(
            title = "Editar Exercício",
            initialExercise = showEditDialog,
            onDismiss = { showEditDialog = null },
            onSave = { name, desc, muscle, weight, video, fileUri ->
                viewModel.updateExercise(showEditDialog!!.id, context, name, desc, muscle, weight, video, fileUri)
                showEditDialog = null
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir Exercício") },
            text = { Text("Tem certeza que deseja excluir \"${showDeleteDialog!!.name}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExercise(showDeleteDialog!!.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseFormDialog(
    title: String,
    initialExercise: Exercise?,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String?, muscle: String, weight: Boolean, video: String?, fileUri: Uri?) -> Unit
) {
    var name by remember { mutableStateOf(initialExercise?.name ?: "") }
    var description by remember { mutableStateOf(initialExercise?.description ?: "") }
    var muscleGroup by remember { mutableStateOf(initialExercise?.muscleGroup ?: "Peito") }
    var usesWeight by remember { mutableStateOf(initialExercise?.usesWeight ?: true) }
    var videoUrl by remember { mutableStateOf(initialExercise?.videoUrl ?: "") }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedMediaUri = uri
    }

    val muscleGroups = listOf("Peito", "Costas", "Ombros", "Bíceps", "Tríceps", "Pernas", "Glúteos", "Core", "Cardio")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
            )
            OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
            )

                var groupExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = { groupExpanded = !groupExpanded }
                ) {
                    OutlinedTextField(
                    value = muscleGroup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grupo Muscular") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                    )
                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false }
                    ) {
                        muscleGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = { muscleGroup = group; groupExpanded = false }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usa peso?", modifier = Modifier.weight(1f))
                    Switch(
                        checked = usesWeight,
                        onCheckedChange = { usesWeight = it }
                    )
                }

                OutlinedButton(
                    onClick = { launcher.launch("*/*") }, // Allow any media type
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IfgGreen)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedMediaUri != null) "Mídia Anexada!" else "Anexar Mídia (GIF/Imagem/Vídeo)")
                }

                if (selectedMediaUri == null) {
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("URL de Vídeo Externo (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        description.ifBlank { null },
                        muscleGroup,
                        usesWeight,
                        if (selectedMediaUri == null) videoUrl.ifBlank { null } else null,
                        selectedMediaUri
                    )
                },
                enabled = name.isNotBlank() && muscleGroup.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
            ) { Text("Salvar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
