package com.example.gymapp.domain.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    @SerializedName("password") val passwordHash: String
)

data class LoginResponse(
    @SerializedName("access_token") val token: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    val user: SupabaseUser?
)

data class RegisterRequest(
    val email: String,
    @SerializedName("password") val passwordHash: String,
    @SerializedName("full_name") val fullName: String
)

data class SupabaseUser(
    val id: String?,
    val email: String?,
    @SerializedName("user_metadata") val userMetadata: UserMetadata?
)

data class UserMetadata(
    @SerializedName("full_name") val fullName: String?,
    val role: String?
)

data class MeResponse(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String?,
    val role: String
)

data class User(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String?,
    val role: String,
    @SerializedName("is_active") val isActive: Boolean = true,
    val instituto: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
