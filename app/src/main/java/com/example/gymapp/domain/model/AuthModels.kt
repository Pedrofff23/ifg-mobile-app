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
    val role: String,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("is_blocked") val isBlocked: Boolean = false,
    @SerializedName("instituto_id") val institutoId: String? = null,
    val instituto: String? = null,
    @SerializedName("profile_completed") val profileCompleted: Boolean = false,
    @SerializedName("activation_sent_at") val activationSentAt: String? = null,
    @SerializedName("current_weight_kg") val currentWeightKg: Double? = null,
    @SerializedName("height_cm") val heightCm: Double? = null,
    @SerializedName("injury_history") val injuryHistory: String? = null
)

data class User(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String?,
    val role: String,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("is_blocked") val isBlocked: Boolean = false,
    @SerializedName("instituto_id") val institutoId: String? = null,
    val instituto: String? = null,
    @SerializedName("profile_completed") val profileCompleted: Boolean = false,
    @SerializedName("activation_sent_at") val activationSentAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("current_weight_kg") val currentWeightKg: Double? = null,
    @SerializedName("height_cm") val heightCm: Double? = null,
    @SerializedName("injury_history") val injuryHistory: String? = null
)

data class CompleteProfileRequest(
    @SerializedName("instituto_id") val institutoId: String,
    @SerializedName("current_weight_kg") val currentWeightKg: Double? = null,
    @SerializedName("height_cm") val heightCm: Double? = null,
    @SerializedName("injury_history") val injuryHistory: String? = null
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?
)

data class ResendActivationRequest(
    @SerializedName("email") val email: String
)
