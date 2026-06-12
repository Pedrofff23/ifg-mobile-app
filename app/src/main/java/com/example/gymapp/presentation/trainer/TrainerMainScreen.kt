package com.example.gymapp.presentation.trainer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import com.example.gymapp.ui.theme.*

data class TrainerTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun TrainerMainScreen(
    viewModel: ProfessorViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onNavigateToAdminInstitutos: () -> Unit = {}
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
        topBar = { TrainerTopBar(isAdmin = isAdmin) },
        bottomBar = { TrainerBottomNav(navController = navController, tabs = tabs) },
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
                            popUpTo("workout_hub") { saveState = true }
                        }
                    },
                    navController = navController
                )
            }
            composable("create_workout") {
                CreateWorkoutScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("edit_template/{templateId}") { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
                EditTemplateScreen(
                    templateId = templateId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("create_exercise") {
                CreateExerciseScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("edit_exercise/{exerciseId}") { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                EditExerciseScreen(
                    exerciseId = exerciseId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("students_hub") {
                TrainerStudentsHubScreen(
                    viewModel = viewModel,
                    isAdmin = isAdmin,
                    onNavigateToAdminInstitutos = onNavigateToAdminInstitutos
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
            composable("admin_institutos") {
                AdminInstitutoScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainerTopBar(isAdmin: Boolean) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Academia IFG",
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
private fun TrainerBottomNav(
    navController: NavHostController,
    tabs: List<TrainerTab>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                },
                selected = selected,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo("dashboard") {
                                saveState = true
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}
