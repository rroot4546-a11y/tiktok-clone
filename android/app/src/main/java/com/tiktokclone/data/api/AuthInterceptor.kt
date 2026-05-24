package com.tiktokclone.data.api

import com.tiktokclone.BuildConfig
import com.tiktokclone.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = runBlocking { tokenManager.getAccessToken() }

        val builder = request.newBuilder()

        // Add tunnel basic auth
        val tunnelAuth = BuildConfig.TUNNEL_AUTH
        if (tunnelAuth.isNotBlank() && tunnelAuth.contains(":")) {
            val parts = tunnelAuth.split(":", limit = 2)
            builder.addHeader("Authorization", Credentials.basic(parts[0], parts[1]))
        }

        // Add API bearer token
        if (token != null) {
            builder.addHeader("X-Auth-Token", token)
        }

        val authenticatedRequest = builder.build()
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
