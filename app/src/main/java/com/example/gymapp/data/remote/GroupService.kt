package com.example.gymapp.data.remote

import com.example.gymapp.domain.model.*
import retrofit2.http.*
import retrofit2.Response

interface GroupService {

	@GET("groups")
	suspend fun getGroups(
		@Query("limit") limit: Int? = null,
		@Query("offset") offset: Int? = null,
		@Query("__with_users") withUsers: Boolean? = null,
		@Query("__with") with: String? = null
	): PaginatedResponse<StudentGroup>

	@GET("groups/{id}")
	suspend fun getGroup(
		@Path("id") id: String,
		@Query("__with") with: String? = null
	): ApiResponse<StudentGroup>

	@POST("groups")
	suspend fun createGroup(
		@Body request: CreateGroupRequest
	): ApiResponse<StudentGroup>

	@PUT("groups/{id}")
	suspend fun updateGroup(
		@Path("id") id: String,
		@Body request: CreateGroupRequest
	): ApiResponse<StudentGroup>

	@DELETE("groups/{id}")
	suspend fun deleteGroup(
		@Path("id") id: String
	): Response<Unit>

	@POST("groups/{id}/members")
	suspend fun addMember(
		@Path("id") groupId: String,
		@Body request: AddGroupMemberRequest
	): Response<Unit>

	@DELETE("groups/{id}/members/{userId}")
	suspend fun removeMember(
		@Path("id") groupId: String,
		@Path("userId") userId: String
	): Response<Unit>
}
