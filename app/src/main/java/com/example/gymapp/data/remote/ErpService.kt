package com.example.gymapp.data.remote

import com.example.gymapp.domain.model.*
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

    @GET("exercises/{id}")
    suspend fun getExercise(
        @Path("id") id: String
    ): ApiResponse<Exercise>

    @POST("exercises")
    suspend fun createExercise(
        @Body request: CreateExerciseRequest
    ): ApiResponse<Exercise>

    @PUT("exercises/{id}")
    suspend fun updateExercise(
        @Path("id") id: String,
        @Body request: CreateExerciseRequest
    ): ApiResponse<Exercise>

    @DELETE("exercises/{id}")
    suspend fun deleteExercise(
        @Path("id") id: String
    ): Response<Unit>

    // ==================== TEMPLATES ====================

    @GET("templates")
    suspend fun getTemplates(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
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
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): PaginatedResponse<WorkoutAssignment>

    @GET("assignments/aluno/{aluno_id}/current")
    suspend fun getCurrentAssignment(
        @Path("aluno_id") alunoId: String
    ): ApiResponse<WorkoutAssignment>

    @POST("assignments")
    suspend fun assignWorkout(
        @Body request: AssignWorkoutRequest
    ): ApiResponse<WorkoutAssignment>

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
    ): ApiResponse<SessionExercise>

    @POST("sessions/{id}/finish")
    suspend fun finishSession(
        @Path("id") id: String,
        @Body request: FinishSessionRequest
    ): ApiResponse<WorkoutSession>

    @GET("sessions/history/{exerciseId}")
    suspend fun getExerciseLoadHistory(
    @Path("exerciseId") exerciseId: String
    ): ApiResponse<List<ExerciseLoadHistory>>

    // ==================== ANNOUNCEMENTS ====================

    @GET("announcements")
    suspend fun getAnnouncements(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): PaginatedResponse<Announcement>

    @GET("announcements/{id}")
    suspend fun getAnnouncement(
        @Path("id") id: String
    ): ApiResponse<Announcement>

    @POST("announcements")
    suspend fun createAnnouncement(
        @Body request: CreateAnnouncementRequest
    ): ApiResponse<Announcement>

    @DELETE("announcements/{id}")
    suspend fun deleteAnnouncement(
        @Path("id") id: String
    ): Response<Unit>
}
