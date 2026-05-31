package com.example.gymapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.AuthService
import com.example.gymapp.domain.model.LoginRequest
import com.example.gymapp.domain.model.RegisterRequest
import com.example.gymapp.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

enum class AuthDestination {
    LOGIN,
    STUDENT_HOME,
    PROFESSOR_HOME
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authDestination = MutableStateFlow<AuthDestination?>(null)
    val authDestination: StateFlow<AuthDestination?> = _authDestination.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authService.login(LoginRequest(email, password))

                // Extract user data from response — handle nullable fields
                val supabaseUser = response.user
                val userId = supabaseUser?.id
                    ?: throw Exception("Erro: resposta do servidor não contém dados do usuário")

                val userEmail = supabaseUser.email ?: email
                val fullName = supabaseUser.userMetadata?.fullName ?: ""
                val metadataRole = supabaseUser.userMetadata?.role ?: "aluno"

                // 1. Persist session to DataStore first so AuthInterceptor has the token for subsequent calls
                tokenManager.saveSession(
                    accessToken = response.token,
                    refreshToken = response.refreshToken,
                    userId = userId,
                    role = metadataRole,
                    fullName = fullName.ifBlank { email },
                    email = userEmail
                )

                // 2. Small delay to ensure DataStore has committed
                delay(100)

                // 3. Now call /auth/me to get the authoritative role from public.users (with token attached)
                val meResponse = try {
                    authService.getMe()
                } catch (_: Exception) {
                    null
                }

                val role = meResponse?.data?.role ?: metadataRole
                val resolvedName = meResponse?.data?.fullName?.ifBlank { null } ?: fullName.ifBlank { email }

                // 4. Update session if fresh data was received from /auth/me
                if (meResponse != null) {
                    tokenManager.saveSession(
                        accessToken = response.token,
                        refreshToken = response.refreshToken,
                        userId = userId,
                        role = role,
                        fullName = resolvedName,
                        email = userEmail
                    )
                }

                val domainUser = User(
                    id = userId,
                    email = userEmail,
                    fullName = resolvedName,
                    role = role
                )
                _authState.value = AuthState.Success(domainUser)
                _authDestination.value = resolveDestination(role)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login falhou")
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authService.register(RegisterRequest(email, password, fullName))

                // Extract user data from response — handle nullable fields
                val supabaseUser = response.user
                val userId = supabaseUser?.id
                    ?: throw Exception("Erro: resposta do servidor não contém dados do usuário")

                val userEmail = supabaseUser.email ?: email
                val metadataName = supabaseUser.userMetadata?.fullName ?: fullName
                val metadataRole = supabaseUser.userMetadata?.role ?: "aluno"

                // 1. Persist session to DataStore first
                tokenManager.saveSession(
                    accessToken = response.token,
                    refreshToken = response.refreshToken,
                    userId = userId,
                    role = metadataRole,
                    fullName = metadataName.ifBlank { fullName },
                    email = userEmail
                )

                // 2. Small delay
                delay(100)

                // 3. Now call /auth/me to get the authoritative role
                val meResponse = try {
                    authService.getMe()
                } catch (_: Exception) {
                    null
                }

                val role = meResponse?.data?.role ?: metadataRole
                val resolvedName = meResponse?.data?.fullName?.ifBlank { null } ?: metadataName.ifBlank { fullName }

                // 4. Update session if fresh data was received
                if (meResponse != null) {
                    tokenManager.saveSession(
                        accessToken = response.token,
                        refreshToken = response.refreshToken,
                        userId = userId,
                        role = role,
                        fullName = resolvedName,
                        email = userEmail
                    )
                }

                val domainUser = User(
                    id = userId,
                    email = userEmail,
                    fullName = resolvedName,
                    role = role
                )
                _authState.value = AuthState.Success(domainUser)
                _authDestination.value = resolveDestination(role)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registro falhou")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
            _authState.value = AuthState.Idle
            _authDestination.value = AuthDestination.LOGIN
        }
    }

    /**
     * Checks if a valid session exists in TokenManager and validates with /auth/me.
     * Returns the appropriate AuthDestination or null if not logged in / invalid.
     */
    suspend fun checkAuth(): AuthDestination? {
        val token = tokenManager.getAccessTokenSync()
        if (token.isNullOrEmpty()) return null

        return try {
            val meResponse = authService.getMe()
            val meData = meResponse.data
            // Update stored session with fresh data from /auth/me
            tokenManager.saveSession(
                accessToken = token,
                refreshToken = tokenManager.refreshToken.first(),
                userId = meData.id,
                role = meData.role,
                fullName = meData.fullName ?: meData.email,
                email = meData.email
            )
            resolveDestination(meData.role)
        } catch (e: Exception) {
            // Token is invalid or expired - clear session
            tokenManager.clearSession()
            null
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
        _authDestination.value = null
    }

    private fun resolveDestination(role: String): AuthDestination {
        return if (role.equals("professor", ignoreCase = true) || role.equals("admin", ignoreCase = true)) {
            AuthDestination.PROFESSOR_HOME
        } else {
            AuthDestination.STUDENT_HOME
        }
    }

    // Helper to get current role from token manager (for navigation use)
    suspend fun getCurrentRole(): String? = tokenManager.getUserRoleSync()
}
