package com.example.gymapp.presentation.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.gymapp.ui.theme.*

data class TrainerTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun TrainerMainScreen(
    viewModel: ProfessorViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    val tabs = listOf(
    	TrainerTab("dashboard", "Início", Icons.Default.Home),
    	TrainerTab("workout_hub", "Treinos", Icons.Default.FitnessCenter),
    	TrainerTab("students_hub", "Alunos", Icons.Default.People),
    	TrainerTab("groups", "Grupos", Icons.Default.Group),
    	TrainerTab("announcements", "Avisos", Icons.Default.Notifications),
    	TrainerTab("profile", "Perfil", Icons.Default.Person)
    )

    val isAdmin by viewModel.isAdmin.collectAsState()

    Scaffold(
        topBar = { TrainerHeader(isAdmin = isAdmin) },
        bottomBar = { TrainerBottomBar(navController = navController, tabs = tabs) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                ProfessorDashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("workout_hub") {
                TrainerWorkoutHubScreen(
                    viewModel = viewModel,
                    onNavigateToCreateWorkout = {
                        navController.navigate("create_workout") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("create_workout") {
                CreateWorkoutScreen(viewModel = viewModel)
            }
            composable("students_hub") {
            	TrainerStudentsHubScreen(
            		viewModel = viewModel,
            		isAdmin = isAdmin
            	)
            }
            composable("groups") {
            	ManageGroupsScreen(viewModel = viewModel)
            }
            composable("announcements") {
                CreateAnnouncementScreen(viewModel = viewModel)
            }
            composable("profile") {
                TrainerProfileScreen(
                    viewModel = viewModel,
                    themeManager = viewModel.themeManager,
                    onLogout = onLogout
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainerHeader(isAdmin: Boolean) {
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
                    text = if (isAdmin) "Painel do Admin" else "Painel do Professor",
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
private fun TrainerBottomBar(
    navController: NavHostController,
    tabs: List<TrainerTab>
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
