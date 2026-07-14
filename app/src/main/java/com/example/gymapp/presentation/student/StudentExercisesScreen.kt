package com.example.gymapp.presentation.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.core.net.toUri
import com.example.gymapp.BuildConfig
import com.example.gymapp.domain.model.Exercise
import com.example.gymapp.ui.theme.*

@Composable
fun StudentExercisesScreen(viewModel: StudentViewModel) {
    val exercises by viewModel.exercises.collectAsState()
    val error by viewModel.error.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var expandedExerciseId by remember { mutableStateOf<String?>(null) }
    val categories = listOf("Todas", "Peito", "Costas", "Pernas", "Ombros", "Bíceps", "Tríceps")

    // Trigger initial load and update on category change
    LaunchedEffect(selectedCategory) {
        val muscleGroup = if (selectedCategory == "Todas") null else selectedCategory
        viewModel.loadExercises(muscleGroup = muscleGroup)
    }

    // Apply client-side filtering for immediate results
    val filteredExercises = remember(exercises, searchQuery) {
        if (searchQuery.isBlank()) {
            exercises
        } else {
            val query = searchQuery.lowercase().trim()
            exercises.filter { 
                it.name.lowercase().contains(query) || 
                it.description?.lowercase()?.contains(query) == true 
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title
        Text(
            text = "Biblioteca de Exercícios",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        Text(
            text = "Explore todos os exercícios disponíveis",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        // Error banner
        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearUpdateStatus() }) {
                        Text("Fechar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Buscar exercícios...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = {
                        Text(
                            category,
                            color = if (selectedCategory == category) IfgGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedCategory == category) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = IfgGreen,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Exercise List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredExercises) { exercise ->
                val isExpanded = expandedExerciseId == exercise.id
                ExerciseCard(
                    exercise = exercise,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedExerciseId = if (isExpanded) null else exercise.id
                    }
                )
            }

            if (filteredExercises.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhum exercício encontrado",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                // Muscle group badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = exercise.muscleGroup ?: "Geral",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    if (!exercise.description.isNullOrBlank()) {
                        Text(
                            text = exercise.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (exercise.usesWeight == true) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Com peso",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ExerciseMultipleMediasDisplay(exercise = exercise)
                }
            }
        }
    }
}

@Composable
private fun ExerciseMultipleMediasDisplay(exercise: Exercise) {
    val context = LocalContext.current
    val medias = exercise.medias ?: emptyList()
    if (medias.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            medias.forEach { media ->
                val path = media.mediaPath
                val type = media.mediaType
                val url = media.videoUrl
                if (!path.isNullOrBlank() && !type.isNullOrBlank()) {
                    if (type == "image" || type == "gif") {
                        val mediaUrl = "${BuildConfig.SUPABASE_URL}$path"
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
                    } else if (type == "video") {
                        val videoUrl = "${BuildConfig.SUPABASE_URL}$path"
                        com.example.gymapp.presentation.components.VideoPlayer(videoUrl = videoUrl)
                    }
                } else if (!url.isNullOrBlank()) {
                    com.example.gymapp.presentation.components.VideoPlayer(videoUrl = url)
                }
            }
        }
    } else {
        if (!exercise.mediaPath.isNullOrBlank()) {
            if (exercise.mediaType == "image" || exercise.mediaType == "gif") {
                val mediaUrl = "${BuildConfig.SUPABASE_URL}${exercise.mediaPath}"
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
                val videoUrl = "${BuildConfig.SUPABASE_URL}${exercise.mediaPath}"
                com.example.gymapp.presentation.components.VideoPlayer(videoUrl = videoUrl)
            }
        } else if (!exercise.videoUrl.isNullOrBlank()) {
            com.example.gymapp.presentation.components.VideoPlayer(videoUrl = exercise.videoUrl)
        }
    }
}
