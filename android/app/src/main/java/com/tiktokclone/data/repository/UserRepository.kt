package com.tiktokclone.data.repository

import com.tiktokclone.data.api.ApiService
import com.tiktokclone.data.models.*
import com.tiktokclone.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun getProfile(id: String): Resource<User> {
        return try {
            val response = api.getUserProfile(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.user)
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getProfileByUsername(username: String): Resource<User> {
        return try {
            val response = api.getUserByUsername(username)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.user)
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateProfile(updates: Map<String, String>): Resource<User> {
        return try {
            val response = api.updateProfile(updates)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.user)
            } else {
                Resource.Error("Failed to update profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun followUser(id: String): Resource<FollowResponse> {
        return try {
            val response = api.followUser(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to follow")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun blockUser(id: String): Resource<Boolean> {
        return try {
            val response = api.blockUser(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["isBlocked"] ?: false)
            } else {
                Resource.Error("Failed to block user")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun searchUsers(query: String, page: Int = 1): Resource<List<User>> {
        return try {
            val response = api.searchUsers(query, page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["users"] ?: emptyList())
            } else {
                Resource.Error("Search failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSuggestedUsers(): Resource<List<User>> {
        return try {
            val response = api.getSuggestedUsers()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["users"] ?: emptyList())
            } else {
                Resource.Error("Failed to load suggestions")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getFollowers(userId: String, page: Int = 1): Resource<List<User>> {
        return try {
            val response = api.getFollowers(userId, page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["followers"] ?: emptyList())
            } else {
                Resource.Error("Failed to load followers")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getFollowing(userId: String, page: Int = 1): Resource<List<User>> {
        return try {
            val response = api.getFollowing(userId, page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["following"] ?: emptyList())
            } else {
                Resource.Error("Failed to load following")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun search(query: String, type: String? = null, page: Int = 1): Resource<SearchResponse> {
        return try {
            val response = api.search(query, type, page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Search failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getNotifications(page: Int = 1): Resource<NotificationListResponse> {
        return try {
            val response = api.getNotifications(page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load notifications")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun markAllNotificationsAsRead(): Resource<Unit> {
        return try {
            api.markAllNotificationsAsRead()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getSavedVideos(page: Int = 1): Resource<VideoListResponse> {
        return try {
            val response = api.getSavedVideos(page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load saved videos")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
