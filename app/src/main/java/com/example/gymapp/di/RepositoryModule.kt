package com.example.gymapp.di

import com.example.gymapp.data.repository.AnnouncementRepository
import com.example.gymapp.data.repository.AnnouncementRepositoryImpl
import com.example.gymapp.data.repository.WorkoutSessionRepository
import com.example.gymapp.data.repository.WorkoutSessionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutSessionRepository(
        impl: WorkoutSessionRepositoryImpl
    ): WorkoutSessionRepository

    @Binds
    @Singleton
    abstract fun bindAnnouncementRepository(
        impl: AnnouncementRepositoryImpl
    ): AnnouncementRepository
}
