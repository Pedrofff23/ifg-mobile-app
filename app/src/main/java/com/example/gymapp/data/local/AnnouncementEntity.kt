package com.example.gymapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class LocalAnnouncement(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val type: String, // noticia/aviso/instrucoes
    val instituto: String?,
    val authorId: String?,
    val publishedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val authorName: String?
)