package com.example.gymapp.domain.model

import com.google.gson.annotations.SerializedName

// ==================== SESSION REQUESTS ====================

data class StartSessionRequest(
    @SerializedName("assignment_id") val assignmentId: String
)

data class UpdateSetRequest(
    @SerializedName("reps_completed") val repsCompleted: Int,
    @SerializedName("weight_kg") val weightKg: Double?,
    @SerializedName("is_completed") val isCompleted: Boolean
)

data class UpdateExerciseStatusRequest(
    val status: String
)

data class FinishSessionRequest(
    val rating: Int?,
    @SerializedName("feedback_text") val feedbackText: String?
)

// ==================== ASSIGNMENT REQUESTS ====================

data class AssignWorkoutRequest(
    @SerializedName("aluno_id") val alunoId: String,
    @SerializedName("template_id") val templateId: String,
    @SerializedName("starts_at") val startsAt: String
)

// ==================== EXERCISE REQUESTS ====================

data class CreateExerciseRequest(
    val name: String,
    val description: String,
    @SerializedName("muscle_group") val muscleGroup: String,
    @SerializedName("uses_weight") val usesWeight: Boolean,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("media_path") val mediaPath: String?
)

// ==================== TEMPLATE REQUESTS ====================

data class CreateTemplateRequest(
    val name: String,
    val type: String,
    val difficulty: String,
    @SerializedName("total_sessions") val totalSessions: Int,
    val exercises: List<TemplateExerciseInput>
)

data class TemplateExerciseInput(
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("order_index") val orderIndex: Int,
    @SerializedName("default_sets") val defaultSets: Int,
    @SerializedName("default_reps") val defaultReps: Int,
    @SerializedName("default_rest_seconds") val defaultRestSeconds: Int
)

// ==================== ANNOUNCEMENT REQUESTS ====================

data class CreateAnnouncementRequest(
    val title: String,
    val content: String
)

// ==================== PROFILE REQUESTS ====================

data class UpsertProfileRequest(
    @SerializedName("current_weight_kg") val currentWeightKg: Double,
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("injury_history") val injuryHistory: String?
)

data class AddMeasurementRequest(
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("measured_at") val measuredAt: String,
    val notes: String?
)

// ==================== USER REQUESTS ====================

data class UpdateUserRequest(
    @SerializedName("full_name") val fullName: String?,
    val instituto: String?
)

data class UpdateRoleRequest(
    val role: String
)

data class UpdateStatusRequest(
    @SerializedName("is_active") val isActive: Boolean
)
