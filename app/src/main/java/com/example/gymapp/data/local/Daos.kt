package com.example.gymapp.data.local

import androidx.room.*

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LocalWorkoutSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<LocalSessionExercise>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<LocalSessionSet>): List<Long>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionWithExercisesAndSetsSync(sessionId: String): LocalWorkoutSession?

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getExercisesForSessionSync(sessionId: String): List<LocalSessionExercise>

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExerciseSync(sessionExerciseId: String): List<LocalSessionSet>

    @Query("SELECT * FROM workout_sessions WHERE alunoId = :alunoId ORDER BY startedAt DESC")
    suspend fun getSessionsByAlunoSync(alunoId: String): List<LocalWorkoutSession>

    @Query("UPDATE session_sets SET repsCompleted = :reps, weightKg = :weight, durationSeconds = :duration, distanceMeters = :distance, isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :setId")
    suspend fun updateSetSync(
        setId: String,
        reps: Int,
        weight: Double?,
        duration: Int?,
        distance: Double?,
        isCompleted: Boolean,
        updatedAt: String
    ): Int

    @Query("UPDATE session_exercises SET status = :status, updatedAt = :updatedAt WHERE id = :exerciseId")
    suspend fun updateExerciseStatusSync(exerciseId: String, status: String, updatedAt: String): Int

    @Query("UPDATE workout_sessions SET finishedAt = :finishedAt, rating = :rating, feedbackText = :feedbackText, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun finishSessionSync(sessionId: String, finishedAt: String, rating: Int?, feedbackText: String?, updatedAt: String): Int

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionSync(sessionId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPoints(points: List<LocalExerciseProgressPoint>): List<Long>

    @Query("SELECT * FROM exercise_progress_history WHERE exerciseId = :exerciseId ORDER BY sessionDate DESC LIMIT 50")
    suspend fun getProgressHistoryForExerciseSync(exerciseId: String): List<LocalExerciseProgressPoint>
}

@Dao
interface PendingSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: PendingSyncAction): Long

    @Query("SELECT * FROM pending_sync_actions ORDER BY timestamp ASC")
    suspend fun getAllPendingActionsSync(): List<PendingSyncAction>

    @Query("DELETE FROM pending_sync_actions WHERE id = :actionId")
    suspend fun deleteActionSync(actionId: Int): Int

    @Query("DELETE FROM pending_sync_actions WHERE sessionId = :sessionId")
    suspend fun deleteActionsForSessionSync(sessionId: String): Int
}
