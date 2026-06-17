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
	suspend fun getUser(@Path("id") userId: String): ApiResponse<User>

	@PATCH("users/{id}")
	suspend fun updateProfile(
		@Path("id") userId: String,
		@Body request: UpdateUserRequest
	): ApiResponse<User>

	@PATCH("users/{id}/role")
	suspend fun updateRole(
		@Path("id") userId: String,
		@Body request: UpdateRoleRequest
	): ApiResponse<User>

	@PATCH("users/{id}/status")
	suspend fun updateStatus(
		@Path("id") userId: String,
		@Body request: UpdateStatusRequest
	): ApiResponse<User>

	@PATCH("users/{id}/block")
	suspend fun updateBlocked(
		@Path("id") userId: String,
		@Body request: UpdateBlockedRequest
	): ApiResponse<User>
}
