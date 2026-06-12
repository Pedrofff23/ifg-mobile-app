package com.example.gymapp.presentation.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymapp.domain.model.Announcement
import com.example.gymapp.ui.theme.*
import com.example.gymapp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCommunicationScreen(viewModel: StudentViewModel) {
    val announcements by viewModel.announcements.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnnouncements()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mural", "Instruções", "Notícias")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(
            modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.md, bottom = Spacing.sm)
        ) {
            Text(
                text = "Conteúdo",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            )
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = Spacing.lg),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Tab Content
        when (selectedTab) {
            0 -> MuralTab(announcements = announcements)
            1 -> InstrucoesTab()
            2 -> NoticiasTab(announcements = announcements)
        }
    }
}

@Composable
private fun MuralTab(announcements: List<Announcement>?) {
    if (announcements.isNullOrEmpty()) {
        EmptyState(
            icon = Icons.Default.Notifications,
            title = "Nenhum aviso no mural",
            subtitle = "Os avisos da academia aparecerão aqui."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                top = Spacing.sm,
                end = Spacing.lg,
                bottom = Spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(announcements) { announcement ->
                AnnouncementCard(announcement = announcement)
            }
        }
    }
}

@Composable
private fun AnnouncementCard(announcement: Announcement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(Spacing.sm)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = announcement.authorName ?: "Administração",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = DateUtils.formatIsoDate(announcement.publishedAt ?: announcement.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InstrucoesTab() {
    val instructions = listOf(
        Instrucao("Fundamentos", "O que são Séries e Repetições?", "Série: Um grupo de repetições consecutivas de um exercício. Repetição: Uma execução completa do movimento do exercício."),
        Instrucao("Fundamentos", "Velocidade de Execução", "A velocidade com que você realiza o movimento. Geralmente expressa em 3 números (ex: 2-0-2)."),
        Instrucao("Técnicas", "Drop Set", "Técnica onde você executa uma série até a falha, reduz o peso e continua imediatamente."),
        Instrucao("Técnicas", "Super Set", "Realizar dois exercícios consecutivos sem descanso, geralmente trabalhando músculos antagonistas."),
        Instrucao("Conceitos", "Hipertrofia", "Aumento do tamanho das fibras musculares através de treinamento resistido e nutrição adequada."),
        Instrucao("Conceitos", "Descanso e Recuperação", "Período entre as séries ou sessões de treino necessário para a recuperação muscular e crescimento.")
    )

    val categories = instructions.groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            top = Spacing.sm,
            end = Spacing.lg,
            bottom = Spacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        categories.forEach { (category, items) ->
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.xs)
                )
            }
            items(items) { instruction ->
                InstructionCard(instruction = instruction)
            }
        }
    }
}

private data class Instrucao(
    val category: String,
    val title: String,
    val content: String
)

@Composable
private fun InstructionCard(instruction: Instrucao) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = instruction.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = instruction.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoticiasTab(announcements: List<Announcement>?) {
    if (announcements.isNullOrEmpty()) {
        EmptyState(
            icon = Icons.Default.Article,
            title = "Nenhuma notícia disponível",
            subtitle = "As notícias da academia aparecerão aqui."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                top = Spacing.sm,
                end = Spacing.lg,
                bottom = Spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(announcements) { announcement ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(Spacing.sm)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Article,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = DateUtils.formatIsoDate(announcement.publishedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
