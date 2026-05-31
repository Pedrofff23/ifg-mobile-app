package com.example.gymapp.domain.model

import com.google.gson.annotations.SerializedName

// ==================== EXERCISE ====================
data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    @SerializedName("muscle_group") val muscleGroup: String,
    @SerializedName("uses_weight") val usesWeight: Boolean,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("media_path") val mediaPath: String?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// ==================== WORKOUT TEMPLATE ====================
data class WorkoutTemplate(
    val id: String,
    val name: String,
    val type: String,
    val difficulty: String,
    @SerializedName("total_sessions") val totalSessions: Int,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("updated_by") val updatedBy: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val exercises: List<TemplateExercise> = emptyList()
)

data class TemplateExercise(
    val id: String,
    @SerializedName("template_id") val templateId: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("exercise_name") val exerciseName: String?,
    @SerializedName("order_index") val orderIndex: Int,
    @SerializedName("default_sets") val defaultSets: Int,
    @SerializedName("default_reps") val defaultReps: Int,
    @SerializedName("default_rest_seconds") val defaultRestSeconds: Int,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// ==================== WORKOUT ASSIGNMENT ====================
data class WorkoutAssignment(
    val id: String,
    @SerializedName("aluno_id") val alunoId: String,
    @SerializedName("template_id") val templateId: String,
    @SerializedName("assigned_by") val assignedBy: String?,
    @SerializedName("starts_at") val startsAt: String,
    @SerializedName("ends_at") val endsAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("template_name") val templateName: String?
)

// ==================== WORKOUT SESSION ====================
data class WorkoutSession(
    val id: String,
    @SerializedName("aluno_id") val alunoId: String,
    @SerializedName("assignment_id") val assignmentId: String,
    @SerializedName("session_number") val sessionNumber: Int,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("finished_at") val finishedAt: String?,
    val rating: Int?,
    @SerializedName("feedback_text") val feedbackText: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val exercises: List<SessionExercise> = emptyList()
)

data class SessionExercise(
    val id: String,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("exercise_name") val exerciseName: String?,
    @SerializedName("order_index") val orderIndex: Int,
    val status: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val sets: List<SessionSet> = emptyList()
)

data class SessionSet(
    val id: String,
    @SerializedName("session_exercise_id") val sessionExerciseId: String,
    @SerializedName("set_number") val setNumber: Int,
    @SerializedName("reps_completed") val repsCompleted: Int,
    @SerializedName("weight_kg") val weightKg: Double?,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// ==================== ANNOUNCEMENT ====================
data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    @SerializedName("author_id") val authorId: String?,
    @SerializedName("published_at") val publishedAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("author_name") val authorName: String?
)

// ==================== ALUNO PROFILE ====================
data class AlunoProfile(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("current_weight_kg") val currentWeightKg: Double,
    @SerializedName("height_cm") val heightCm: Double,
    @SerializedName("injury_history") val injuryHistory: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class BodyMeasurement(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("weight_kg") val weightKg: Double,
    @SerializedName("measured_at") val measuredAt: String,
    val notes: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

// ==================== LOAD HISTORY (view) ====================
data class ExerciseLoadHistory(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("max_weight_kg") val maxWeightKg: Double,
    @SerializedName("session_date") val sessionDate: String
)

// ==================== SESSION COMPLETION (view) ====================
data class SessionCompletion(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("total_exercises") val totalExercises: Int,
    @SerializedName("completed_exercises") val completedExercises: Int,
    @SerializedName("completion_percentage") val completionPercentage: Double
)
