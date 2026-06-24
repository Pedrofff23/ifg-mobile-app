package com.example.gymapp.data.repository

import com.example.gymapp.data.local.LocalSessionExercise
import com.example.gymapp.data.local.LocalSessionSet
import com.example.gymapp.data.local.LocalWorkoutSession
import com.example.gymapp.data.local.PendingSyncAction
import com.example.gymapp.data.local.WorkoutSessionDao
import com.example.gymapp.data.local.PendingSyncDao
import com.example.gymapp.data.local.LocalExerciseProgressPoint
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.domain.model.*
import com.example.gymapp.utils.NetworkMonitor
import com.example.gymapp.utils.DateUtils
import com.google.gson.Gson
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface WorkoutSessionRepository {
    suspend fun startSession(assignmentId: String): WorkoutSession
    suspend fun getSession(sessionId: String): WorkoutSession
    suspend fun getSessionsByAluno(alunoId: String): List<WorkoutSession>
    suspend fun updateSet(setId: String, reps: Int, weight: Double?, duration: Int?, distance: Double?, isCompleted: Boolean): SessionSet
    suspend fun updateExerciseStatus(exerciseId: String, status: String)
    suspend fun finishSession(sessionId: String, rating: Int?, feedback: String?): WorkoutSession
    suspend fun syncPendingActions()
    suspend fun getExerciseProgress(exerciseId: String): List<ExerciseProgressPoint>
}

