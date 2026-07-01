package com.example.gymapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(announcements: List<LocalAnnouncement>): LongArray

    @Query("SELECT * FROM announcements WHERE instituto = :institutoId ORDER BY publishedAt DESC")
    suspend fun getByInstituto(institutoId: String): List<LocalAnnouncement>

    @Query("SELECT * FROM announcements WHERE instituto = :institutoId ORDER BY publishedAt DESC")
    fun getByInstitutoFlow(institutoId: String): Flow<List<LocalAnnouncement>>

    @Query("DELETE FROM announcements WHERE instituto = :institutoId")
    suspend fun deleteByInstituto(institutoId: String): Int

    @Query("DELETE FROM announcements")
    suspend fun deleteAll(): Int
}