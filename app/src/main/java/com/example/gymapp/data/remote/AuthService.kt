package com.example.gymapp.data.remote

import com.example.gymapp.domain.model.ApiResponse
import com.example.gymapp.domain.model.LoginRequest
import com.example.gymapp.domain.model.LoginResponse
import com.example.gymapp.domain.model.MeResponse
import com.example.gymapp.domain.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("auth/signin")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    @GET("auth/me")
    suspend fun getMe(): ApiResponse<MeResponse>
}

data class RefreshTokenRequest(val refresh_token: String)

data class RefreshTokenResponse(
    val access_token: String,
    val refresh_token: String?
)
