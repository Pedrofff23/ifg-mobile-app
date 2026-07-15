package com.example.gymapp.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.repository.WorkoutSessionRepository
import com.example.gymapp.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

enum class WorkoutSessionState {
    Loading, Active, Resumed, Error
}

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val erpService: ErpService,
    private val tokenManager: TokenManager,
    private val workoutSessionRepository: WorkoutSessionRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow(WorkoutSessionState.Loading)
    val sessionState: StateFlow<WorkoutSessionState> = _sessionState.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    private val _currentSet = MutableStateFlow(1)
    val currentSet: StateFlow<Int> = _currentSet.asStateFlow()

    private val _isFinishing = MutableStateFlow(false)
    val isFinishing: StateFlow<Boolean> = _isFinishing.asStateFlow()

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    private val _loadHistory = MutableStateFlow<List<ExerciseProgressPoint>>(emptyList())
    val loadHistory: StateFlow<List<ExerciseProgressPoint>> = _loadHistory.asStateFlow()

    private val _currentExerciseDetails = MutableStateFlow<Exercise?>(null)
    val currentExerciseDetails: StateFlow<Exercise?> = _currentExerciseDetails.asStateFlow()

    var session: WorkoutSession? = null
        private set
    var templateExercises: List<TemplateExercise> = emptyList()
        private set
    var template: WorkoutTemplate? = null
        private set

    /** Session exercises from the local/remote repository */
    private val _sessionExercises = MutableStateFlow<List<SessionExercise>>(emptyList())
    val sessionExercises: StateFlow<List<SessionExercise>> = _sessionExercises.asStateFlow()

    fun startWorkoutSession(assignmentId: String) {
        viewModelScope.launch {
            _sessionState.value = WorkoutSessionState.Loading
            try {
                val userId = tokenManager.getUserIdSync() ?: throw Exception("Not logged in")

                // Start session via repository
                val startSession = workoutSessionRepository.startSession(assignmentId)
                session = startSession
                _sessionExercises.value = (startSession.exercises ?: emptyList()).sortedBy { it.orderIndex ?: 0 }

                // Load template details
                val currentResp = erpService.getCurrentAssignment(userId)
                val assignment = currentResp.data?.takeIf { it.id == assignmentId }
                    ?: run {
                        val assignmentsResp = erpService.getAssignmentsByAluno(userId)
                        assignmentsResp.data?.find { it.id == assignmentId }
                    }
                val templateId = assignment?.templateId ?: assignmentId
                val templateResp = erpService.getTemplate(templateId)
                template = templateResp.data
                templateExercises = (templateResp.data?.workoutDays?.flatMap { it.exercises ?: emptyList() } ?: emptyList()).sortedBy { it.orderIndex }

                resumeFromProgress()

                _sessionState.value = WorkoutSessionState.Active
            } catch (e: Exception) {
                _sessionState.value = WorkoutSessionState.Error
            }
        }
    }

    private fun resumeFromProgress() {
        if (_sessionExercises.value.isEmpty()) return

        val incompleteExIdx = _sessionExercises.value.indexOfFirst { ex ->
            ex.status != "completed"
        }

        if (incompleteExIdx >= 0) {
            _currentExerciseIndex.value = incompleteExIdx

            val ex = _sessionExercises.value[incompleteExIdx]
            val sortedSets = (ex.sets ?: emptyList()).sortedBy { it.setNumber ?: 0 }
            val incompleteSet = sortedSets.indexOfFirst { set ->
                set.isCompleted != true
            }
            _currentSet.value = if (incompleteSet >= 0) {
                sortedSets[incompleteSet].setNumber ?: 1
            } else {
                1
            }
        } else {
            _currentExerciseIndex.value = _sessionExercises.value.lastIndex.coerceAtLeast(0)
            _currentSet.value = 1
        }
    }

    fun tryResumeSession(assignmentId: String) {
        viewModelScope.launch {
            _sessionState.value = WorkoutSessionState.Loading
            try {
                val userId = tokenManager.getUserIdSync() ?: throw Exception("Not logged in")

                // Try to find if there is an interrupted local or remote session
                val sessions = workoutSessionRepository.getSessionsByAluno(userId)
                val inProgressSession = sessions.find {
                    it.assignmentId == assignmentId && it.finishedAt == null
                }

                if (inProgressSession != null) {
                    val fullSession = workoutSessionRepository.getSession(inProgressSession.id)
                    session = fullSession
                    _sessionExercises.value = (fullSession.exercises ?: emptyList()).sortedBy { it.orderIndex ?: 0 }

                    val currentResp = erpService.getCurrentAssignment(userId)
                    val assignment = currentResp.data?.takeIf { it.id == assignmentId }
                        ?: run {
                            val assignmentsResp = erpService.getAssignmentsByAluno(userId)
                            assignmentsResp.data?.find { it.id == assignmentId }
                        }
                    val templateId = assignment?.templateId ?: assignmentId
                    val templateResp = erpService.getTemplate(templateId)
                    template = templateResp.data
                    templateExercises = (templateResp.data?.workoutDays?.flatMap { it.exercises ?: emptyList() } ?: emptyList()).sortedBy { it.orderIndex }

                    resumeFromProgress()
                    _sessionState.value = WorkoutSessionState.Resumed
                } else {
                    startWorkoutSession(assignmentId)
                }
            } catch (e: Exception) {
                _sessionState.value = WorkoutSessionState.Error
            }
        }
    }

    fun completeSet(setNumber: Int, weightKg: Double?, repsCompleted: Int = 0, durationSeconds: Int? = null, distanceMeters: Double? = null) {
        viewModelScope.launch {
            try {
                val exIdx = _currentExerciseIndex.value
                val sessionEx = _sessionExercises.value.getOrNull(exIdx) ?: return@launch
                val set = sessionEx.sets?.find { it.setNumber == setNumber } ?: return@launch

                workoutSessionRepository.updateSet(
                    set.id, repsCompleted, weightKg, durationSeconds, distanceMeters, true
                )

                _sessionExercises.value = _sessionExercises.value.mapIndexed { idx, ex ->
                    if (idx == exIdx) {
                        ex.copy(sets = ex.sets?.map { s ->
                            if (s.setNumber == setNumber) s.copy(
                                isCompleted = true,
                                weightKg = weightKg ?: s.weightKg,
                                repsCompleted = repsCompleted,
                                durationSeconds = durationSeconds ?: s.durationSeconds,
                                distanceMeters = distanceMeters ?: s.distanceMeters
                            ) else s
                        })
                    } else ex
                }

                val templateEx = templateExercises.getOrNull(exIdx)
                val totalSets = templateEx?.defaultSets ?: (sessionEx.sets.size)

                if (setNumber < totalSets) {
                    _currentSet.value = setNumber + 1
                } else {
                    workoutSessionRepository.updateExerciseStatus(sessionEx.id, "completed")

                    _sessionExercises.value = _sessionExercises.value.mapIndexed { idx, ex ->
                        if (idx == exIdx) ex.copy(status = "completed") else ex
                    }

                    val nextIdx = exIdx + 1
                    if (nextIdx < _sessionExercises.value.size) {
                        _currentExerciseIndex.value = nextIdx
                        _currentSet.value = 1
                    }
                }
            } catch (e: Exception) {
                Log.e("GymApp/Error", "An error occurred", e)
            }
        }
    }

    fun markSetIncomplete(sessionExerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            try {
                val sessionEx = _sessionExercises.value.find { it.id == sessionExerciseId } ?: return@launch
                val set = sessionEx.sets?.find { it.setNumber == setNumber } ?: return@launch

                workoutSessionRepository.updateSet(
                    set.id, 0, null, null, null, false
                )
                workoutSessionRepository.updateExerciseStatus(sessionExerciseId, "in_progress")

                _sessionExercises.value = _sessionExercises.value.mapIndexed { idx, ex ->
                    if (ex.id == sessionExerciseId) {
                        ex.copy(
                            status = "in_progress",
                            sets = ex.sets?.map { s ->
                                if (s.setNumber == setNumber) s.copy(
                                    isCompleted = false,
                                    weightKg = null,
                                    repsCompleted = 0,
                                    durationSeconds = null,
                                    distanceMeters = null
                                ) else s
                            }
                        )
                    } else ex
                }

                _currentSet.value = setNumber
            } catch (e: Exception) {
                Log.e("GymApp/Error", "An error occurred", e)
            }
        }
    }

    fun nextExercise() {
        val nextIdx = _currentExerciseIndex.value + 1
        if (nextIdx < _sessionExercises.value.size) {
            selectExercise(nextIdx)
        }
    }

    fun previousExercise() {
        val prevIdx = _currentExerciseIndex.value - 1
        if (prevIdx >= 0) {
            selectExercise(prevIdx)
        }
    }

    fun selectExercise(index: Int) {
        if (index < 0 || index >= _sessionExercises.value.size) return
        _currentExerciseIndex.value = index

        val sessionEx = _sessionExercises.value.getOrNull(index) ?: return

        val sortedSets = (sessionEx.sets ?: emptyList()).sortedBy { it.setNumber ?: 0 }
        val incompleteSet = sortedSets.indexOfFirst { set -> set.isCompleted != true }
        _currentSet.value = if (incompleteSet >= 0) {
            sortedSets[incompleteSet].setNumber ?: 1
        } else {
            1
        }

        if (sessionEx.status == "not_started") {
            viewModelScope.launch {
                try {
                    workoutSessionRepository.updateExerciseStatus(sessionEx.id, "in_progress")
                    _sessionExercises.value = _sessionExercises.value.map { ex ->
                        if (ex.id == sessionEx.id) ex.copy(status = "in_progress") else ex
                    }
                } catch (e: Exception) {
                    Log.e("GymApp/Error", "An error occurred", e)
                }
            }
        }
    }

    fun showRating() {
        _showRatingDialog.value = true
    }

    fun dismissRating() {
        _showRatingDialog.value = false
    }

    fun finishWorkout(rating: Int, feedback: String) {
        viewModelScope.launch {
            _isFinishing.value = true
            try {
                val sessionId = session?.id ?: return@launch
                workoutSessionRepository.finishSession(sessionId, rating, feedback.ifBlank { null })
            } catch (e: Exception) {
                Log.e("GymApp/Error", "An error occurred", e)
            } finally {
                _isFinishing.value = false
                _showRatingDialog.value = false
            }
        }
    }

    fun getCurrentSessionExercise(): SessionExercise? {
        return _sessionExercises.value.getOrNull(_currentExerciseIndex.value)
    }

    fun getCurrentTemplateExercise(): TemplateExercise? {
        return templateExercises.getOrNull(_currentExerciseIndex.value)
    }

    fun loadExerciseHistory(exerciseId: String) {
        viewModelScope.launch {
            try {
                val points = workoutSessionRepository.getExerciseProgress(exerciseId)
                _loadHistory.value = points
            } catch (_: Exception) {
                _loadHistory.value = emptyList()
            }
        }
    }

    fun loadExerciseDetails(exerciseId: String) {
        viewModelScope.launch {
            try {
                val resp = erpService.getExercise(exerciseId)
                _currentExerciseDetails.value = resp.data
            } catch (_: Exception) {
                _currentExerciseDetails.value = null
            }
        }
    }
}
