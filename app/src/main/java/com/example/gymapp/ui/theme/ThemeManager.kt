package com.example.gymapp.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

enum class AppThemeScheme(val displayName: String, val isDark: Boolean) {
    LIGHT_FOREST("Floresta (Claro)", false),
    LIGHT_OCEAN("Oceano (Claro)", false),
    LIGHT_SAND("Areia (Claro)", false),
    DARK_ANTIGRAVITY("Antigravity (Escuro)", true),
    DARK_MIDNIGHT("Meia-Noite (Escuro)", true),
    DARK_OBSIDIAN("Obsidiana (Escuro)", true)
}

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_THEME_SCHEME = stringPreferencesKey("theme_scheme")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val themeScheme: StateFlow<AppThemeScheme> = context.themeDataStore.data.map { prefs ->
        val name = prefs[KEY_THEME_SCHEME] ?: AppThemeScheme.DARK_ANTIGRAVITY.name
        try {
            AppThemeScheme.valueOf(name)
        } catch (_: Exception) {
            AppThemeScheme.DARK_ANTIGRAVITY
        }
    }.stateIn(scope, SharingStarted.Eagerly, AppThemeScheme.DARK_ANTIGRAVITY)

    suspend fun setThemeScheme(scheme: AppThemeScheme) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_SCHEME] = scheme.name
        }
    }

    fun setThemeSchemeAsync(scheme: AppThemeScheme) {
        scope.launch {
            setThemeScheme(scheme)
        }
    }
}
