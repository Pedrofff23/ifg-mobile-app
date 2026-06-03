package com.example.gymapp.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WorkoutSessionState {
 Loading, Active, Resumed, Error
}

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
 private val erpService: ErpService,
 private val tokenManager: TokenManager
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

 var session: WorkoutSession? = null
 private set
 var templateExercises: List<TemplateExercise> = emptyList()
 private set
 var template: WorkoutTemplate? = null
 private set

 /** Session exercises from the backend (with real IDs for updateSet/updateExerciseStatus) */
 var sessionExercises: List<SessionExercise> = emptyList()
 private set

 fun startWorkoutSession(assignmentId: String) {
 	viewModelScope.launch {
 		_sessionState.value = WorkoutSessionState.Loading
 		try {
 			val userId = tokenManager.getUserIdSync() ?: throw Exception("Not logged in")

 			// Start session — backend creates SessionExercises + SessionSets
 			// If an interrupted session exists for this assignment, backend returns it
 			val startResp = erpService.startSession(StartSessionRequest(assignmentId))
 			session = startResp.data
 			sessionExercises = (startResp.data?.exercises ?: emptyList()).sortedBy { it.orderIndex ?: 0 }

 			// Load template — use assignment directly from getCurrentAssignment
 			val currentResp = erpService.getCurrentAssignment(userId)
 			val assignment = currentResp.data?.takeIf { it.id == assignmentId }
 				// Fallback: if current assignment doesn't match, fetch all
 				?: run {
 					val assignmentsResp = erpService.getAssignmentsByAluno(userId)
 					assignmentsResp.data?.find { it.id == assignmentId }
 				}
 			val templateId = assignment?.templateId ?: assignmentId
 			val templateResp = erpService.getTemplate(templateId)
 			template = templateResp.data
 			templateExercises = (templateResp.data?.exercises ?: emptyList()).sortedBy { it.orderIndex }

 			// Auto-resume: find the first incomplete exercise and set
 			resumeFromProgress()

 			_sessionState.value = WorkoutSessionState.Active
 		} catch (_: Exception) {
 			_sessionState.value = WorkoutSessionState.Error
 		}
 	}
 }

 /**
 * Resume an interrupted session: find the first exercise that is not
 * completed, and within that exercise find the first incomplete set.
 * If all exercises are completed, stay on the last one.
 */
 private fun resumeFromProgress() {
 if (sessionExercises.isEmpty()) return

 // Find first incomplete exercise
 val incompleteExIdx = sessionExercises.indexOfFirst { ex ->
 	ex.status != "completed"
 }

 if (incompleteExIdx >= 0) {
 _currentExerciseIndex.value = incompleteExIdx

 // Find first incomplete set within that exercise
 val ex = sessionExercises[incompleteExIdx]
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
 // All exercises done — position at last exercise
 _currentExerciseIndex.value = sessionExercises.lastIndex.coerceAtLeast(0)
 _currentSet.value = 1
 }
 }

 /**
 * Attempt to resume an existing in-progress session for the given assignment.
 * Called when the student opens a workout that was previously interrupted.
 * Returns the session ID if a resumable session was found, null otherwise.
 */
 fun tryResumeSession(assignmentId: String) {
 	viewModelScope.launch {
 		_sessionState.value = WorkoutSessionState.Loading
 		try {
 			val userId = tokenManager.getUserIdSync() ?: throw Exception("Not logged in")

 			// Check for existing sessions for this aluno
 			val sessionsResp = erpService.getSessionsByAluno(userId)
 			val inProgressSession = sessionsResp.data?.find {
 				it.assignmentId == assignmentId && it.finishedAt == null
 			}

 			if (inProgressSession != null) {
 				// Resume: fetch full session with exercises/sets
 				val sessionResp = erpService.getSession(inProgressSession.id)
 				session = sessionResp.data
 				sessionExercises = (sessionResp.data?.exercises ?: emptyList()).sortedBy { it.orderIndex ?: 0 }

 				// Load template — use getCurrentAssignment first
 				val currentResp = erpService.getCurrentAssignment(userId)
 				val assignment = currentResp.data?.takeIf { it.id == assignmentId }
 					?: run {
 						val assignmentsResp = erpService.getAssignmentsByAluno(userId)
 						assignmentsResp.data?.find { it.id == assignmentId }
 					}
 				val templateId = assignment?.templateId ?: assignmentId
 				val templateResp = erpService.getTemplate(templateId)
 				template = templateResp.data
 				templateExercises = (templateResp.data?.exercises ?: emptyList()).sortedBy { it.orderIndex }

 				resumeFromProgress()
 				_sessionState.value = WorkoutSessionState.Resumed
 			} else {
 				// No session to resume — start a fresh one
 				startWorkoutSession(assignmentId)
 			}
 		} catch (_: Exception) {
 			_sessionState.value = WorkoutSessionState.Error
 		}
 	}
 }

 /**
  * Complete a set: persist weight/reps or cardio data to backend, then advance to next set or exercise.
  */
 fun completeSet(setNumber: Int, weightKg: Double?, repsCompleted: Int = 0, durationSeconds: Int? = null, distanceMeters: Double? = null) {
 viewModelScope.launch {
 try {
 val exIdx = _currentExerciseIndex.value
 val sessionEx = sessionExercises.getOrNull(exIdx) ?: return@launch
 val set = sessionEx.sets?.find { it.setNumber == setNumber } ?: return@launch

 // Persist set data to backend
 erpService.updateSet(set.id, UpdateSetRequest(
 repsCompleted = repsCompleted,
 weightKg = weightKg,
 durationSeconds = durationSeconds,
 distanceMeters = distanceMeters,
 notes = null,
 isCompleted = true
 ))

 // Advance: if more sets in this exercise, go to next set; else mark exercise done and advance
 val templateEx = templateExercises.getOrNull(exIdx)
 val totalSets = templateEx?.defaultSets ?: (sessionEx.sets.size)

 if (setNumber < totalSets) {
 _currentSet.value = setNumber + 1
 } else {
 // All sets done for this exercise — mark exercise as completed on backend
 erpService.updateExerciseStatus(sessionEx.id, UpdateExerciseStatusRequest(status = "completed"))

 // Move to next exercise
 val nextIdx = exIdx + 1
 if (nextIdx < sessionExercises.size) {
 _currentExerciseIndex.value = nextIdx
 _currentSet.value = 1
 }
 // If this was the last exercise, user should tap "Finalizar"
 }
 } catch (_: Exception) { }
 }
 }

 fun nextExercise() {
 	val nextIdx = _currentExerciseIndex.value + 1
 	if (nextIdx < sessionExercises.size) {
 		selectExercise(nextIdx)
 	}
 }

 fun previousExercise() {
 	val prevIdx = _currentExerciseIndex.value - 1
 	if (prevIdx >= 0) {
 		selectExercise(prevIdx)
 	}
 }

 /**
  * Select any exercise freely — the student can choose which exercise
  * to do regardless of the workout order.
  * Marks the selected exercise as in_progress if it hasn't been started yet.
  */
 fun selectExercise(index: Int) {
 	if (index < 0 || index >= sessionExercises.size) return
 	_currentExerciseIndex.value = index

 	val sessionEx = sessionExercises.getOrNull(index) ?: return

 	// Find first incomplete set within that exercise
 	val sortedSets = (sessionEx.sets ?: emptyList()).sortedBy { it.setNumber ?: 0 }
 	val incompleteSet = sortedSets.indexOfFirst { set -> set.isCompleted != true }
 	_currentSet.value = if (incompleteSet >= 0) {
 		sortedSets[incompleteSet].setNumber ?: 1
 	} else {
 		1
 	}

 	// Mark exercise as in_progress if not started yet
 	if (sessionEx.status == "not_started") {
 		viewModelScope.launch {
 			try {
 				erpService.updateExerciseStatus(sessionEx.id, UpdateExerciseStatusRequest(status = "in_progress"))
 			} catch (_: Exception) { }
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
 erpService.finishSession(sessionId, FinishSessionRequest(rating, feedback.ifBlank { null }))
 } catch (_: Exception) { } finally {
 _isFinishing.value = false
 _showRatingDialog.value = false
 }
 }
 }

 /** Get the current session exercise (with backend ID) */
 fun getCurrentSessionExercise(): SessionExercise? {
 return sessionExercises.getOrNull(_currentExerciseIndex.value)
 }

 /** Get the current template exercise (with metadata like defaultSets, defaultReps) */
 fun getCurrentTemplateExercise(): TemplateExercise? {
 return templateExercises.getOrNull(_currentExerciseIndex.value)
 }

 /** Load exercise load history from the backend */
 fun loadExerciseHistory(exerciseId: String) {
 viewModelScope.launch {
 try {
 val resp = erpService.getExerciseProgress(exerciseId)
 _loadHistory.value = resp.data ?: emptyList()
 } catch (_: Exception) {
 _loadHistory.value = emptyList()
 }
 }
 }
}
