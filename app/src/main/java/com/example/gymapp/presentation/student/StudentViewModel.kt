package com.example.gymapp.presentation.student

import androidx.lifecycle.ViewModel
import com.example.gymapp.utils.ErrorUtils
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.ProfileService
import com.example.gymapp.data.remote.UserService
import com.example.gymapp.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
	private val erpService: ErpService,
	private val profileService: ProfileService,
	private val userService: UserService,
	private val tokenManager: TokenManager
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<WorkoutAssignment>>(emptyList())
    val assignments: StateFlow<List<WorkoutAssignment>> = _assignments.asStateFlow()

    private val _currentAssignment = MutableStateFlow<WorkoutAssignment?>(null)
    val currentAssignment: StateFlow<WorkoutAssignment?> = _currentAssignment.asStateFlow()

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

    private val _stats = MutableStateFlow<AlunoStats?>(null)
    val stats: StateFlow<AlunoStats?> = _stats.asStateFlow()

    // ---------- Exercise Progress State ----------
    private val _exerciseProgress = MutableStateFlow<Map<String, List<ExerciseProgressPoint>>>(emptyMap())
    val exerciseProgress: StateFlow<Map<String, List<ExerciseProgressPoint>>> = _exerciseProgress.asStateFlow()

    private val _exerciseProgressLoading = MutableStateFlow(false)
    val exerciseProgressLoading: StateFlow<Boolean> = _exerciseProgressLoading.asStateFlow()

    // ---------- Custom Exercise Metric State ----------
    private val _exerciseCustomMetrics = MutableStateFlow<Map<String, ExerciseCustomMetric>>(emptyMap())
    val exerciseCustomMetrics: StateFlow<Map<String, ExerciseCustomMetric>> = _exerciseCustomMetrics.asStateFlow()

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
    // Load all assignments (non-paginated endpoint)
    val response = erpService.getAssignmentsByAluno(userId)
    _assignments.value = (response.data ?: emptyList()).sortedByDescending { it.startsAt }
    // Load current assignment in the same coroutine so isLoading covers both
    try {
    val currentResp = erpService.getCurrentAssignment(userId)
    _currentAssignment.value = currentResp.data
    } catch (e: HttpException) {
    if (e.code() == 404) {
    _currentAssignment.value = null
    } else {
    throw e // re-throw non-404 HTTP errors
    }
    }
    } catch (e: Exception) {
    _error.value = ErrorUtils.parseErrorMessage(e)
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
                _error.value = ErrorUtils.parseErrorMessage(e)
            }
        }
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            try {
                val response = erpService.getAnnouncements()
                _announcements.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            }
        }
    }

    fun loadExercises(search: String? = null, muscleGroup: String? = null) {
        viewModelScope.launch {
            try {
                val mappedMuscle = muscleGroup?.let {
                    when (it.lowercase()) {
                        "peito" -> "peito"
                        "costas" -> "costas"
                        "ombros" -> "ombros"
                        "bíceps", "biceps", "tríceps", "triceps", "braços", "bracos" -> "bracos"
                        "pernas", "glúteos", "gluteos" -> "pernas"
                        "core", "abdômen", "abdomen" -> "abdomen"
                        "cardio" -> "cardio"
                        else -> it
                    }
                }
                val response = erpService.getExercises(search = search, muscleGroup = mappedMuscle)
                _exercises.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                if (e is HttpException && e.code() == 404) {
                    _profile.value = null
                    _measurements.value = emptyList()
                } else {
                    _error.value = ErrorUtils.parseErrorMessage(e)
                }
            }
        }
    }

    fun loadStudentData() {
    loadAssignments()
    loadSessions()
    loadAnnouncements()
    loadStats()
    }

    fun loadStats() {
    viewModelScope.launch {
    try {
    val userId = tokenManager.getUserIdSync() ?: return@launch
    val response = erpService.getAlunoStats(userId)
    _stats.value = response.data
    } catch (e: Exception) {
    // Stats are supplementary — don't override error state
    }
    }
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
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                // Get current date in ISO format (yyyy-MM-dd)
                val now = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    java.time.OffsetDateTime.now().format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } else {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    sdf.format(java.util.Date())
                }

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
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun updateUserName(fullName: String, instituto: String?) {
    	viewModelScope.launch {
    		_isUpdating.value = true
    		_error.value = null
    		_updateSuccess.value = null
    		try {
    			val userId = tokenManager.getUserIdSync() ?: return@launch
    			userService.updateProfile(userId, UpdateUserRequest(fullName = fullName, instituto = instituto))
    			_userName.value = fullName
    				tokenManager.saveUserName(fullName)
    			_updateSuccess.value = "Nome atualizado com sucesso!"
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isUpdating.value = false
    		}
    	}
    }

    fun clearUpdateStatus() {
    _updateSuccess.value = null
    _error.value = null
    }

    // ==================== EXERCISE PROGRESS ====================

    // ==================== EXERCISE CUSTOM METRICS ====================

    /** Load custom metric preferences for given exercise IDs */
    fun loadExerciseCustomMetrics(exerciseIds: List<String>) {
        viewModelScope.launch {
            val newMap = mutableMapOf<String, ExerciseCustomMetric>()
            for (id in exerciseIds) {
                try {
                    val resp = erpService.getExerciseMetric(id)
                    resp.data?.let { newMap[id] = it }
                } catch (e: HttpException) {
                    if (e.code() != 404) {
                        // ignore other errors
                    }
                } catch (_: Exception) {
                    // ignore
                }
            }
            if (newMap.isNotEmpty()) {
                _exerciseCustomMetrics.value = _exerciseCustomMetrics.value + newMap
            }
        }
    }

    /** Set custom metric for an exercise and refresh state */
    fun setExerciseMetric(exerciseId: String, metricType: String) {
        viewModelScope.launch {
            try {
                erpService.setExerciseMetric(SetExerciseMetricRequest(exerciseId = exerciseId, metricType = metricType))
                loadExerciseCustomMetrics(listOf(exerciseId))
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            }
        }
    }

    /** Delete custom metric preference for an exercise */
    fun deleteExerciseMetric(exerciseId: String) {
        viewModelScope.launch {
            try {
                erpService.deleteExerciseMetric(exerciseId)
                val updated = _exerciseCustomMetrics.value.toMutableMap()
                updated.remove(exerciseId)
                _exerciseCustomMetrics.value = updated
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            }
        }
    }

    // ==================== EXERCISE PROGRESS ====================

    /**
     * Loads exercise progress data for the given exercise IDs.
     * Results are stored in [exerciseProgress] map keyed by exercise ID.
     */
    fun loadExerciseProgress(exerciseIds: List<String>) {
        viewModelScope.launch {
            _exerciseProgressLoading.value = true
            try {
                val newMap = mutableMapOf<String, List<ExerciseProgressPoint>>()
                for (id in exerciseIds) {
                    if (!_exerciseProgress.value.containsKey(id)) {
                        try {
                            val response = erpService.getExerciseProgress(id)
                            newMap[id] = response.data ?: emptyList()
                        } catch (_: Exception) {
                            newMap[id] = emptyList()
                        }
                    }
                }
                if (newMap.isNotEmpty()) {
                    _exerciseProgress.value = _exerciseProgress.value + newMap
                }
                // Load custom metrics after progress data
                loadExerciseCustomMetrics(exerciseIds)
            } finally {
                _exerciseProgressLoading.value = false
            }
        }
    }
    }
