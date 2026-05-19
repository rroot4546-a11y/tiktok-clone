package com.tiktokclone.data.repository

import com.tiktokclone.data.api.ApiService
import com.tiktokclone.data.models.*
import com.tiktokclone.utils.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun getFeed(page: Int = 1): Resource<VideoListResponse> {
        return try {
            val response = api.getFeed(page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load feed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getFollowingFeed(page: Int = 1): Resource<VideoListResponse> {
        return try {
            val response = api.getFollowingFeed(page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load feed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getVideo(id: String): Resource<Video> {
        return try {
            val response = api.getVideo(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.video)
            } else {
                Resource.Error("Video not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun likeVideo(id: String): Resource<LikeResponse> {
        return try {
            val response = api.likeVideo(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to like video")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun saveVideo(id: String): Resource<Boolean> {
        return try {
            val response = api.saveVideo(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["isSaved"] ?: false)
            } else {
                Resource.Error("Failed to save video")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun shareVideo(id: String): Resource<Unit> {
        return try {
            api.shareVideo(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun uploadVideo(file: File, caption: String): Resource<Video> {
        return try {
            val videoBody = file.asRequestBody("video/*".toMediaTypeOrNull())
            val videoPart = MultipartBody.Part.createFormData("video", file.name, videoBody)
            val captionBody = caption.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadVideo(videoPart, captionBody)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.video)
            } else {
                Resource.Error("Upload failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteVideo(id: String): Resource<Unit> {
        return try {
            val response = api.deleteVideo(id)
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Failed to delete video")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getUserVideos(userId: String, page: Int = 1): Resource<VideoListResponse> {
        return try {
            val response = api.getUserVideos(userId, page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load videos")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getTrendingVideos(): Resource<VideoListResponse> {
        return try {
            val response = api.getTrendingVideos()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load trending")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getComments(videoId: String, page: Int = 1): Resource<CommentListResponse> {
        return try {
            val response = api.getComments(videoId, page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load comments")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun addComment(videoId: String, text: String, parentComment: String? = null): Resource<Comment> {
        return try {
            val body = mutableMapOf<String, String?>("text" to text)
            if (parentComment != null) body["parentComment"] = parentComment

            val response = api.addComment(videoId, body)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.comment)
            } else {
                Resource.Error("Failed to add comment")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun likeComment(commentId: String): Resource<LikeResponse> {
        return try {
            val response = api.likeComment(commentId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to like comment")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun reportVideo(id: String, reason: String, description: String = ""): Resource<Unit> {
        return try {
            val response = api.reportVideo(id, mapOf("reason" to reason, "description" to description))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Failed to report")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
