package com.example.gymapp.data.remote

import com.example.gymapp.domain.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.http.*
import retrofit2.Response

interface ErpService {

    // ==================== EXERCISES ====================

    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("muscle_group") muscleGroup: String? = null,
        @Query("search") search: String? = null
    ): PaginatedResponse<Exercise>

    @Multipart
    @POST("exercises")
    suspend fun createExercise(
        @Part("name") name: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody?,
        @Part("muscle_group") muscleGroup: okhttp3.RequestBody,
        @Part("uses_weight") usesWeight: okhttp3.RequestBody,
        @Part("video_url") videoUrl: okhttp3.RequestBody?,
        @Part file: okhttp3.MultipartBody.Part?
    ): ApiResponse<Exercise>

    @Multipart
    @PUT("exercises/{id}")
    suspend fun updateExercise(
        @Path("id") id: String,
        @Part("name") name: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody?,
        @Part("muscle_group") muscleGroup: okhttp3.RequestBody,
        @Part("uses_weight") usesWeight: okhttp3.RequestBody,
        @Part("video_url") videoUrl: okhttp3.RequestBody?,
        @Part("media_path") mediaPath: okhttp3.RequestBody?,
        @Part("media_type") mediaType: okhttp3.RequestBody?,
        @Part file: okhttp3.MultipartBody.Part?
    ): ApiResponse<Exercise>

    @DELETE("exercises/{id}")
    suspend fun deleteExercise(
        @Path("id") id: String
    ): Response<Unit>

    @GET("exercises/{id}")
    suspend fun getExercise(
        @Path("id") id: String
    ): ApiResponse<Exercise>

    // ==================== TEMPLATES ====================

    @GET("templates")
    suspend fun getTemplates(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("__with_workout_days") withWorkoutDays: Boolean? = null
    ): PaginatedResponse<WorkoutTemplate>

    @GET("templates/{id}")
    suspend fun getTemplate(
        @Path("id") id: String
    ): ApiResponse<WorkoutTemplate>

    @POST("templates")
    suspend fun createTemplate(
        @Body request: CreateTemplateRequest
    ): ApiResponse<WorkoutTemplate>

    @PUT("templates/{id}")
    suspend fun updateTemplate(
        @Path("id") id: String,
        @Body request: CreateTemplateRequest
    ): ApiResponse<WorkoutTemplate>

    @DELETE("templates/{id}")
    suspend fun deleteTemplate(
        @Path("id") id: String
    ): Response<Unit>

    // ==================== ASSIGNMENTS ====================

    @GET("assignments/aluno/{aluno_id}")
    suspend fun getAssignmentsByAluno(
        @Path("aluno_id") alunoId: String,
        @Query("__with") with: String? = null
    ): ApiResponse<List<WorkoutAssignment>>

    @GET("assignments/aluno/{aluno_id}/current")
    suspend fun getCurrentAssignment(
        @Path("aluno_id") alunoId: String
    ): ApiResponse<WorkoutAssignment?>

    @POST("assignments")
    suspend fun assignWorkout(
        @Body request: AssignWorkoutRequest
    ): ApiResponse<WorkoutAssignment>

    @POST("assignments/group")
    suspend fun assignWorkoutToGroup(
        @Body request: AssignGroupWorkoutRequest
    ): ApiResponse<List<WorkoutAssignment>>

    // ==================== SESSIONS ====================

    @GET("sessions/aluno/{aluno_id}")
    suspend fun getSessionsByAluno(
        @Path("aluno_id") alunoId: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): PaginatedResponse<WorkoutSession>

    @POST("sessions/start")
    suspend fun startSession(
        @Body request: StartSessionRequest
    ): ApiResponse<WorkoutSession>

    @GET("sessions/{id}")
    suspend fun getSession(
        @Path("id") id: String
    ): ApiResponse<WorkoutSession>

    @PATCH("sessions/sets/{setId}")
    suspend fun updateSet(
        @Path("setId") setId: String,
        @Body request: UpdateSetRequest
    ): ApiResponse<SessionSet>

    @PATCH("sessions/exercises/{exerciseId}/status")
    suspend fun updateExerciseStatus(
        @Path("exerciseId") exerciseId: String,
        @Body request: UpdateExerciseStatusRequest
    ): Response<Unit>

    @POST("sessions/{id}/finish")
    suspend fun finishSession(
        @Path("id") id: String,
        @Body request: FinishSessionRequest
    ): ApiResponse<WorkoutSession>

    @GET("sessions/exercises/{exerciseId}/progress")
    suspend fun getExerciseProgress(
        @Path("exerciseId") exerciseId: String
    ): ApiResponse<List<ExerciseProgressPoint>>

    @GET("sessions/aluno/{aluno_id}/stats")
    suspend fun getAlunoStats(
        @Path("aluno_id") alunoId: String
    ): ApiResponse<AlunoStats>

    // ==================== ANNOUNCEMENTS ====================

    @GET("announcements")
    suspend fun getAnnouncements(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("type") type: String? = null
    ): PaginatedResponse<Announcement>

    @POST("announcements")
    suspend fun createAnnouncement(
        @Body request: CreateAnnouncementRequest
    ): ApiResponse<Announcement>

    @PATCH("announcements/{id}")
    suspend fun updateAnnouncement(
        @Path("id") id: String,
        @Body request: UpdateAnnouncementRequest
    ): ApiResponse<Announcement>

    @DELETE("announcements/{id}")
    suspend fun deleteAnnouncement(
        @Path("id") id: String
    ): Response<Unit>

    // ==================== PROFILE / MEASUREMENTS ====================

    @GET("profiles/{id}/measurements/chart")
    suspend fun getMeasurementsChart(
        @Path("id") id: String
    ): ApiResponse<List<BodyMeasurement>>

    // ==================== EXERCISE METRICS ====================

    @POST("exercise-metrics")
    suspend fun setExerciseMetric(
        @Body request: SetExerciseMetricRequest
    ): ApiResponse<ExerciseCustomMetric>

    @GET("exercise-metrics/{exerciseId}")
    suspend fun getExerciseMetric(
        @Path("exerciseId") exerciseId: String
    ): ApiResponse<ExerciseCustomMetric>

    @DELETE("exercise-metrics/{exerciseId}")
    suspend fun deleteExerciseMetric(
        @Path("exerciseId") exerciseId: String
    ): Response<Unit>

    // ==================== INSTITUTOS ====================

    @GET("institutos")
    suspend fun getInstitutos(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): PaginatedResponse<Instituto>

    @POST("institutos")
    suspend fun createInstituto(@Body request: CreateInstitutoRequest): ApiResponse<Instituto>

    @PUT("institutos/{id}")
    suspend fun updateInstituto(
        @Path("id") id: String,
        @Body request: CreateInstitutoRequest
    ): ApiResponse<Instituto>

    @DELETE("institutos/{id}")
    suspend fun deleteInstituto(@Path("id") id: String): Response<Unit>

    // ==================== ADMIN ====================

    @GET("admin/audit-logs")
    suspend fun getAuditLogs(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): PaginatedResponse<AuditLogEntry>

    @GET("admin/jobs")
    suspend fun getBackgroundJobs(): List<BackgroundJob>

    // ==================== NOTIFICATIONS ====================

    @POST("user/fcm-token")
    suspend fun storeFCMToken(@Body request: FCMTokenRequest): ApiResponse<FCMToken>

    @DELETE("user/fcm-token")
    suspend fun deleteFCMToken(@Body request: FCMTokenDeleteRequest): Response<Unit>

    @GET("user/fcm-tokens")
    suspend fun listFCMTokens(): ApiResponse<List<FCMToken>>
}

data class CreateInstitutoRequest(val name: String)

// ==================== NOTIFICATIONS ====================

data class FCMTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("device_info") val deviceInfo: String? = null
)

data class FCMTokenDeleteRequest(
    @SerializedName("fcm_token") val fcmToken: String
)

data class FCMToken(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("device_info") val deviceInfo: String?,
    @SerializedName("created_at") val createdAt: String?
)
