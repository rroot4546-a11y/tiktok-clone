package com.tiktokclone.data.repository

import com.tiktokclone.data.api.ApiService
import com.tiktokclone.data.models.*
import com.tiktokclone.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun getConversations(): Resource<List<Conversation>> {
        return try {
            val response = api.getConversations()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.conversations)
            } else {
                Resource.Error("Failed to load conversations")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getOrCreateConversation(userId: String): Resource<Conversation> {
        return try {
            val response = api.getOrCreateConversation(mapOf("userId" to userId))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["conversation"]!!)
            } else {
                Resource.Error("Failed to create conversation")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMessages(conversationId: String, page: Int = 1): Resource<List<Message>> {
        return try {
            val response = api.getMessages(conversationId, page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.messages)
            } else {
                Resource.Error("Failed to load messages")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun sendMessage(conversationId: String, content: String, type: String = "text"): Resource<Message> {
        return try {
            val response = api.sendMessage(
                mapOf("conversationId" to conversationId, "content" to content, "type" to type)
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!["message"]!!)
            } else {
                Resource.Error("Failed to send message")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun markAsRead(conversationId: String): Resource<Unit> {
        return try {
            api.markMessagesAsRead(conversationId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
