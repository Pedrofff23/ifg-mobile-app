package com.example.gymapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.AuthService
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.ResendActivationRequest
import com.example.gymapp.domain.model.*
import com.example.gymapp.utils.ErrorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class NeedsProfileCompletion(val user: User) : AuthState()
    data class NeedsActivation(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

enum class AuthDestination {
    LOGIN,
    REGISTER,
    COMPLETE_PROFILE,
    ACTIVATION_PENDING,
    STUDENT_HOME,
    PROFESSOR_HOME
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val erpService: ErpService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _institutos = MutableStateFlow<List<Instituto>>(emptyList())
    val institutos: StateFlow<List<Instituto>> = _institutos.asStateFlow()

    private val _isInstitutosLoading = MutableStateFlow(false)
    val isInstitutosLoading: StateFlow<Boolean> = _isInstitutosLoading.asStateFlow()

    fun loadInstitutos() {
        viewModelScope.launch {
            _isInstitutosLoading.value = true
            try {
                val response = erpService.getInstitutos(limit = 100)
                _institutos.value = response.data ?: emptyList()
            } catch (_: Exception) {
                // Silently fail
            } finally {
                _isInstitutosLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Call Supabase signup - this always returns 200 if the request is valid
                // even if email confirmation is required
                authService.register(RegisterRequest(email, password, fullName))

                // Always navigate to activation pending page after successful register
                // The user needs to check their email to activate the account
                _authState.value = AuthState.NeedsActivation(email)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    ErrorUtils.parseErrorMessage(e, "Falha ao criar conta")
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Try to login via Supabase
                val response = authService.login(LoginRequest(email, password))

                // Login succeeded - save session
                val token = response.token
                val refreshToken = response.refreshToken
                val supabaseUser = response.user

                if (token.isNullOrEmpty()) {
                    throw Exception("Token não recebido")
                }

                val userId = supabaseUser?.id ?: ""
                val userEmail = supabaseUser?.email ?: email
                val fullName = supabaseUser?.userMetadata?.fullName ?: ""
                val role = supabaseUser?.userMetadata?.role ?: "aluno"

                tokenManager.saveSession(
                    accessToken = token,
                    refreshToken = refreshToken,
                    userId = userId,
                    role = role,
                    fullName = fullName.ifBlank { email },
                    email = userEmail
                )

                // Check if profile is completed by calling /auth/me
                try {
                    val meResponse = authService.getMe()
                    val meData = meResponse.data

                    if (meData != null) {
                        val profileCompleted = meData.profileCompleted
                        val institutoId = meData.institutoId
                        val isActive = meData.isActive

                        // Sync the database role to tokenManager (overrides stale Supabase metadata role)
                        val dbRole = meData.role
                        if (dbRole != role) {
                            tokenManager.saveUserRole(dbRole)
                        }

                        val domainUser = User(
                            id = meData.id,
                            email = meData.email,
                            fullName = meData.fullName,
                            role = dbRole,
                            isActive = isActive,
                            institutoId = institutoId,
                            instituto = meData.instituto,
                            profileCompleted = profileCompleted
                        )

                        if (!isActive) {
                            _authState.value = AuthState.NeedsActivation(meData.email)
                        } else if (!profileCompleted || institutoId.isNullOrEmpty()) {
                            _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                        } else {
                            _authState.value = AuthState.Success(domainUser)
                        }
                    } else {
                        // No profile data yet - needs profile completion
                        val domainUser = User(id = userId, email = userEmail, fullName = fullName, role = role)
                        _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                    }
                } catch (_: Exception) {
                    // /auth/me failed - user might not have completed profile yet
                    val domainUser = User(id = userId, email = userEmail, fullName = fullName, role = role)
                    _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                }
            } catch (e: Exception) {
                // Login failed - check if email not confirmed
                val errorMsg = e.message ?: ""
                if (errorMsg == "EMAIL_NOT_CONFIRMED" ||
                    errorMsg.contains("confirm", ignoreCase = true) ||
                    errorMsg.contains("Email not confirmed", ignoreCase = true) ||
                    errorMsg.contains("400", ignoreCase = true) ||
                    errorMsg.contains("invalid", ignoreCase = true) ||
                    errorMsg.contains("Invalid login", ignoreCase = true)
                ) {
                    // Email not confirmed - send to activation pending
                    _authState.value = AuthState.NeedsActivation(email)
                } else {
                    _authState.value = AuthState.Error(
                        ErrorUtils.parseErrorMessage(e, "Falha no login")
                    )
                }
            }
        }
    }

    fun completeProfile(
        institutoId: String,
        weightKg: Double? = null,
        heightCm: Double? = null,
        injuryHistory: String? = null
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authService.completeProfile(
                    CompleteProfileRequest(institutoId, weightKg, heightCm, injuryHistory)
                )
                val meData = response.data

                if (meData != null && meData.profileCompleted) {
                    if (meData.isActive) {
                        val domainUser = User(
                            id = meData.id,
                            email = meData.email,
                            fullName = meData.fullName,
                            role = meData.role,
                            isActive = meData.isActive,
                            institutoId = meData.institutoId,
                            instituto = meData.instituto,
                            profileCompleted = meData.profileCompleted
                        )
                        _authState.value = AuthState.Success(domainUser)
                    } else {
                        _authState.value = AuthState.NeedsActivation(meData.email)
                    }
                } else {
                    val email = tokenManager.getUserEmailSync() ?: ""
                    _authState.value = AuthState.NeedsActivation(email)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    ErrorUtils.parseErrorMessage(e, "Falha ao completar perfil")
                )
            }
        }
    }

    fun resendActivation(email: String) {
        viewModelScope.launch {
            try {
                authService.resendActivation(ResendActivationRequest(email))
            } catch (_: Exception) {
                // Silently fail
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
            _authState.value = AuthState.Idle
        }
    }

    fun logoutAndNavigate(onComplete: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearSession()
            _authState.value = AuthState.Idle
            kotlinx.coroutines.delay(200)
            onComplete()
        }
    }

    suspend fun checkAuth(): AuthDestination? {
        val token = tokenManager.getAccessTokenSync()
        if (token.isNullOrEmpty()) return null

        return try {
            val meResponse = authService.getMe()
            val meData = meResponse.data ?: return null

            tokenManager.saveSession(
                accessToken = token,
                refreshToken = tokenManager.getRefreshTokenSync() ?: "",
                userId = meData.id,
                role = meData.role,
                fullName = meData.fullName ?: meData.email,
                email = meData.email
            )

            when {
                !meData.isActive -> AuthDestination.ACTIVATION_PENDING
                !meData.profileCompleted || meData.institutoId.isNullOrEmpty() ->
                    AuthDestination.COMPLETE_PROFILE
                else -> resolveDestination(meData.role)
            }
        } catch (e: Exception) {
            tokenManager.clearSession()
            null
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun resolveDestination(role: String): AuthDestination {
        return if (role.equals("professor", ignoreCase = true) || role.equals("admin", ignoreCase = true)) {
            AuthDestination.PROFESSOR_HOME
        } else {
            AuthDestination.STUDENT_HOME
        }
    }

    fun getLastEmail(): String = when (val state = _authState.value) {
        is AuthState.NeedsActivation -> state.email
        is AuthState.NeedsProfileCompletion -> state.user.email
        is AuthState.Success -> state.user.email
        else -> ""
    }

    fun clearSession() {
        viewModelScope.launch {
            tokenManager.clearSession()
        }
    }
}
