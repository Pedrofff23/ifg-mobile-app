package com.example.gymapp.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.example.gymapp.domain.model.WorkoutAssignment
import com.example.gymapp.ui.theme.*

@Composable
fun StudentHomeScreen(
    viewModel: StudentViewModel,
    onStartWorkout: (WorkoutAssignment) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val assignments by viewModel.assignments.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStudentData()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        // Welcome Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(IfgGreen, IfgGreenDark)
                        )
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = "Bem-vindo, $userName!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Continue seu treino de hoje",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.8f))
                )
            }
        }

        // Today's Workout
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Text(
                        text = "Treino de Hoje",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    )

                    if (assignments.isNotEmpty()) {
                        val activeAssignment = assignments.firstOrNull { assignment ->
                            assignment.endsAt == null
                        } ?: assignments.firstOrNull()

                        if (activeAssignment != null) {
                            // Workout summary row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LightSurfaceVariant)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = activeAssignment.templateName ?: "Treino ${activeAssignment.templateId}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "${sessions.size} sessões realizadas",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Button(
                                    onClick = { onStartWorkout(activeAssignment) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IfgGreen)
                                ) {
                                    Text("Iniciar", color = Color.White)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Nenhum treino atribuído",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Stats Grid
        item {
        Column {
        Text(
        text = "Estatísticas",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        StatCard(
        modifier = Modifier.weight(1f),
        value = "${sessions.size}",
        label = "Treinos",
        icon = Icons.Default.FitnessCenter,
        iconBgColor = Green100,
        iconTint = IfgGreen
        )
        StatCard(
        modifier = Modifier.weight(1f),
        value = "${sessions.count { it.finishedAt != null }}",
        label = "Concluídos",
        icon = Icons.Default.CheckCircle,
        iconBgColor = Green100,
        iconTint = IfgGreen
        )
        }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        StatCard(
        modifier = Modifier.weight(1f),
        value = "${assignments.size}",
        label = "Treinos Atribuídos",
        icon = Icons.Default.CalendarToday,
        iconBgColor = Purple100,
        iconTint = Color(0xFF7C3AED)
        )
        }
        }

        // Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Text(
                        text = "Ações Rápidas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    )
                    Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    OutlinedButton(
                    onClick = { onNavigate("progress") },
                    modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                    shape = RoundedCornerShape(8.dp)
                    ) {
                    Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                    ) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ver Progresso", style = MaterialTheme.typography.labelSmall)
                    }
                    }
                        OutlinedButton(
                        onClick = { onNavigate("workout_hub") },
                        modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                        shape = RoundedCornerShape(8.dp)
                        ) {
                        Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                        ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Exercícios", style = MaterialTheme.typography.labelSmall)
                        }
                        }
                    }
                }
            }
        }

        // Loading indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun StatCard(
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
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
