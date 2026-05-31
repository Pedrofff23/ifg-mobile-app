package com.example.gymapp.data.remote

import com.example.gymapp.domain.model.*
import retrofit2.http.*

interface UserService {

    @GET("users")
    suspend fun getUsers(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): PaginatedResponse<User>

    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: String
    ): ApiResponse<User>

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): ApiResponse<User>

    @PATCH("users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: String,
        @Body request: UpdateRoleRequest
    ): ApiResponse<User>

    @PATCH("users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequest
    ): ApiResponse<User>
}
