package com.example.gymapp.data.repository

import com.example.gymapp.data.local.*
import com.example.gymapp.data.remote.*
import com.example.gymapp.domain.model.*
import com.example.gymapp.utils.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class WorkoutSessionRepositoryTest {

    private class FakeNetworkMonitor : NetworkMonitor {
        var online: Boolean = true
        override val isOnline: Flow<Boolean> = flowOf(online)
        override suspend fun isCurrentlyOnline(): Boolean = online
    }

    private class FakeWorkoutSessionDao : WorkoutSessionDao {
        val sessions = mutableListOf<LocalWorkoutSession>()
        val progressPoints = mutableListOf<LocalExerciseProgressPoint>()

        override suspend fun insertSession(session: LocalWorkoutSession): Long {
            sessions.add(session)
            return 1L
        }
        override suspend fun insertExercises(exercises: List<LocalSessionExercise>): List<Long> = emptyList()
        override suspend fun insertSets(sets: List<LocalSessionSet>): List<Long> = emptyList()
        override suspend fun getSessionWithExercisesAndSetsSync(sessionId: String): LocalWorkoutSession? = null
        override suspend fun getExercisesForSessionSync(sessionId: String): List<LocalSessionExercise> = emptyList()
        override suspend fun getSetsForExerciseSync(sessionExerciseId: String): List<LocalSessionSet> = emptyList()
        override suspend fun getSessionsByAlunoSync(alunoId: String): List<LocalWorkoutSession> = emptyList()
        override suspend fun updateSetSync(
            setId: String, reps: Int, weight: Double?, duration: Int?, distance: Double?, isCompleted: Boolean, updatedAt: String
        ): Int = 0
        override suspend fun updateExerciseStatusSync(exerciseId: String, status: String, updatedAt: String): Int = 0
        override suspend fun finishSessionSync(sessionId: String, finishedAt: String, rating: Int?, feedbackText: String?, updatedAt: String): Int = 0
        override suspend fun deleteSessionSync(sessionId: String): Int = 0

        override suspend fun insertProgressPoints(points: List<LocalExerciseProgressPoint>): List<Long> {
            progressPoints.addAll(points)
            return points.map { 1L }
        }

        override suspend fun getProgressHistoryForExerciseSync(exerciseId: String): List<LocalExerciseProgressPoint> {
            return progressPoints.filter { it.exerciseId == exerciseId }
        }
    }

    private class FakePendingSyncDao : PendingSyncDao {
        val actions = mutableListOf<PendingSyncAction>()
        override suspend fun insertAction(action: PendingSyncAction): Long {
            actions.add(action)
            return 1L
        }
        override suspend fun getAllPendingActionsSync(): List<PendingSyncAction> = actions
        override suspend fun deleteActionSync(actionId: Int): Int {
            actions.removeIf { it.id == actionId }
            return 1
        }
        override suspend fun deleteActionsForSessionSync(sessionId: String): Int {
            actions.removeIf { it.sessionId == sessionId }
            return 1
        }
    }

    private class FakeErpService : ErpService {
        var startSessionResult: ApiResponse<WorkoutSession>? = null
        var exerciseProgressResult: ApiResponse<List<ExerciseProgressPoint>>? = null
        
        override suspend fun startSession(request: StartSessionRequest): ApiResponse<WorkoutSession> {
            return startSessionResult ?: throw Exception("Not mocked")
        }

        override suspend fun getExercises(limit: Int?, offset: Int?, muscleGroup: String?, search: String?) = PaginatedResponse<Exercise>(null, null)
        override suspend fun createExercise(
            name: okhttp3.RequestBody,
            description: okhttp3.RequestBody?,
            muscleGroup: okhttp3.RequestBody,
            usesWeight: okhttp3.RequestBody,
            videoUrl: okhttp3.RequestBody?,
            videoUrls: List<okhttp3.MultipartBody.Part>?,
            files: List<okhttp3.MultipartBody.Part>?
        ) = ApiResponse<Exercise>(null)

        override suspend fun updateExercise(
            id: String,
            name: okhttp3.RequestBody,
            description: okhttp3.RequestBody?,
            muscleGroup: okhttp3.RequestBody,
            usesWeight: okhttp3.RequestBody,
            videoUrl: okhttp3.RequestBody?,
            mediaPath: okhttp3.RequestBody?,
            mediaType: okhttp3.RequestBody?,
            keepMediaIds: List<okhttp3.MultipartBody.Part>?,
            videoUrls: List<okhttp3.MultipartBody.Part>?,
            files: List<okhttp3.MultipartBody.Part>?
        ) = ApiResponse<Exercise>(null)
        override suspend fun deleteExercise(id: String): Response<Unit> = Response.success(Unit)
        override suspend fun getExercise(id: String) = ApiResponse<Exercise>(null)
        override suspend fun getTemplates(limit: Int?, offset: Int?, withWorkoutDays: Boolean?) = PaginatedResponse<WorkoutTemplate>(null, null)
        override suspend fun getTemplate(id: String) = ApiResponse<WorkoutTemplate>(null)
        override suspend fun createTemplate(request: CreateTemplateRequest) = ApiResponse<WorkoutTemplate>(null)
        override suspend fun updateTemplate(id: String, request: CreateTemplateRequest) = ApiResponse<WorkoutTemplate>(null)
        override suspend fun deleteTemplate(id: String) = Response.success(Unit)
        override suspend fun getAssignmentsByAluno(alunoId: String, with: String?) = ApiResponse<List<WorkoutAssignment>>(null)
        override suspend fun getCurrentAssignment(alunoId: String) = ApiResponse<WorkoutAssignment?>(null)
        override suspend fun assignWorkout(request: AssignWorkoutRequest) = ApiResponse<WorkoutAssignment>(null)
        override suspend fun assignWorkoutToGroup(request: AssignGroupWorkoutRequest) = ApiResponse<List<WorkoutAssignment>>(null)
        override suspend fun getSessionsByAluno(alunoId: String, limit: Int?, offset: Int?) = PaginatedResponse<WorkoutSession>(null, null)
        override suspend fun getSession(id: String) = ApiResponse<WorkoutSession>(null)
        override suspend fun updateSet(setId: String, request: UpdateSetRequest) = ApiResponse<SessionSet>(null)
        override suspend fun updateExerciseStatus(exerciseId: String, request: UpdateExerciseStatusRequest) = Response.success(Unit)
        override suspend fun finishSession(id: String, request: FinishSessionRequest) = ApiResponse<WorkoutSession>(null)
        override suspend fun getExerciseProgress(exerciseId: String): ApiResponse<List<ExerciseProgressPoint>> {
            return exerciseProgressResult ?: ApiResponse(null)
        }
        override suspend fun getAnnouncement(id: String) = ApiResponse<Announcement>(null)
        override suspend fun getAlunoStats(alunoId: String) = ApiResponse<AlunoStats>(null)
        override suspend fun getAnnouncements(limit: Int?, offset: Int?, type: String?) = PaginatedResponse<Announcement>(null, null)
        override suspend fun createAnnouncement(request: CreateAnnouncementRequest) = ApiResponse<Announcement>(null)
        override suspend fun updateAnnouncement(id: String, request: UpdateAnnouncementRequest) = ApiResponse<Announcement>(null)
        override suspend fun deleteAnnouncement(id: String) = Response.success(Unit)
        override suspend fun getMeasurementsChart(id: String) = ApiResponse<List<BodyMeasurement>>(null)
        override suspend fun setExerciseMetric(request: SetExerciseMetricRequest) = ApiResponse<ExerciseCustomMetric>(null)
        override suspend fun getExerciseMetric(exerciseId: String) = ApiResponse<ExerciseCustomMetric>(null)
        override suspend fun deleteExerciseMetric(exerciseId: String) = Response.success(Unit)
        override suspend fun getInstitutos(limit: Int?, offset: Int?) = PaginatedResponse<Instituto>(null, null)
        override suspend fun createInstituto(request: CreateInstitutoRequest) = ApiResponse<Instituto>(null)
        override suspend fun updateInstituto(id: String, request: CreateInstitutoRequest) = ApiResponse<Instituto>(null)
        override suspend fun deleteInstituto(id: String) = Response.success(Unit)
        override suspend fun getAuditLogs(limit: Int?, offset: Int?) = PaginatedResponse<AuditLogEntry>(null, null)
        override suspend fun getBackgroundJobs() = emptyList<BackgroundJob>()
        override suspend fun storeFCMToken(request: FCMTokenRequest) = ApiResponse<FCMToken>(null)
        override suspend fun deleteFCMToken(request: FCMTokenDeleteRequest) = Response.success(Unit)
        override suspend fun listFCMTokens() = ApiResponse<List<FCMToken>>(null)
    }

    private val erpService = FakeErpService()
    private val workoutSessionDao = FakeWorkoutSessionDao()
    private val pendingSyncDao = FakePendingSyncDao()
    private val networkMonitor = FakeNetworkMonitor()

    private lateinit var repository: WorkoutSessionRepository

    @Before
    fun setUp() {
        repository = WorkoutSessionRepositoryImpl(
            erpService,
            workoutSessionDao,
            pendingSyncDao,
            networkMonitor
        )
    }

    @Test
    fun testStartSessionOnline() {
        runBlocking {
            networkMonitor.online = true
            val mockSession = WorkoutSession(
                id = "test-session-id",
                alunoId = "aluno-1",
                assignmentId = "assign-1",
                templateWorkoutId = null,
                workoutName = "Treino A",
                sessionNumber = 1,
                startedAt = "2026-06-21T19:00:00Z",
                finishedAt = null,
                rating = null,
                feedbackText = null,
                createdAt = null,
                updatedAt = null,
                exercises = emptyList()
            )
            erpService.startSessionResult = ApiResponse(data = mockSession)

            val result = repository.startSession("assign-1")

            assertEquals("test-session-id", result.id)
            assertEquals(1, workoutSessionDao.sessions.size)
            assertEquals("test-session-id", workoutSessionDao.sessions[0].id)
            assertEquals(0, pendingSyncDao.actions.size)
        }
    }

    @Test
    fun testStartSessionOffline() {
        runBlocking {
            networkMonitor.online = false

            val result = repository.startSession("assign-1")

            assertNotNull(result.id)
            assertEquals("Treino Offline", result.workoutName)
            assertEquals(1, workoutSessionDao.sessions.size)
            assertEquals(result.id, workoutSessionDao.sessions[0].id)
            assertEquals(1, pendingSyncDao.actions.size)
            assertEquals("START_SESSION", pendingSyncDao.actions[0].actionType)
        }
    }

    @Test
    fun testGetExerciseProgressOnline() {
        runBlocking {
            networkMonitor.online = true
            val mockPoints = listOf(
                ExerciseProgressPoint(
                    sessionDate = "2026-06-21",
                    maxWeightKg = 80.0,
                    totalReps = 36,
                    totalDurationSeconds = null,
                    totalDistanceMeters = null
                )
            )
            erpService.exerciseProgressResult = ApiResponse(data = mockPoints)

            val result = repository.getExerciseProgress("ex-1")

            assertEquals(1, result.size)
            assertEquals(80.0, result[0].maxWeightKg)
            
            // Verify cache insert
            assertEquals(1, workoutSessionDao.progressPoints.size)
            assertEquals("ex-1", workoutSessionDao.progressPoints[0].exerciseId)
            assertEquals("2026-06-21", workoutSessionDao.progressPoints[0].sessionDate)
            assertEquals(80.0, workoutSessionDao.progressPoints[0].maxWeightKg)
        }
    }

    @Test
    fun testGetExerciseProgressOffline() {
        runBlocking {
            networkMonitor.online = false
            // Pre-populate database cache
            workoutSessionDao.progressPoints.add(
                LocalExerciseProgressPoint(
                    exerciseId = "ex-1",
                    sessionDate = "2026-06-20",
                    maxWeightKg = 75.0,
                    totalReps = 30,
                    totalDurationSeconds = null,
                    totalDistanceMeters = null
                )
            )

            val result = repository.getExerciseProgress("ex-1")

            assertEquals(1, result.size)
            assertEquals("2026-06-20", result[0].sessionDate)
            assertEquals(75.0, result[0].maxWeightKg)
        }
    }
}
