package com.tiktokclone.data.api

import com.tiktokclone.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleAuth(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/facebook")
    suspend fun facebookAuth(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/phone")
    suspend fun phoneAuth(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body body: Map<String, String>): Response<TokenResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<MessageResponse>

    // Videos
    @Multipart
    @POST("videos/upload")
    suspend fun uploadVideo(
        @Part video: MultipartBody.Part,
        @Part("caption") caption: RequestBody,
        @Part("duration") duration: RequestBody? = null,
        @Part("allowComments") allowComments: RequestBody? = null,
    ): Response<VideoResponse>

    @GET("videos/feed")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
    ): Response<VideoListResponse>

    @GET("videos/following")
    suspend fun getFollowingFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
    ): Response<VideoListResponse>

    @GET("videos/trending")
    suspend fun getTrendingVideos(@Query("limit") limit: Int = 20): Response<VideoListResponse>

    @GET("videos/{id}")
    suspend fun getVideo(@Path("id") id: String): Response<VideoResponse>

    @POST("videos/{id}/like")
    suspend fun likeVideo(@Path("id") id: String): Response<LikeResponse>

    @POST("videos/{id}/save")
    suspend fun saveVideo(@Path("id") id: String): Response<Map<String, Boolean>>

    @POST("videos/{id}/share")
    suspend fun shareVideo(@Path("id") id: String): Response<MessageResponse>

    @POST("videos/{id}/report")
    suspend fun reportVideo(
        @Path("id") id: String,
        @Body body: Map<String, String>,
    ): Response<MessageResponse>

    @DELETE("videos/{id}")
    suspend fun deleteVideo(@Path("id") id: String): Response<MessageResponse>

    @GET("videos/user/{userId}")
    suspend fun getUserVideos(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
    ): Response<VideoListResponse>

    @GET("videos/hashtag/{tag}")
    suspend fun getVideosByHashtag(
        @Path("tag") tag: String,
        @Query("page") page: Int = 1,
    ): Response<VideoListResponse>

    // Comments
    @POST("comments/video/{videoId}")
    suspend fun addComment(
        @Path("videoId") videoId: String,
        @Body body: Map<String, String?>,
    ): Response<CommentResponse>

    @GET("comments/video/{videoId}")
    suspend fun getComments(
        @Path("videoId") videoId: String,
        @Query("page") page: Int = 1,
        @Query("sort") sort: String = "recent",
    ): Response<CommentListResponse>

    @GET("comments/{commentId}/replies")
    suspend fun getReplies(
        @Path("commentId") commentId: String,
        @Query("page") page: Int = 1,
    ): Response<Map<String, List<Comment>>>

    @POST("comments/{commentId}/like")
    suspend fun likeComment(@Path("commentId") commentId: String): Response<LikeResponse>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: String): Response<MessageResponse>

    // Users
    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): Response<UserResponse>

    @GET("users/profile/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): Response<UserResponse>

    @PUT("users/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<UserResponse>

    @Multipart
    @PUT("users/avatar")
    suspend fun updateAvatar(@Part avatar: MultipartBody.Part): Response<Map<String, String>>

    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") id: String): Response<FollowResponse>

    @POST("users/{id}/block")
    suspend fun blockUser(@Path("id") id: String): Response<Map<String, Boolean>>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
    ): Response<Map<String, List<User>>>

    @GET("users/suggested")
    suspend fun getSuggestedUsers(@Query("limit") limit: Int = 10): Response<Map<String, List<User>>>

    @GET("users/{id}/followers")
    suspend fun getFollowers(@Path("id") id: String, @Query("page") page: Int = 1): Response<Map<String, List<User>>>

    @GET("users/{id}/following")
    suspend fun getFollowing(@Path("id") id: String, @Query("page") page: Int = 1): Response<Map<String, List<User>>>

    @GET("users/saved-videos")
    suspend fun getSavedVideos(@Query("page") page: Int = 1): Response<VideoListResponse>

    @GET("users/{id}/liked-videos")
    suspend fun getLikedVideos(@Path("id") id: String, @Query("page") page: Int = 1): Response<VideoListResponse>

    // Chat
    @GET("chat/conversations")
    suspend fun getConversations(): Response<ConversationListResponse>

    @POST("chat/conversations")
    suspend fun getOrCreateConversation(@Body body: Map<String, String>): Response<Map<String, Conversation>>

    @POST("chat/send")
    suspend fun sendMessage(@Body body: Map<String, String>): Response<Map<String, Message>>

    @GET("chat/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
    ): Response<MessageListResponse>

    @PUT("chat/{conversationId}/read")
    suspend fun markMessagesAsRead(@Path("conversationId") conversationId: String): Response<MessageResponse>

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(@Query("page") page: Int = 1): Response<NotificationListResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<MessageResponse>

    // Search
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
    ): Response<SearchResponse>

    @GET("search/trending")
    suspend fun getTrending(): Response<SearchResponse>

    @GET("search/suggestions")
    suspend fun getSearchSuggestions(@Query("q") query: String): Response<SuggestionsResponse>

    // Live
    @GET("live")
    suspend fun getLiveStreams(): Response<LiveStreamListResponse>

    @GET("live/{id}")
    suspend fun getLiveStream(@Path("id") id: String): Response<LiveStreamResponse>

    @POST("live/create")
    suspend fun createLiveStream(@Body body: Map<String, String>): Response<LiveStreamResponse>

    @POST("live/{id}/end")
    suspend fun endLiveStream(@Path("id") id: String): Response<LiveStreamResponse>
}
