package com.example.gymapp.domain.model

import com.google.gson.annotations.SerializedName

data class ExerciseMedia(
	val id: String,
	@SerializedName("exercise_id") val exerciseId: String,
	@SerializedName("media_type") val mediaType: String,
	@SerializedName("media_path") val mediaPath: String?,
	@SerializedName("video_url") val videoUrl: String?
)

// ==================== EXERCISE ====================
data class Exercise(
	val id: String,
	val name: String,
	val description: String?,
	@SerializedName("muscle_group") val muscleGroup: String?,
	@SerializedName("uses_weight") val usesWeight: Boolean?,
	@SerializedName("video_url") val videoUrl: String?,
	@SerializedName("media_type") val mediaType: String?,
	@SerializedName("media_path") val mediaPath: String?,
	val medias: List<ExerciseMedia>? = emptyList(),
	val instituto: String?,
	@SerializedName("created_by") val createdBy: String?,
	@SerializedName("updated_by") val updatedBy: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?
)

// ==================== WORKOUT TEMPLATE ====================
data class WorkoutTemplate(
	val id: String,
	val name: String,
	val type: String?,
	val difficulty: String?,
	val instituto: String?,
	@SerializedName("created_by") val createdBy: String?,
	@SerializedName("updated_by") val updatedBy: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?,
	@SerializedName("workout_days") val workoutDays: List<TemplateWorkout>? = emptyList()
)

// TemplateWorkout represents a single workout day within a template.
// Example: "Treino A", "Treino B", "Leg Day"
data class TemplateWorkout(
	val id: String,
	@SerializedName("template_id") val templateId: String,
	val name: String,
	@SerializedName("order_index") val orderIndex: Int?,
	val exercises: List<TemplateExercise>? = emptyList(),
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?
)

data class TemplateExercise(
	val id: String,
	@SerializedName("template_id") val templateId: String,
	@SerializedName("template_workout_id") val templateWorkoutId: String?,
	@SerializedName("exercise_id") val exerciseId: String,
	@SerializedName("exercise_name") val exerciseName: String?,
	@SerializedName("order_index") val orderIndex: Int?,
	@SerializedName("default_sets") val defaultSets: Int?,
	@SerializedName("default_reps") val defaultReps: Int?,
	@SerializedName("default_rest_seconds") val defaultRestSeconds: Int?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?
)

// ==================== WORKOUT ASSIGNMENT ====================
data class WorkoutAssignment(
	val id: String,
	@SerializedName("aluno_id") val alunoId: String,
	@SerializedName("template_id") val templateId: String,
	@SerializedName("assigned_by") val assignedBy: String?,
	@SerializedName("starts_at") val startsAt: String?,
	@SerializedName("ends_at") val endsAt: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?,
	@SerializedName("template_name") val templateName: String?,
	@SerializedName("current_workout_index") val currentWorkoutIndex: Int?
)

// ==================== WORKOUT SESSION ====================
data class WorkoutSession(
	val id: String,
	@SerializedName("aluno_id") val alunoId: String,
	@SerializedName("assignment_id") val assignmentId: String,
	@SerializedName("template_workout_id") val templateWorkoutId: String?,
	@SerializedName("workout_name") val workoutName: String?,
	@SerializedName("session_number") val sessionNumber: Int?,
	@SerializedName("started_at") val startedAt: String?,
	@SerializedName("finished_at") val finishedAt: String?,
	val rating: Int?,
	@SerializedName("feedback_text") val feedbackText: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?,
	val exercises: List<SessionExercise>? = emptyList()
)

data class SessionExercise(
	val id: String,
	@SerializedName("session_id") val sessionId: String,
	@SerializedName("exercise_id") val exerciseId: String,
	@SerializedName("exercise_name") val exerciseName: String?,
	@SerializedName("muscle_group") val muscleGroup: String?,
	@SerializedName("order_index") val orderIndex: Int?,
	val status: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?,
	val sets: List<SessionSet>? = emptyList()
)

data class SessionSet(
	val id: String,
	@SerializedName("session_exercise_id") val sessionExerciseId: String,
	@SerializedName("set_number") val setNumber: Int?,
	@SerializedName("reps_completed") val repsCompleted: Int?,
	@SerializedName("weight_kg") val weightKg: Double?,
	@SerializedName("duration_seconds") val durationSeconds: Int?,
	@SerializedName("distance_meters") val distanceMeters: Double?,
	val notes: String?,
	@SerializedName("is_completed") val isCompleted: Boolean?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?
)

// ==================== SESSION EXERCISE STATUS ====================

