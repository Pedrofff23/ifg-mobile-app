package com.example.gymapp.data.remote

import com.example.gymapp.domain.model.ApiResponse
import com.example.gymapp.domain.model.CompleteProfileRequest
import com.example.gymapp.domain.model.LoginRequest
import com.example.gymapp.domain.model.LoginResponse
import com.example.gymapp.domain.model.MeResponse
import com.example.gymapp.domain.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("auth/signin")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    @GET("auth/me")
    suspend fun getMe(): ApiResponse<MeResponse>

    @GET("auth/status")
    suspend fun getStatus(): ApiResponse<MeResponse>

    @POST("auth/complete-profile")
    suspend fun completeProfile(@Body request: CompleteProfileRequest): ApiResponse<MeResponse>

    @POST("auth/resend-activation")
    suspend fun resendActivation(@Body request: ResendActivationRequest): ApiResponse<Unit>
}

data class RefreshTokenRequest(val refresh_token: String)

data class RefreshTokenResponse(
    val access_token: String,
    val refresh_token: String?
)

data class ResendActivationRequest(val email: String)
