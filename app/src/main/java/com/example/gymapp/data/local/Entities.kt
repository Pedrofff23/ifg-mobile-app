package com.example.gymapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class LocalWorkoutSession(
    @PrimaryKey val id: String,
    val alunoId: String,
    val assignmentId: String,
    val templateWorkoutId: String?,
    val workoutName: String?,
    val sessionNumber: Int,
    val startedAt: String,
    val finishedAt: String?,
    val rating: Int?,
    val feedbackText: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@Entity(tableName = "session_exercises")
data class LocalSessionExercise(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String?,
    val muscleGroup: String?,
    val orderIndex: Int,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?
)

@Entity(tableName = "session_sets")
data class LocalSessionSet(
    @PrimaryKey val id: String,
    val sessionExerciseId: String,
    val setNumber: Int,
    val repsCompleted: Int,
    val weightKg: Double?,
    val durationSeconds: Int?,
    val distanceMeters: Double?,
    val notes: String?,
    val isCompleted: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

@Entity(tableName = "pending_sync_actions")
data class PendingSyncAction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val actionType: String, // "START_SESSION", "UPDATE_SET", "UPDATE_EXERCISE_STATUS", "FINISH_SESSION"
    val timestamp: Long,
    val sessionId: String, // Contextual session ID
    val entityId: String, // Can be sessionId, exerciseId, or setId depending on actionType
    val payloadJson: String // Serialized DTO or payload
)

@Entity(
    tableName = "exercise_progress_history",
    primaryKeys = ["exerciseId", "sessionDate"]
)
data class LocalExerciseProgressPoint(
    val exerciseId: String,
    val sessionDate: String,
    val maxWeightKg: Double?,
    val totalReps: Int,
    val totalDurationSeconds: Int?,
    val totalDistanceMeters: Double?
)

