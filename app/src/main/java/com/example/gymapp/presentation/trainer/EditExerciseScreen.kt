package com.example.gymapp.presentation.trainer

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapp.domain.model.Exercise
import com.example.gymapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseScreen(
    exerciseId: String,
    viewModel: ProfessorViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val exercises by viewModel.exercises.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val exercise = exercises.find { it.id == exerciseId }

    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var description by remember { mutableStateOf(exercise?.description ?: "") }
    var muscleGroup by remember { mutableStateOf(exercise?.muscleGroup ?: "Peito") }
    var usesWeight by remember { mutableStateOf(exercise?.usesWeight ?: true) }
    val selectedMediaUris = remember { mutableStateListOf<Uri>() }
    val newVideoUrls = remember { mutableStateListOf<String>() }
    val keepMediaIds = remember { mutableStateListOf<String>() }
    var currentVideoUrlInput by remember { mutableStateOf("") }

    val muscleGroups = listOf("Peito", "Costas", "Ombros", "Bíceps", "Tríceps", "Pernas", "Glúteos", "Core", "Cardio")
    var groupExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(exercise) {
        exercise?.let {
            if (name.isBlank()) name = it.name
            if (description.isBlank()) description = it.description ?: ""
            muscleGroup = it.muscleGroup ?: "Peito"
            usesWeight = it.usesWeight ?: true
            if (keepMediaIds.isEmpty()) {
                it.medias?.map { m -> m.id }?.let { ids -> keepMediaIds.addAll(ids) }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.clearSuccessMessage()
        viewModel.clearError()
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedMediaUris.add(it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(successMessage) {
        successMessage?.let { msg ->
            if (msg.contains("atualizado", ignoreCase = true)) {
                viewModel.clearSuccessMessage()
                onBack()
            } else {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearSuccessMessage()
            }
        }
    }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = { Text("Editar Exercício", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Exercício") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
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

                    // Existing & New Medias List
                    val existingMedias = exercise?.medias ?: emptyList()
                    val visibleExistingMedias = existingMedias.filter { keepMediaIds.contains(it.id) }

                    if (visibleExistingMedias.isNotEmpty() || selectedMediaUris.isNotEmpty() || newVideoUrls.isNotEmpty()) {
                        Text(
                            text = "Mídias Atreladas:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            visibleExistingMedias.forEach { media ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (!media.videoUrl.isNullOrBlank()) Icons.Default.Link else Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = media.videoUrl ?: media.mediaPath?.substringAfterLast('/') ?: "Arquivo",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { keepMediaIds.remove(media.id) },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover Mídia")
                                    }
                                }
                            }

                            selectedMediaUris.forEachIndexed { index, uri ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Novo arquivo local #${index + 1}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { selectedMediaUris.remove(uri) },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover Mídia")
                                    }
                                }
                            }

                            newVideoUrls.forEach { url ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Link,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = url,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { newVideoUrls.remove(url) },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover URL")
                                    }
                                }
                            }
                        }
                    }

                    // Attach local file button
                    OutlinedButton(
                        onClick = { launcher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Anexar Nova Mídia (GIF/Imagem/Vídeo)")
                    }

                    // Add external URL field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentVideoUrlInput,
                            onValueChange = { currentVideoUrlInput = it },
                            label = { Text("Nova URL de Vídeo Externo") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(
                            onClick = {
                                if (currentVideoUrlInput.isNotBlank()) {
                                    newVideoUrls.add(currentVideoUrlInput.trim())
                                    currentVideoUrlInput = ""
                                }
                            },
                            enabled = currentVideoUrlInput.isNotBlank(),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar URL")
                        }
                    }
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val finalNewVideoUrls = newVideoUrls.toMutableList()
                        if (currentVideoUrlInput.isNotBlank() && !finalNewVideoUrls.contains(currentVideoUrlInput.trim())) {
                            finalNewVideoUrls.add(currentVideoUrlInput.trim())
                        }
                        viewModel.updateExercise(
                            id = exerciseId,
                            context = context,
                            name = name,
                            description = description.ifBlank { null },
                            muscleGroup = muscleGroup,
                            usesWeight = usesWeight,
                            keepMediaIds = keepMediaIds.toList(),
                            newVideoUrls = finalNewVideoUrls,
                            newFileUris = selectedMediaUris
                        )
                    },
                    enabled = name.isNotBlank() && muscleGroup.isNotBlank() && !isLoading,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    else Text("Salvar", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
