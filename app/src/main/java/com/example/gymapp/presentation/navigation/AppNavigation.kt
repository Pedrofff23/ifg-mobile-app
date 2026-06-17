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
import com.example.gymapp.presentation.auth.ActivationPendingScreen
import com.example.gymapp.presentation.auth.AuthDestination
import com.example.gymapp.presentation.auth.AuthState
import com.example.gymapp.presentation.auth.AuthViewModel
import com.example.gymapp.presentation.auth.CompleteProfileScreen
import com.example.gymapp.presentation.auth.LoginScreen
import com.example.gymapp.presentation.auth.RegisterScreen
import com.example.gymapp.presentation.auth.BlockedScreen
import com.example.gymapp.presentation.student.StudentMainScreen
import com.example.gymapp.presentation.student.WorkoutSessionScreen
import com.example.gymapp.presentation.trainer.AdminInstitutoScreen
import com.example.gymapp.presentation.trainer.TrainerMainScreen
import com.example.gymapp.ui.theme.ThemeManager

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val COMPLETE_PROFILE = "complete_profile"
    const val ACTIVATION_PENDING = "activation_pending?email={email}"
    const val BLOCKED = "blocked"
    const val STUDENT_HOME = "student_home"
    const val TRAINER_HOME = "trainer_home"
    const val ADMIN_INSTITUTOS = "admin_institutos"
    const val WORKOUT_SESSION = "workout_session/{assignmentId}"

    fun workoutSessionRoute(assignmentId: String) = "workout_session/$assignmentId"
    fun activationPendingRoute(email: String) = "activation_pending?email=$email"
}

@Composable
fun AppNavigation(themeManager: ThemeManager) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    val performLogout: () -> Unit = {
        authViewModel.logoutAndNavigate {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
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
                        AuthDestination.COMPLETE_PROFILE -> Routes.COMPLETE_PROFILE
                        AuthDestination.ACTIVATION_PENDING -> Routes.activationPendingRoute(authViewModel.getLastEmail())
                        AuthDestination.BLOCKED -> Routes.BLOCKED
                        AuthDestination.REGISTER -> Routes.REGISTER
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
                onLoginSuccess = { destination ->
                    val route = when (destination) {
                        AuthDestination.STUDENT_HOME -> Routes.STUDENT_HOME
                        AuthDestination.PROFESSOR_HOME -> Routes.TRAINER_HOME
                        AuthDestination.COMPLETE_PROFILE -> Routes.COMPLETE_PROFILE
                        AuthDestination.ACTIVATION_PENDING -> Routes.activationPendingRoute(authViewModel.getLastEmail())
                        AuthDestination.BLOCKED -> Routes.BLOCKED
                        else -> Routes.LOGIN
                    }
                    navController.navigate(route) {
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
                onRegisterSuccess = { email ->
                    navController.navigate(Routes.activationPendingRoute(email)) {
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

        composable(Routes.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                authViewModel = authViewModel,
                onProfileCompleted = {
                    val state = authViewModel.authState.value
                    if (state is AuthState.Success) {
                        val route = if (state.user.role.equals("professor", ignoreCase = true) ||
                            state.user.role.equals("admin", ignoreCase = true)
                        ) {
                            Routes.TRAINER_HOME
                        } else {
                            Routes.STUDENT_HOME
                        }
                        navController.navigate(route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onBackToLogin = {
                    authViewModel.logoutAndNavigate {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.ACTIVATION_PENDING,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ActivationPendingScreen(
                email = email,
                authViewModel = authViewModel,
                onResendSuccess = { /* Show snackbar */ },
                onBackToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.STUDENT_HOME) {
            StudentMainScreen(
                themeManager = themeManager,
                onLogout = performLogout
            )
        }

        composable(Routes.TRAINER_HOME) {
            TrainerMainScreen(
                onLogout = performLogout,
                onNavigateToAdminInstitutos = {
                    navController.navigate(Routes.ADMIN_INSTITUTOS)
                }
            )
        }

        composable(Routes.ADMIN_INSTITUTOS) {
            AdminInstitutoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BLOCKED) {
            BlockedScreen(
                onLogout = performLogout
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
