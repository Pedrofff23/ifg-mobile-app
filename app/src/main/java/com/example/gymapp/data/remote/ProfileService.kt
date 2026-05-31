package com.example.gymapp.data.remote

import com.example.gymapp.domain.model.*
import retrofit2.http.*

interface ProfileService {

    @GET("profiles/{id}")
    suspend fun getProfile(
        @Path("id") id: String
    ): ApiResponse<AlunoProfile>

    @POST("profiles/me")
    suspend fun upsertProfile(
        @Body request: UpsertProfileRequest
    ): ApiResponse<AlunoProfile>

    @GET("profiles/{id}/measurements")
    suspend fun getMeasurements(
        @Path("id") id: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): PaginatedResponse<BodyMeasurement>

    @POST("profiles/me/measurements")
    suspend fun addMeasurement(
        @Body request: AddMeasurementRequest
    ): ApiResponse<BodyMeasurement>
}
