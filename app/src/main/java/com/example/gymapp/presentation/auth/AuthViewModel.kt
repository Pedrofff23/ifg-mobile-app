package com.example.gymapp.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.AuthService
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.FCMTokenRequest
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
    object Blocked : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ResendState {
    object Idle : ResendState()
    object Loading : ResendState()
    object Success : ResendState()
    data class Error(val message: String) : ResendState()
}

enum class AuthDestination {
    LOGIN,
    REGISTER,
    COMPLETE_PROFILE,
    ACTIVATION_PENDING,
    BLOCKED,
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

    private val _resendState = MutableStateFlow<ResendState>(ResendState.Idle)
    val resendState: StateFlow<ResendState> = _resendState.asStateFlow()

    var lastEnteredEmail: String = ""
        private set

    fun resetResendState() {
        _resendState.value = ResendState.Idle
    }

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
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading institutos: ${e.message}")
            } finally {
                _isInstitutosLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        lastEnteredEmail = email
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Call Supabase signup
                val response = authService.register(RegisterRequest(email, password, fullName))

                // If autoconfirm is active on the backend, we receive an access_token immediately
                val token = response.token
                if (!token.isNullOrEmpty()) {
                    val refreshToken = response.refreshToken
                    val supabaseUser = response.user
                    val userId = supabaseUser?.id ?: ""
                    val userEmail = supabaseUser?.email ?: email
                    val userFullName = supabaseUser?.userMetadata?.fullName ?: fullName
                    val role = supabaseUser?.userMetadata?.role ?: "aluno"

                    tokenManager.saveSession(
                        accessToken = token,
                        refreshToken = refreshToken,
                        userId = userId,
                        role = role,
                        fullName = userFullName.ifBlank { email },
                        email = userEmail
                    )

                    // Retrieve and store FCM Token for push notifications
                    retrieveAndStoreFCMToken()

                    // Check if profile is completed by calling /auth/me
                    try {
                        val meResponse = authService.getMe()
                        val meData = meResponse.data

                        if (meData != null) {
                            val profileCompleted = meData.profileCompleted
                            val institutoId = meData.institutoId
                            val isActive = meData.isActive
                            val dbRole = meData.role

                            // Sync the database role to tokenManager (overrides stale Supabase metadata role)
                            if (dbRole != role) {
                                tokenManager.saveUserRole(dbRole)
                            }
                            
                            // Save instituto info
                            if (!institutoId.isNullOrEmpty()) {
                                tokenManager.saveInstitutoId(institutoId)
                            }
                            if (!meData.instituto.isNullOrEmpty()) {
                                tokenManager.saveInstitutoName(meData.instituto!!)
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

                            val isProfileIncomplete = !profileCompleted || (institutoId.isNullOrEmpty() && meData.instituto.isNullOrEmpty())

                            if (!isActive) {
                                _authState.value = AuthState.NeedsActivation(meData.email)
                            } else if (isProfileIncomplete) {
                                _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                            } else {
                                _authState.value = AuthState.Success(domainUser)
                            }
                        } else {
                            val domainUser = User(id = userId, email = userEmail, fullName = userFullName, role = role)
                            _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                        }
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Error fetching me profile after autoconfirm register: ${e.message}", e)
                        val domainUser = User(id = userId, email = userEmail, fullName = userFullName, role = role)
                        _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                    }
                } else {
                    // Email confirmation is required - navigate to activation pending page
                    _authState.value = AuthState.NeedsActivation(email)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    ErrorUtils.parseErrorMessage(e, "Falha ao criar conta")
                )
            }
        }
    }

    fun login(email: String, password: String) {
        lastEnteredEmail = email
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

                // Retrieve and store FCM Token for push notifications
                retrieveAndStoreFCMToken()

                // Check if profile is completed by calling /auth/me
                try {
                    val meResponse = authService.getMe()
                    val meData = meResponse.data

                    if (meData != null) {
                        val profileCompleted = meData.profileCompleted
                        val institutoId = meData.institutoId
                        val isActive = meData.isActive
                        val dbRole = meData.role

                        Log.d("AuthViewModel", "Login Me profile: role=$dbRole, active=$isActive, instId=$institutoId, instName=${meData.instituto}, completed=$profileCompleted")

                        // Sync the database role to tokenManager (overrides stale Supabase metadata role)
                        if (dbRole != role) {
                            tokenManager.saveUserRole(dbRole)
                        }
                        
                        // Save instituto info
                        if (!institutoId.isNullOrEmpty()) {
                            tokenManager.saveInstitutoId(institutoId)
                        }
                        if (!meData.instituto.isNullOrEmpty()) {
                            tokenManager.saveInstitutoName(meData.instituto!!)
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

                        val isProfileIncomplete = !profileCompleted || (institutoId.isNullOrEmpty() && meData.instituto.isNullOrEmpty())

                        if (!isActive) {
                            Log.d("AuthViewModel", "User not active, needs activation")
                            _authState.value = AuthState.NeedsActivation(meData.email)
                        } else if (isProfileIncomplete) {
                            Log.d("AuthViewModel", "Profile incomplete (completed=$profileCompleted). Redirecting to Complete Profile.")
                            _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                        } else {
                            Log.d("AuthViewModel", "Profile complete. Proceeding to Home.")
                            _authState.value = AuthState.Success(domainUser)
                        }
                    } else {
                        // No profile data yet - check role from metadata
                        val domainUser = User(id = userId, email = userEmail, fullName = fullName, role = role)
                        Log.d("AuthViewModel", "No profile data, redirecting to Complete Profile")
                        _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                    }
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error fetching me profile: ${e.message}", e)
                    val parsedError = ErrorUtils.parseErrorMessage(e)
                    if (parsedError == "ACCOUNT_BLOCKED") {
                        tokenManager.clearSession()
                        _authState.value = AuthState.Blocked
                        return@launch
                    }
                    // If /auth/me failed, we redirect to complete profile to be safe
                    val domainUser = User(id = userId, email = userEmail, fullName = fullName, role = role)
                    _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                }
            } catch (e: Exception) {
                // Login failed - check if email not confirmed
                val parsedError = ErrorUtils.parseErrorMessage(e)
                
                when (parsedError) {
                    "EMAIL_NOT_CONFIRMED" -> {
                        _authState.value = AuthState.NeedsActivation(email)
                    }
                    "ACCOUNT_BLOCKED" -> {
                        _authState.value = AuthState.Blocked
                    }
                    else -> {
                        _authState.value = AuthState.Error(parsedError)
                    }
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

                if (meData != null && (meData.profileCompleted || !meData.institutoId.isNullOrEmpty() || !meData.instituto.isNullOrEmpty())) {
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
            _resendState.value = ResendState.Loading
            try {
                authService.resendActivation(ResendActivationRequest(email))
                _resendState.value = ResendState.Success
            } catch (e: Exception) {
                _resendState.value = ResendState.Error(
                    ErrorUtils.parseErrorMessage(e, "Falha ao reenviar e-mail")
                )
            }
        }
    }

    private val _forgotPasswordState = MutableStateFlow<String?>(null)
    val forgotPasswordState: StateFlow<String?> = _forgotPasswordState.asStateFlow()

    private val _forgotPasswordError = MutableStateFlow<String?>(null)
    val forgotPasswordError: StateFlow<String?> = _forgotPasswordError.asStateFlow()

    private val _isForgotPasswordLoading = MutableStateFlow(false)
    val isForgotPasswordLoading: StateFlow<Boolean> = _isForgotPasswordLoading.asStateFlow()

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _isForgotPasswordLoading.value = true
            _forgotPasswordError.value = null
            _forgotPasswordState.value = null
            try {
                authService.forgotPassword(ForgotPasswordRequest(email))
                _forgotPasswordState.value = "Email de recuperação enviado! Verifique sua caixa de entrada."
            } catch (e: Exception) {
                _forgotPasswordError.value = ErrorUtils.parseErrorMessage(e, "Erro ao solicitar recuperação")
            } finally {
                _isForgotPasswordLoading.value = false
            }
        }
    }

    fun clearForgotPasswordStatus() {
        _forgotPasswordState.value = null
        _forgotPasswordError.value = null
    }

    /**
     * Store FCM token for push notifications.
     * Called after Firebase Messaging integration is set up.
     */
    fun storeFCMToken(fcmToken: String, deviceInfo: String? = null) {
        viewModelScope.launch {
            try {
                erpService.storeFCMToken(FCMTokenRequest(fcmToken, deviceInfo))
            } catch (_: Exception) {
                // Silently fail - FCM is optional
            }
        }
    }

    fun retrieveAndStoreFCMToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrEmpty()) {
                        storeFCMToken(token, android.os.Build.MODEL)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Failed to retrieve FCM token", e)
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

            Log.d("AuthViewModel", "CheckAuth Me profile: role=${meData.role}, active=${meData.isActive}, instId=${meData.institutoId}, instName=${meData.instituto}, completed=${meData.profileCompleted}")

            val isStaff = meData.role.equals("professor", ignoreCase = true) || meData.role.equals("admin", ignoreCase = true)
            val isProfileIncomplete = !meData.profileCompleted || (meData.institutoId.isNullOrEmpty() && meData.instituto.isNullOrEmpty())

            when {
                !meData.isActive -> {
                    _authState.value = AuthState.NeedsActivation(meData.email)
                    AuthDestination.ACTIVATION_PENDING
                }
                isProfileIncomplete -> {
                    _authState.value = AuthState.NeedsProfileCompletion(domainUser)
                    AuthDestination.COMPLETE_PROFILE
                }
                else -> {
                    _authState.value = AuthState.Success(domainUser)
                    resolveDestination(meData.role)
                }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "CheckAuth error: ${e.message}")
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

    fun getLastEmail(): String = lastEnteredEmail.ifBlank {
        when (val state = _authState.value) {
            is AuthState.NeedsActivation -> state.email
            is AuthState.NeedsProfileCompletion -> state.user.email
            is AuthState.Success -> state.user.email
            else -> ""
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            tokenManager.clearSession()
        }
    }
}
