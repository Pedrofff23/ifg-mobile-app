package com.example.gymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymapp.ui.theme.AppThemeScheme
import com.example.gymapp.ui.theme.GymAppTheme
import com.example.gymapp.ui.theme.ThemeManager
import com.example.gymapp.presentation.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentScheme by themeManager.themeScheme.collectAsStateWithLifecycle(
                initialValue = AppThemeScheme.DARK_ANTIGRAVITY
            )
            GymAppTheme(themeScheme = currentScheme) {
                AppNavigation(themeManager = themeManager)
            }
        }
    }
}
