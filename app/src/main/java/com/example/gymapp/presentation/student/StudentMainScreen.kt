package com.example.gymapp.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymapp.domain.model.WorkoutAssignment
import com.example.gymapp.ui.theme.*

import com.example.gymapp.ui.theme.ThemeManager

data class StudentTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun StudentMainScreen(
    viewModel: StudentViewModel = hiltViewModel(),
    themeManager: ThemeManager,
    onStartWorkout: (WorkoutAssignment) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    val tabs = listOf(
        StudentTab("home", "Home", Icons.Default.Home),
        StudentTab("workout_hub", "Treinos", Icons.Default.FitnessCenter),
        StudentTab("progress", "Progresso", Icons.AutoMirrored.Filled.TrendingUp),
        StudentTab("communication", "Conteúdo", Icons.AutoMirrored.Filled.Chat),
        StudentTab("profile", "Perfil", Icons.Default.Person)
    )

    Scaffold(
        topBar = { StudentHeader() },
        bottomBar = { StudentBottomBar(navController = navController, tabs = tabs) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                StudentHomeScreen(viewModel = viewModel, onStartWorkout = onStartWorkout, onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                })
            }
            composable("workout_hub") {
                StudentWorkoutHubScreen(viewModel = viewModel, onStartWorkout = onStartWorkout)
            }
            composable("progress") {
                StudentProgressScreen(viewModel = viewModel)
            }
            composable("communication") {
                StudentCommunicationScreen(viewModel = viewModel)
            }
            composable("profile") {
                StudentProfileScreen(
                    viewModel = viewModel,
                    themeManager = themeManager,
                    onLogout = onLogout
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentHeader() {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Academia IFG Anápolis",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Instituto Federal de Goiás",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(IfgGreen, IfgGreenDark)
            )
        )
    )
}

@Composable
private fun StudentBottomBar(
    navController: NavHostController,
    tabs: List<StudentTab>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
