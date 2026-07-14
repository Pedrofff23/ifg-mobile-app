package com.example.gymapp.domain.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ModelParsingTest {

    @Test
    fun `Exercise JSON deserialization maps fields correctly`() {
        val json = """
        {
          "id": "ex1",
          "name": "Bench Press",
          "description": "Chest exercise",
          "muscle_group": "peito",
          "uses_weight": true,
          "video_url": "http://example.com/video.mp4",
          "media_type": "video",
          "media_path": "/videos/bench.mp4",
          "instituto": "IFG",
          "created_by": "admin",
          "updated_by": "admin",
          "created_at": "2023-01-01T00:00:00Z",
          "updated_at": "2023-01-02T00:00:00Z"
        }
        """.trimIndent()
        val exercise = Gson().fromJson(json, Exercise::class.java)
        assertEquals("ex1", exercise.id)
        assertEquals("Bench Press", exercise.name)
        assertEquals("peito", exercise.muscleGroup)
        assertEquals(true, exercise.usesWeight)
        assertEquals("http://example.com/video.mp4", exercise.videoUrl)
    }

    @Test
    fun `LoginResponse JSON deserialization with nested SupabaseUser`() {
        val json = """
        {
          "access_token": "abc123",
          "refresh_token": "def456",
          "user": {
            "id": "u1",
            "email": "test@example.com",
            "user_metadata": {
              "full_name": "Test User",
              "role": "aluno"
            }
          }
        }
        """.trimIndent()
        val response = Gson().fromJson(json, LoginResponse::class.java)
        assertEquals("abc123", response.token)
        assertEquals("def456", response.refreshToken)
        val supabaseUser = response.user
        assertNotNull(supabaseUser)
        assertEquals("u1", supabaseUser?.id)
        assertEquals("test@example.com", supabaseUser?.email)
        assertEquals("Test User", supabaseUser?.userMetadata?.fullName)
        assertEquals("aluno", supabaseUser?.userMetadata?.role)
    }

    @Test
    fun `User data class can be constructed directly`() {
        val user = User(
            id = "u1",
            email = "test@example.com",
            fullName = "Test User",
            role = "aluno",
            isActive = true
        )
        assertEquals("u1", user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.fullName)
        assertEquals("aluno", user.role)
        assertEquals(true, user.isActive)
    }

    @Test
    fun `Exercise data class can be constructed directly`() {
        val exercise = Exercise(
            id = "ex1",
            name = "Bench Press",
            description = "Chest exercise",
            muscleGroup = "peito",
            usesWeight = true,
            videoUrl = "http://example.com/video.mp4",
            mediaType = "video",
            mediaPath = "/videos/bench.mp4",
            instituto = "IFG",
            createdBy = "admin",
            updatedBy = "admin",
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-02T00:00:00Z"
        )
        assertEquals("ex1", exercise.id)
        assertEquals("Bench Press", exercise.name)
        assertEquals("peito", exercise.muscleGroup)
        assertEquals(true, exercise.usesWeight)
    }

    @Test
    fun `Exercise JSON deserialization maps medias correctly`() {
        val json = """
        {
          "id": "ex1",
          "name": "Bench Press",
          "description": "Chest exercise",
          "muscle_group": "peito",
          "uses_weight": true,
          "video_url": "http://example.com/video.mp4",
          "media_type": "video",
          "media_path": "/videos/bench.mp4",
          "medias": [
            {
              "id": "m1",
              "exercise_id": "ex1",
              "media_type": "video",
              "media_path": "/videos/bench.mp4",
              "video_url": "http://example.com/video.mp4"
            },
            {
              "id": "m2",
              "exercise_id": "ex1",
              "media_type": "image",
              "media_path": "/images/bench.jpg"
            }
          ]
        }
        """.trimIndent()
        val exercise = Gson().fromJson(json, Exercise::class.java)
        assertEquals("ex1", exercise.id)
        assertNotNull(exercise.medias)
        assertEquals(2, exercise.medias?.size)
        assertEquals("m1", exercise.medias?.get(0)?.id)
        assertEquals("ex1", exercise.medias?.get(0)?.exerciseId)
        assertEquals("video", exercise.medias?.get(0)?.mediaType)
        assertEquals("/videos/bench.mp4", exercise.medias?.get(0)?.mediaPath)
        assertEquals("http://example.com/video.mp4", exercise.medias?.get(0)?.videoUrl)
        assertEquals("m2", exercise.medias?.get(1)?.id)
        assertEquals("image", exercise.medias?.get(1)?.mediaType)
        assertEquals("/images/bench.jpg", exercise.medias?.get(1)?.mediaPath)
    }
}
