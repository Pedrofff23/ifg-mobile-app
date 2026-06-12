package com.example.gymapp.presentation.student

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymapp.domain.model.WorkoutAssignment
import com.example.gymapp.presentation.navigation.Routes
import com.example.gymapp.ui.theme.*
import kotlinx.coroutines.launch

data class StudentTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun StudentMainScreen(
    viewModel: StudentViewModel = hiltViewModel(),
    themeManager: ThemeManager,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        StudentTab("home", "Home", Icons.Default.Home, Icons.Filled.Home),
        StudentTab("workout_hub", "Treinos", Icons.Default.FitnessCenter, Icons.Filled.FitnessCenter),
        StudentTab("progress", "Progresso", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Filled.TrendingUp),
        StudentTab("communication", "Avisos", Icons.Default.Notifications, Icons.Filled.Notifications),
        StudentTab("profile", "Perfil", Icons.Default.Person, Icons.Filled.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = { 
            if (currentRoute != Routes.WORKOUT_SESSION) {
                StudentTopBar() 
            }
        },
        bottomBar = { StudentBottomNav(navController = navController, tabs = tabs) },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                StudentHomeScreen(
                    viewModel = viewModel,
                    onStartWorkout = { assignment ->
                        navController.navigate(Routes.workoutSessionRoute(assignment.id))
                    },
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    snackbarHostState = snackbarHostState
                )
            }
            composable("workout_hub") {
                StudentWorkoutHubScreen(
                    viewModel = viewModel,
                    onStartWorkout = { assignment ->
                        navController.navigate(Routes.workoutSessionRoute(assignment.id))
                    }
                )
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
            composable(
                route = Routes.WORKOUT_SESSION,
                arguments = listOf(
                    navArgument("assignmentId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: return@composable
                WorkoutSessionScreen(
                    assignmentId = assignmentId,
                    onFinish = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentTopBar() {
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
                    text = "Anápolis",
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
private fun StudentBottomNav(
    navController: NavHostController,
    tabs: List<StudentTab>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.background(
            MaterialTheme.colorScheme.surface
        )
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label
                    )
                },
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
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
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
