package com.example.gymapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    val userId: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ROLE] }
    val userName: Flow<String?> = context.dataStore.data.map { it[KEY_USER_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_USER_EMAIL] }

    val isLoggedIn: Flow<Boolean> = accessToken.map { !it.isNullOrEmpty() }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String?,
        userId: String,
        role: String,
        fullName: String,
        email: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            if (refreshToken != null) prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_ROLE] = role
            prefs[KEY_USER_NAME] = fullName
            prefs[KEY_USER_EMAIL] = email
        }
    }

    suspend fun updateAccessToken(token: String) {
    context.dataStore.edit { prefs ->
    prefs[KEY_ACCESS_TOKEN] = token
    }
    }

    suspend fun saveRefreshToken(token: String) {
    context.dataStore.edit { prefs ->
    prefs[KEY_REFRESH_TOKEN] = token
    }
    }

    suspend fun clearSession() {
    	context.dataStore.edit { prefs ->
    		prefs.clear()
    	}
    }

    suspend fun saveUserName(name: String) {
    context.dataStore.edit { prefs ->
        prefs[KEY_USER_NAME] = name
    }
}

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ROLE] = role
        }
    }

    suspend fun getAccessTokenSync(): String? = accessToken.first()
    suspend fun getUserIdSync(): String? = userId.first()
    suspend fun getUserRoleSync(): String? = userRole.first()
    suspend fun getUserNameSync(): String? = userName.first()
    suspend fun getUserEmailSync(): String? = userEmail.first()
    suspend fun getRefreshTokenSync(): String? = refreshToken.first()
}
