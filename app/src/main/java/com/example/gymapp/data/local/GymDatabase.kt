package com.example.gymapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalWorkoutSession::class,
        LocalSessionExercise::class,
        LocalSessionSet::class,
        PendingSyncAction::class,
        LocalExerciseProgressPoint::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun pendingSyncDao(): PendingSyncDao
}
