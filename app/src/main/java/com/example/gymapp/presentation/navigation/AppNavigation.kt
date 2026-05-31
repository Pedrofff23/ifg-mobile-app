package com.example.gymapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymapp.presentation.auth.AuthDestination
import com.example.gymapp.presentation.auth.AuthViewModel
import com.example.gymapp.presentation.auth.LoginScreen
import com.example.gymapp.presentation.auth.RegisterScreen
import com.example.gymapp.presentation.student.StudentMainScreen
import com.example.gymapp.presentation.student.WorkoutSessionScreen
import com.example.gymapp.presentation.trainer.TrainerMainScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val STUDENT_HOME = "student_home"
    const val TRAINER_HOME = "trainer_home"
    const val WORKOUT_SESSION = "workout_session/{assignmentId}"

    fun workoutSessionRoute(assignmentId: String) = "workout_session/$assignmentId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    // Shared logout handler: clears session and navigates to login
    val performLogout: () -> Unit = {
        authViewModel.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    // Navigate to workout session
    val startWorkout: (String) -> Unit = { assignmentId ->
        navController.navigate(Routes.workoutSessionRoute(assignmentId))
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                authViewModel = authViewModel,
                onChecked = { destination ->
                    val route = when (destination) {
                    AuthDestination.STUDENT_HOME -> Routes.STUDENT_HOME
                    AuthDestination.PROFESSOR_HOME -> Routes.TRAINER_HOME
                    AuthDestination.LOGIN -> Routes.LOGIN
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = if (role.equals("professor", ignoreCase = true) || role.equals("admin", ignoreCase = true)) {
                        Routes.TRAINER_HOME
                    } else {
                        Routes.STUDENT_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    val destination = if (role.equals("professor", ignoreCase = true) || role.equals("admin", ignoreCase = true)) {
                        Routes.TRAINER_HOME
                    } else {
                        Routes.STUDENT_HOME
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.STUDENT_HOME) {
            StudentMainScreen(
                onStartWorkout = { assignment ->
                    navController.navigate(Routes.workoutSessionRoute(assignment.id))
                },
                onLogout = performLogout
            )
        }

        composable(Routes.TRAINER_HOME) {
            TrainerMainScreen(
                onLogout = performLogout
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

@Composable
private fun SplashScreen(
    authViewModel: AuthViewModel,
    onChecked: (AuthDestination) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()

        LaunchedEffect(Unit) {
            val destination = authViewModel.checkAuth()
            onChecked(destination ?: AuthDestination.LOGIN)
        }
    }
}