@Singleton
class WorkoutSessionRepositoryImpl @Inject constructor(
    private val erpService: ErpService,
    private val workoutSessionDao: WorkoutSessionDao,
    private val pendingSyncDao: PendingSyncDao,
    private val networkMonitor: NetworkMonitor
) : WorkoutSessionRepository {

    private val gson = Gson()

    override suspend fun startSession(assignmentId: String): WorkoutSession {
        val isOnline = networkMonitor.isCurrentlyOnline()
        val customSessionId = UUID.randomUUID().toString()

        if (isOnline) {
            try {
                val response = erpService.startSession(StartSessionRequest(assignmentId, customSessionId))
                val session = response.data ?: throw Exception("Failed to start session remotely")
                saveSessionLocally(session)
                return session
            } catch (e: Exception) {
                // Fail fallback to offline creation
            }
        }

        // Offline mode creation
        val newSession = WorkoutSession(
            id = customSessionId,
            alunoId = "",
            assignmentId = assignmentId,
            templateWorkoutId = null,
            workoutName = "Treino Offline",
            sessionNumber = 1,
            startedAt = DateUtils.getNowIso(),
            finishedAt = null,
            rating = null,
            feedbackText = null,
            createdAt = DateUtils.getNowIso(),
            updatedAt = DateUtils.getNowIso(),
            exercises = emptyList()
        )

        workoutSessionDao.insertSession(newSession.toLocal())
        
        pendingSyncDao.insertAction(
            PendingSyncAction(
                id = 0,
                actionType = "START_SESSION",
                timestamp = System.currentTimeMillis(),
                sessionId = customSessionId,
                entityId = customSessionId,
                payloadJson = gson.toJson(StartSessionRequest(assignmentId, customSessionId))
            )
        )

        return newSession
    }

    override suspend fun getSession(sessionId: String): WorkoutSession {
        val isOnline = networkMonitor.isCurrentlyOnline()
        if (isOnline) {
            try {
                val response = erpService.getSession(sessionId)
                val session = response.data ?: throw Exception("Session not found")
                saveSessionLocally(session)
                return session
            } catch (e: Exception) {
                // Fallback to local
            }
        }

        val localSession = workoutSessionDao.getSessionWithExercisesAndSetsSync(sessionId) ?: throw Exception("Session not found locally")
        val exercises = workoutSessionDao.getExercisesForSessionSync(sessionId).map { localEx ->
            val sets = workoutSessionDao.getSetsForExerciseSync(localEx.id).map { localSet ->
                SessionSet(
                    id = localSet.id,
                    sessionExerciseId = localSet.sessionExerciseId,
                    setNumber = localSet.setNumber,
                    repsCompleted = localSet.repsCompleted,
                    weightKg = localSet.weightKg,
                    durationSeconds = localSet.durationSeconds,
                    distanceMeters = localSet.distanceMeters,
                    notes = localSet.notes,
                    isCompleted = localSet.isCompleted,
                    createdAt = localSet.createdAt,
                    updatedAt = localSet.updatedAt
                )
            }
            SessionExercise(
                id = localEx.id,
                sessionId = localEx.sessionId,
                exerciseId = localEx.exerciseId,
                exerciseName = localEx.exerciseName,
                muscleGroup = localEx.muscleGroup,
                orderIndex = localEx.orderIndex,
                status = localEx.status,
                createdAt = localEx.createdAt,
                updatedAt = localEx.updatedAt,
                sets = sets
            )
        }

        return WorkoutSession(
            id = localSession.id,
            alunoId = localSession.alunoId,
            assignmentId = localSession.assignmentId,
            templateWorkoutId = localSession.templateWorkoutId,
            workoutName = localSession.workoutName,
            sessionNumber = localSession.sessionNumber,
            startedAt = localSession.startedAt,
            finishedAt = localSession.finishedAt,
            rating = localSession.rating,
            feedbackText = localSession.feedbackText,
            createdAt = localSession.createdAt,
            updatedAt = localSession.updatedAt,
            exercises = exercises
        )
    }

    override suspend fun getSessionsByAluno(alunoId: String): List<WorkoutSession> {
        val isOnline = networkMonitor.isCurrentlyOnline()
        if (isOnline) {
            try {
                val response = erpService.getSessionsByAluno(alunoId)
                val sessions = response.data ?: emptyList()
                sessions.forEach { saveSessionLocally(it) }
                return sessions
            } catch (e: Exception) {
                // Fallback
            }
        }

        return workoutSessionDao.getSessionsByAlunoSync(alunoId).map { localSession ->
            WorkoutSession(
                id = localSession.id,
                alunoId = localSession.alunoId,
                assignmentId = localSession.assignmentId,
                templateWorkoutId = localSession.templateWorkoutId,
                workoutName = localSession.workoutName,
                sessionNumber = localSession.sessionNumber,
                startedAt = localSession.startedAt,
                finishedAt = localSession.finishedAt,
                rating = localSession.rating,
                feedbackText = localSession.feedbackText,
                createdAt = localSession.createdAt,
                updatedAt = localSession.updatedAt,
                exercises = emptyList()
            )
        }
    }

    override suspend fun updateSet(
        setId: String,
        reps: Int,
        weight: Double?,
        duration: Int?,
        distance: Double?,
        isCompleted: Boolean
    ): SessionSet {
        val isOnline = networkMonitor.isCurrentlyOnline()
        val request = UpdateSetRequest(
            repsCompleted = reps,
            weightKg = weight,
            durationSeconds = duration,
            distanceMeters = distance,
            notes = null,
            isCompleted = isCompleted
        )

        val sessionId = "" 

        workoutSessionDao.updateSetSync(setId, reps, weight, duration, distance, isCompleted, DateUtils.getNowIso())

        if (isOnline) {
            try {
                val response = erpService.updateSet(setId, request)
                return response.data ?: throw Exception("Remote update failed")
            } catch (e: Exception) {
                // Fail fallback
            }
        }

        pendingSyncDao.insertAction(
            PendingSyncAction(
                id = 0,
                actionType = "UPDATE_SET",
                timestamp = System.currentTimeMillis(),
                sessionId = sessionId,
                entityId = setId,
                payloadJson = gson.toJson(request)
            )
        )

        return SessionSet(
            id = setId,
            sessionExerciseId = "",
            setNumber = 1,
            repsCompleted = reps,
            weightKg = weight,
            durationSeconds = duration,
            distanceMeters = distance,
            notes = null,
            isCompleted = isCompleted,
            createdAt = null,
            updatedAt = DateUtils.getNowIso()
        )
    }

    override suspend fun updateExerciseStatus(exerciseId: String, status: String) {
        val isOnline = networkMonitor.isCurrentlyOnline()
        val request = UpdateExerciseStatusRequest(status)

        workoutSessionDao.updateExerciseStatusSync(exerciseId, status, DateUtils.getNowIso())

        if (isOnline) {
            try {
                erpService.updateExerciseStatus(exerciseId, request)
                return
            } catch (e: Exception) {
                // Fail fallback
            }
        }

        pendingSyncDao.insertAction(
            PendingSyncAction(
                id = 0,
                actionType = "UPDATE_EXERCISE_STATUS",
                timestamp = System.currentTimeMillis(),
                sessionId = "",
                entityId = exerciseId,
                payloadJson = gson.toJson(request)
            )
        )
    }

    override suspend fun finishSession(sessionId: String, rating: Int?, feedback: String?): WorkoutSession {
        val isOnline = networkMonitor.isCurrentlyOnline()
        val request = FinishSessionRequest(rating, feedback)

        workoutSessionDao.finishSessionSync(sessionId, DateUtils.getNowIso(), rating, feedback, DateUtils.getNowIso())

        if (isOnline) {
            try {
                val response = erpService.finishSession(sessionId, request)
                val session = response.data ?: throw Exception("Remote finish failed")
                saveSessionLocally(session)
                return session
            } catch (e: Exception) {
                // Fail fallback
            }
        }

        pendingSyncDao.insertAction(
            PendingSyncAction(
                id = 0,
                actionType = "FINISH_SESSION",
                timestamp = System.currentTimeMillis(),
                sessionId = sessionId,
                entityId = sessionId,
                payloadJson = gson.toJson(request)
            )
        )

        return getSession(sessionId)
    }

    override suspend fun syncPendingActions() {
        val isOnline = networkMonitor.isCurrentlyOnline()
        if (!isOnline) return

        val pendingActions = pendingSyncDao.getAllPendingActionsSync()
        for (action in pendingActions) {
            try {
                when (action.actionType) {
                    "START_SESSION" -> {
                        val req = gson.fromJson(action.payloadJson, StartSessionRequest::class.java)
                        val response = erpService.startSession(req)
                        response.data?.let { saveSessionLocally(it) }
                    }
                    "UPDATE_SET" -> {
                        val req = gson.fromJson(action.payloadJson, UpdateSetRequest::class.java)
                        erpService.updateSet(action.entityId, req)
                    }
                    "UPDATE_EXERCISE_STATUS" -> {
                        val req = gson.fromJson(action.payloadJson, UpdateExerciseStatusRequest::class.java)
                        erpService.updateExerciseStatus(action.entityId, req)
                    }
                    "FINISH_SESSION" -> {
                        val req = gson.fromJson(action.payloadJson, FinishSessionRequest::class.java)
                        val response = erpService.finishSession(action.entityId, req)
                        response.data?.let { saveSessionLocally(it) }
                    }
                }
                pendingSyncDao.deleteActionSync(action.id)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 409 || e.code() == 404) {
                    pendingSyncDao.deleteActionSync(action.id)
                }
            } catch (e: Exception) {
                break
            }
        }
    }

    override suspend fun getExerciseProgress(exerciseId: String): List<ExerciseProgressPoint> {
        val isOnline = networkMonitor.isCurrentlyOnline()
        if (isOnline) {
            try {
                val response = erpService.getExerciseProgress(exerciseId)
                val points = response.data ?: emptyList()

                // Cache locally
                val localPoints = points.mapNotNull { p ->
                    val date = p.sessionDate ?: return@mapNotNull null
                    LocalExerciseProgressPoint(
                        exerciseId = exerciseId,
                        sessionDate = date,
                        maxWeightKg = p.maxWeightKg,
                        totalReps = p.totalReps ?: 0,
                        totalDurationSeconds = p.totalDurationSeconds,
                        totalDistanceMeters = p.totalDistanceMeters
                    )
                }
                workoutSessionDao.insertProgressPoints(localPoints)
                return points
            } catch (e: Exception) {
                // Fallback to local cache
            }
        }

        // Fetch offline from Room
        return workoutSessionDao.getProgressHistoryForExerciseSync(exerciseId).map { local ->
            ExerciseProgressPoint(
                sessionDate = local.sessionDate,
                maxWeightKg = local.maxWeightKg,
                totalReps = local.totalReps,
                totalDurationSeconds = local.totalDurationSeconds,
                totalDistanceMeters = local.totalDistanceMeters
            )
        }
    }

    private suspend fun saveSessionLocally(session: WorkoutSession) {
        workoutSessionDao.insertSession(session.toLocal())
        session.exercises?.let { exercises ->
            workoutSessionDao.insertExercises(exercises.map { it.toLocal() })
            val sets = exercises.flatMap { it.sets ?: emptyList() }
            workoutSessionDao.insertSets(sets.map { it.toLocal() })
        }
    }

    private fun WorkoutSession.toLocal() = LocalWorkoutSession(
        id = id,
        alunoId = alunoId,
        assignmentId = assignmentId,
        templateWorkoutId = templateWorkoutId,
        workoutName = workoutName,
        sessionNumber = sessionNumber ?: 0,
        startedAt = startedAt ?: DateUtils.getNowIso(),
        finishedAt = finishedAt,
        rating = rating,
        feedbackText = feedbackText,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun SessionExercise.toLocal() = LocalSessionExercise(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        muscleGroup = muscleGroup,
        orderIndex = orderIndex ?: 0,
        status = status ?: "not_started",
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun SessionSet.toLocal() = LocalSessionSet(
        id = id,
        sessionExerciseId = sessionExerciseId,
        setNumber = setNumber ?: 0,
        repsCompleted = repsCompleted ?: 0,
        weightKg = weightKg,
        durationSeconds = durationSeconds,
        distanceMeters = distanceMeters,
        notes = notes,
        isCompleted = isCompleted ?: false,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
