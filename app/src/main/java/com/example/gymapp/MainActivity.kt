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
import com.example.gymapp.data.remote.ErpService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var themeManager: ThemeManager
    @Inject lateinit var erpService: ErpService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val announcementId = intent?.getStringExtra("announcement_id") ?: intent?.extras?.getString("announcement_id")
        setContent {
            val currentScheme by themeManager.themeScheme.collectAsStateWithLifecycle(
                initialValue = AppThemeScheme.DARK_ANTIGRAVITY
            )
            GymAppTheme(themeScheme = currentScheme) {
                AppNavigation(
                    themeManager = themeManager,
                    erpService = erpService,
                    initialAnnouncementId = announcementId
                )
            }
        }
    }
}
