package com.example.gymapp.domain.model

import com.google.gson.annotations.SerializedName

// ==================== SESSION REQUESTS ====================

data class StartSessionRequest(
	@SerializedName("assignment_id") val assignmentId: String,
	@SerializedName("session_id") val sessionId: String? = null
)

data class UpdateSetRequest(
	@SerializedName("reps_completed") val repsCompleted: Int,
	@SerializedName("weight_kg") val weightKg: Double?,
	@SerializedName("duration_seconds") val durationSeconds: Int?,
	@SerializedName("distance_meters") val distanceMeters: Double?,
	val notes: String?,
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
	@SerializedName("starts_at") val startsAt: String,
	@SerializedName("ends_at") val endsAt: String? = null
)

data class AssignGroupWorkoutRequest(
	@SerializedName("group_id") val groupId: String,
	@SerializedName("template_id") val templateId: String,
	@SerializedName("starts_at") val startsAt: String,
	@SerializedName("ends_at") val endsAt: String? = null
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

// Request for setting a custom metric preference for an exercise
data class SetExerciseMetricRequest(
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("metric_type") val metricType: String
)

// ==================== TEMPLATE REQUESTS ====================

// CreateTemplateRequest now uses workout_days instead of flat exercises + total_sessions.
// Each workout day (e.g. "Treino A", "Treino B") has its own list of exercises.
data class CreateTemplateRequest(
	val name: String,
	val type: String,
	val difficulty: String,
	@SerializedName("workout_days") val workoutDays: List<WorkoutDayInput>
)

data class WorkoutDayInput(
	val name: String,
	@SerializedName("order_index") val orderIndex: Int,
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
	val content: String,
	val type: String
)

data class UpdateAnnouncementRequest(
	@SerializedName("title") val title: String? = null,
	@SerializedName("content") val content: String? = null,
	@SerializedName("type") val type: String? = null
)

// ==================== PROFILE REQUESTS ====================

data class UpsertProfileRequest(
	@SerializedName("current_weight_kg") val currentWeightKg: Double,
	@SerializedName("height_cm") val heightCm: Double,
	@SerializedName("injury_history") val injuryHistory: String?
)

data class AddMeasurementRequest(
	@SerializedName("weight_kg") val weightKg: Double,
	@SerializedName("measured_at") val measuredAt: String
)

// ==================== GROUP REQUESTS ====================

data class CreateGroupRequest(
	val name: String,
	val description: String? = null
)

data class AddGroupMemberRequest(
	@SerializedName("user_id") val userId: String
)

// ==================== USER REQUESTS ====================

data class UpdateUserRequest(
	@SerializedName("full_name") val fullName: String? = null,
	val instituto: String? = null,
	@SerializedName("current_weight_kg") val currentWeightKg: Double? = null,
	@SerializedName("height_cm") val heightCm: Double? = null,
	@SerializedName("injury_history") val injuryHistory: String? = null
)

data class UpdateRoleRequest(
	val role: String
)

data class UpdateStatusRequest(
	@SerializedName("is_active") val isActive: Boolean
)

data class UpdateBlockedRequest(
	@SerializedName("is_blocked") val isBlocked: Boolean
)
