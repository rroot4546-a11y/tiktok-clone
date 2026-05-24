package com.tiktokclone.data.repository

import com.tiktokclone.data.api.ApiService
import com.tiktokclone.data.local.TokenManager
import com.tiktokclone.data.models.AuthResponse
import com.tiktokclone.data.models.User
import com.tiktokclone.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
) {
    suspend fun register(username: String, email: String, password: String): Resource<AuthResponse> {
        return try {
            val response = api.register(
                mapOf("username" to username, "email" to email, "password" to password)
            )
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveAuth(authResponse)
                Resource.Success(authResponse)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return try {
            val response = api.login(mapOf("email" to email, "password" to password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveAuth(authResponse)
                Resource.Success(authResponse)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun googleAuth(
        idToken: String,
        email: String = "",
        displayName: String = "",
        photoUrl: String = "",
        googleId: String = "",
    ): Resource<AuthResponse> {
        return try {
            val body = mutableMapOf("idToken" to idToken)
            if (email.isNotBlank()) body["email"] = email
            if (displayName.isNotBlank()) body["displayName"] = displayName
            if (photoUrl.isNotBlank()) body["photoUrl"] = photoUrl
            if (googleId.isNotBlank()) body["googleId"] = googleId

            val response = api.googleAuth(body)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveAuth(authResponse)
                Resource.Success(authResponse)
            } else {
                Resource.Error("Google authentication failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMe(): Resource<User> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.user)
            } else {
                Resource.Error("Failed to get user")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout(): Resource<Unit> {
        return try {
            api.logout()
            tokenManager.clearAll()
            Resource.Success(Unit)
        } catch (e: Exception) {
            tokenManager.clearAll()
            Resource.Success(Unit)
        }
    }

    suspend fun forgotPassword(email: String): Resource<String> {
        return try {
            val response = api.forgotPassword(mapOf("email" to email))
            if (response.isSuccessful) {
                Resource.Success("Reset link sent")
            } else {
                Resource.Error("Failed to send reset link")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    private suspend fun saveAuth(authResponse: AuthResponse) {
        tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
        tokenManager.saveUserInfo(authResponse.user.id, authResponse.user.username)
    }
}
