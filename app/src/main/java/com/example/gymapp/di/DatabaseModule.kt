package com.example.gymapp.di

import android.content.Context
import androidx.room.Room
import com.example.gymapp.data.local.AnnouncementDao
import com.example.gymapp.data.local.GymDatabase
import com.example.gymapp.data.local.PendingSyncDao
import com.example.gymapp.data.local.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymDatabase {
        return Room.databaseBuilder(
            context,
            GymDatabase::class.java,
            "gym_app_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideWorkoutSessionDao(db: GymDatabase): WorkoutSessionDao {
        return db.workoutSessionDao()
    }

    @Provides
    @Singleton
    fun providePendingSyncDao(db: GymDatabase): PendingSyncDao {
        return db.pendingSyncDao()
    }

    @Provides
    @Singleton
    fun provideAnnouncementDao(db: GymDatabase): AnnouncementDao {
        return db.announcementDao()
    }
}
