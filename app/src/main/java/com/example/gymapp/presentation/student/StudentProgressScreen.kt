package com.example.gymapp.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import com.example.gymapp.domain.model.BodyMeasurement
import com.example.gymapp.domain.model.WorkoutSession
import com.example.gymapp.ui.theme.*

@Composable
fun StudentProgressScreen(viewModel: StudentViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
        viewModel.loadProfile()
        viewModel.loadAssignments()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Seu Progresso",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Acompanhe sua evolução",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Error banner
        if (error != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Red100)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠", fontSize = 18.sp, color = Red500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = Red500),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearUpdateStatus() }) {
                            Text("Fechar", color = Red500, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Summary Cards (2x2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressStatCard(
                    modifier = Modifier.weight(1f),
                    value = profile?.currentWeightKg?.let { "${it}kg" } ?: "--",
                    label = "Peso Atual",
                    icon = Icons.Default.MonitorWeight,
                    iconBgColor = Green100,
                    iconTint = IfgGreen
                )
                ProgressStatCard(
                    modifier = Modifier.weight(1f),
                    value = "${sessions.size}",
                    label = "Dias Ativos",
                    icon = Icons.Default.CalendarToday,
                    iconBgColor = Green100,
                    iconTint = IfgGreen
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressStatCard(
                modifier = Modifier.weight(1f),
                value = "${sessions.count { it.finishedAt != null }}",
                label = "Treinos Completos",
                icon = Icons.Default.FitnessCenter,
                iconBgColor = Purple100,
                iconTint = Color(0xFF7C3AED)
                )
                ProgressStatCard(
                modifier = Modifier.weight(1f),
                value = "${assignments.size}",
                label = "Treinos Ativos",
                icon = Icons.Default.LocalFireDepartment,
                iconBgColor = Orange100,
                iconTint = Orange600
                )
            }
        }

        // Weekly Activity
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Atividade Semanal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
                        days.forEach { day ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(LightSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.first().toString(),
                                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Sessions
        item {
            Text(
                text = "Sessões Recentes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        items(sessions.take(10)) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Sessão ${session.sessionNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = session.startedAt?.take(10) ?: "N/A",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    if (session.finishedAt != null) {
                        Badge(
                            containerColor = Green100,
                            contentColor = IfgGreen
                        ) { Text("Concluída") }
                    } else {
                        Badge(
                            containerColor = Orange100,
                            contentColor = Orange600
                        ) { Text("Em andamento") }
                    }
                }
            }
        }

        // Measurements
        if (measurements.isNotEmpty()) {
            item {
                Text(
                    text = "Medições Recentes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(measurements.take(5)) { measurement ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Peso",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = measurement.measuredAt.take(10),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Text(
                            text = "${measurement.weightKg} kg",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = IfgGreen
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
