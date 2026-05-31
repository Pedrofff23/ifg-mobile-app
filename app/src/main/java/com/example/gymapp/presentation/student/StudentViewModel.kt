package com.example.gymapp.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.ProfileService
import com.example.gymapp.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val erpService: ErpService,
    private val profileService: ProfileService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<WorkoutAssignment>>(emptyList())
    val assignments: StateFlow<List<WorkoutAssignment>> = _assignments.asStateFlow()

    private val _sessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val sessions: StateFlow<List<WorkoutSession>> = _sessions.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _profile = MutableStateFlow<AlunoProfile?>(null)
    val profile: StateFlow<AlunoProfile?> = _profile.asStateFlow()

    private val _measurements = MutableStateFlow<List<BodyMeasurement>>(emptyList())
    val measurements: StateFlow<List<BodyMeasurement>> = _measurements.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateSuccess = MutableStateFlow<String?>(null)
    val updateSuccess: StateFlow<String?> = _updateSuccess.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
    viewModelScope.launch {
    _userName.value = tokenManager.getUserNameSync()
    _userEmail.value = tokenManager.getUserEmailSync()
    }
    }

    fun loadAssignments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = tokenManager.getUserIdSync() ?: return@launch
                val response = erpService.getAssignmentsByAluno(userId)
                _assignments.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar treinos"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            try {
                val userId = tokenManager.getUserIdSync() ?: return@launch
                val response = erpService.getSessionsByAluno(userId)
                _sessions.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar sessões"
            }
        }
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            try {
                val response = erpService.getAnnouncements()
                _announcements.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar avisos"
            }
        }
    }

    fun loadExercises(search: String? = null, muscleGroup: String? = null) {
        viewModelScope.launch {
            try {
                val response = erpService.getExercises(search = search, muscleGroup = muscleGroup)
                _exercises.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar exercícios"
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val userId = tokenManager.getUserIdSync() ?: return@launch
                val profileResp = profileService.getProfile(userId)
                _profile.value = profileResp.data
                val measResp = profileService.getMeasurements(userId)
                _measurements.value = measResp.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar perfil"
            }
        }
    }

    fun loadStudentData() {
        loadAssignments()
        loadSessions()
        loadAnnouncements()
    }

    fun updateProfile(heightCm: Double, currentWeightKg: Double, injuryHistory: String?) {
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null
            _updateSuccess.value = null
            try {
                profileService.upsertProfile(
                    UpsertProfileRequest(
                        currentWeightKg = currentWeightKg,
                        heightCm = heightCm,
                        injuryHistory = injuryHistory
                    )
                )
                loadProfile()
                _updateSuccess.value = "Perfil atualizado com sucesso!"
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao atualizar perfil"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun addMeasurement(weightKg: Double, notes: String?) {
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null
            _updateSuccess.value = null
            try {
                val now = java.time.LocalDate.now().toString()
                profileService.addMeasurement(
                    AddMeasurementRequest(
                        weightKg = weightKg,
                        measuredAt = now,
                        notes = notes
                    )
                )
                loadProfile()
                _updateSuccess.value = "Medição registrada com sucesso!"
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao registrar medição"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearUpdateStatus() {
        _updateSuccess.value = null
        _error.value = null
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
        }
    }
}
