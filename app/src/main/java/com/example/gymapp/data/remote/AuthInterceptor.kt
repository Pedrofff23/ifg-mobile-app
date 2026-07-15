package com.example.gymapp.data.remote

import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.domain.model.RefreshTokenRequest
import com.example.gymapp.domain.model.RefreshTokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthInterceptor that attaches the JWT access token to requests.
 * On 401 responses, attempts to refresh the token once and retries the request.
 *
 * IMPORTANT: All runBlocking calls use Dispatchers.IO to avoid deadlocking
 * with DataStore's main-thread dispatcher. DataStore.edit() and .first()
 * internally use the DataStore scope which can contend for the main thread
 * if called from a runBlocking without an explicit IO dispatcher override.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authServiceLazy: dagger.Lazy<AuthService>
) : Interceptor {

    @Volatile
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Read token on IO dispatcher to avoid main-thread deadlock with DataStore
        val token = runCatching {
            runBlocking(Dispatchers.IO) { tokenManager.getAccessTokenSync() }
        }.getOrNull()

        val request = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        // If 401, try to refresh the token once
        if (response.code == 401 && !token.isNullOrEmpty()) {
            response.close()

            synchronized(this) {
                // Double-check: another thread might have already refreshed
                val currentToken = runCatching {
                    runBlocking(Dispatchers.IO) { tokenManager.getAccessTokenSync() }
                }.getOrNull()

                if (currentToken != null && currentToken != token) {
                    // Token was refreshed by another thread, retry with new token
                    val retryRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $currentToken")
                        .build()
                    return chain.proceed(retryRequest)
                }

                if (!isRefreshing) {
                    isRefreshing = true
                    try {
                        val refreshToken = runCatching {
                            runBlocking(Dispatchers.IO) { tokenManager.refreshToken.first() }
                        }.getOrNull()

                        if (refreshToken.isNullOrEmpty()) {
                            // No refresh token available, clear session
                            runBlocking(Dispatchers.IO) { tokenManager.clearSession() }
                            return response
                        }

                        val refreshResp = runBlocking(Dispatchers.IO) {
                            authServiceLazy.get().refreshToken(RefreshTokenRequest(refreshToken))
                        }

                        // Save new tokens
                        runBlocking(Dispatchers.IO) {
                            tokenManager.updateAccessToken(refreshResp.accessToken)
                            if (refreshResp.refreshToken != null) {
                                tokenManager.saveRefreshToken(refreshResp.refreshToken)
                            }
                        }

                        isRefreshing = false

                        // Retry original request with new access token
                        val retryRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer ${refreshResp.accessToken}")
                            .build()
                        return chain.proceed(retryRequest)
                    } catch (e: Exception) {
                        isRefreshing = false
                        // Refresh failed, clear session
                        runCatching { runBlocking(Dispatchers.IO) { tokenManager.clearSession() } }
                        return response
                    }
                }
            }
        }

        return response
    }
}
