package com.tiktokclone.data.api

import com.tiktokclone.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = runBlocking { tokenManager.getAccessToken() }

        val authenticatedRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        val response = chain.proceed(authenticatedRequest)

        if (response.code == 401) {
            val refreshToken = runBlocking { tokenManager.getRefreshToken() }
            if (refreshToken != null) {
                // Token refresh logic handled by repository
            }
        }

        return response
    }
}