enum class SessionExerciseStatus(val raw: String) {
	NOT_STARTED("not_started"),
	IN_PROGRESS("in_progress"),
	COMPLETED("completed");

	companion object {
		fun fromRaw(raw: String?): SessionExerciseStatus = when (raw) {
			"not_started" -> NOT_STARTED
			"in_progress" -> IN_PROGRESS
			"completed" -> COMPLETED
			else -> NOT_STARTED
		}
	}
}

// Extension property on String? to convert to SessionExerciseStatus
val String?.sessionExerciseStatus: SessionExerciseStatus
	get() = SessionExerciseStatus.fromRaw(this)

// ==================== ANNOUNCEMENT ====================
data class Announcement(
	val id: String,
	val title: String,
	val content: String,
	val type: String?,
	val instituto: String?,
	@SerializedName("author_id") val authorId: String?,
	@SerializedName("published_at") val publishedAt: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?,
	@SerializedName("author_name") val authorName: String?
)

// ==================== STUDENT GROUP ====================
data class StudentGroup(
	val id: String,
	val name: String,
	val description: String?,
	val instituto: String?,
	@SerializedName("created_by") val createdBy: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?,
	val members: List<GroupMember>? = emptyList(),
	@SerializedName("current_assignment") val currentAssignment: GroupCurrentAssignment? = null
)

data class GroupMember(
	@SerializedName("group_id") val groupId: String,
	@SerializedName("user_id") val userId: String,
	@SerializedName("full_name") val fullName: String?,
	@SerializedName("joined_at") val joinedAt: String?
)

// GroupCurrentAssignment is returned when groups are fetched with __with=assignments.
data class GroupCurrentAssignment(
	val id: String,
	@SerializedName("aluno_id") val alunoId: String,
	@SerializedName("template_id") val templateId: String,
	@SerializedName("template_name") val templateName: String?,
	@SerializedName("assigned_by") val assignedBy: String?,
	@SerializedName("starts_at") val startsAt: String?,
	@SerializedName("ends_at") val endsAt: String?
)

// ==================== ALUNO PROFILE ====================
data class AlunoProfile(
	val id: String,
	@SerializedName("user_id") val userId: String,
	@SerializedName("current_weight_kg") val currentWeightKg: Double?,
	@SerializedName("height_cm") val heightCm: Double?,
	@SerializedName("injury_history") val injuryHistory: String?,
	@SerializedName("created_at") val createdAt: String?,
	@SerializedName("updated_at") val updatedAt: String?
)

data class BodyMeasurement(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("weight_kg") val weightKg: Double?,
    @SerializedName("measured_at") val measuredAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// New data class for custom exercise metric preference
data class ExerciseCustomMetric(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("metric_type") val metricType: String
)

// Simple chart point for measurement chart (date + weight)
data class MeasurementChartPoint(
    @SerializedName("measured_at") val measuredAt: String?,
    @SerializedName("weight_kg") val weightKg: Double?
)

// ==================== EXERCISE PROGRESS (v_exercise_progress) ====================
data class ExerciseProgressPoint(
	@SerializedName("session_date") val sessionDate: String?,
	@SerializedName("max_weight_kg") val maxWeightKg: Double?,
	@SerializedName("total_reps") val totalReps: Int?,
	@SerializedName("total_duration_seconds") val totalDurationSeconds: Int?,
	@SerializedName("total_distance_meters") val totalDistanceMeters: Double?
)

// ==================== ALUNO STATS ====================
data class AlunoStats(
	@SerializedName("total_sessions") val totalSessions: Int?,
	@SerializedName("completed_sessions") val completedSessions: Int?,
	@SerializedName("active_assignments") val activeAssignments: Int?,
	@SerializedName("total_exercises_done") val totalExercisesDone: Int?,
	@SerializedName("current_streak") val currentStreak: Int?,
	@SerializedName("weekly_frequency") val weeklyFrequency: Double?
)

// ==================== INSTITUTO ====================
data class Instituto(
	val id: String,
	val name: String
)

// ==================== ADMIN ====================

data class AuditLogEntry(
	val id: String,
	@SerializedName("user_id") val userId: String?,
	val action: String,
	val resource: String,
	@SerializedName("ip_address") val ipAddress: String?,
	@SerializedName("user_agent") val userAgent: String?,
	@SerializedName("created_at") val createdAt: String?
)

data class BackgroundJob(
	val id: String,
	val type: String,
	val status: String,
	val payload: String?,
	val error: String?,
	val retries: Int,
	@SerializedName("max_retries") val maxRetries: Int,
	@SerializedName("created_at") val createdAt: String?
)
