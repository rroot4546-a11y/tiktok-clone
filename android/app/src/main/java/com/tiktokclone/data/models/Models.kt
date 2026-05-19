package com.tiktokclone.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatar: String = "",
    val coverImage: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val likesCount: Int = 0,
    val videosCount: Int = 0,
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val isOnline: Boolean = false,
    val isFollowing: Boolean = false,
    val isBlocked: Boolean = false,
    val coins: Int = 0,
    val isPremium: Boolean = false,
)

data class Video(
    @SerializedName("_id") val id: String = "",
    val user: User = User(),
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val caption: String = "",
    val music: Music = Music(),
    val hashtags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Int = 0,
    val savesCount: Int = 0,
    val duration: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val allowComments: Boolean = true,
    val allowDuet: Boolean = true,
    val allowStitch: Boolean = true,
    val allowDownload: Boolean = true,
    val createdAt: String = "",
)

data class Music(
    val name: String = "Original Sound",
    val artist: String = "",
    val url: String = "",
    val duration: Int = 0,
)

data class Comment(
    @SerializedName("_id") val id: String = "",
    val user: User = User(),
    val video: String = "",
    val text: String = "",
    val parentComment: String? = null,
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val isLiked: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: String = "",
)

data class Conversation(
    @SerializedName("_id") val id: String = "",
    val participants: List<User> = emptyList(),
    val lastMessage: Message? = null,
    val otherUser: User? = null,
    val unreadCount: Int = 0,
    val lastMessageAt: String = "",
)

data class Message(
    @SerializedName("_id") val id: String = "",
    val conversation: String = "",
    val sender: User = User(),
    val type: String = "text",
    val content: String = "",
    val mediaUrl: String? = null,
    val isDeleted: Boolean = false,
    val createdAt: String = "",
)

data class Notification(
    @SerializedName("_id") val id: String = "",
    val sender: User = User(),
    val type: String = "",
    val video: VideoThumbnail? = null,
    val message: String? = null,
    val isRead: Boolean = false,
    val createdAt: String = "",
)

data class VideoThumbnail(
    @SerializedName("_id") val id: String = "",
    val thumbnailUrl: String = "",
    val caption: String = "",
)

data class Hashtag(
    @SerializedName("_id") val id: String = "",
    val name: String = "",
    val videosCount: Int = 0,
    val viewsCount: Int = 0,
    val isTrending: Boolean = false,
)

data class LiveStream(
    @SerializedName("_id") val id: String = "",
    val host: User = User(),
    val title: String = "",
    val thumbnailUrl: String = "",
    val status: String = "live",
    val viewersCount: Int = 0,
    val totalGiftValue: Int = 0,
)

// API Response wrappers
data class AuthResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)

data class VideoListResponse(
    val videos: List<Video>,
    val page: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
)

data class CommentListResponse(
    val comments: List<Comment>,
    val total: Int = 0,
    val hasMore: Boolean = false,
)

data class ConversationListResponse(
    val conversations: List<Conversation>,
)

data class MessageListResponse(
    val messages: List<Message>,
)

data class NotificationListResponse(
    val notifications: List<Notification>,
    val unreadCount: Int = 0,
)

data class SearchResponse(
    val users: List<User>? = null,
    val videos: List<Video>? = null,
    val hashtags: List<Hashtag>? = null,
)

data class SearchSuggestion(
    val type: String = "",
    val text: String = "",
    val count: Int = 0,
    val avatar: String? = null,
    val displayName: String? = null,
)

data class SuggestionsResponse(
    val suggestions: List<SearchSuggestion>,
)

data class LikeResponse(
    val isLiked: Boolean,
    val likesCount: Int,
)

data class FollowResponse(
    val isFollowing: Boolean,
    val followersCount: Int,
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class UserResponse(
    val user: User,
)

data class VideoResponse(
    val video: Video,
)

data class CommentResponse(
    val comment: Comment,
)

data class LiveStreamListResponse(
    val streams: List<LiveStream>,
)

data class LiveStreamResponse(
    val stream: LiveStream,
)

data class UnreadCountResponse(
    val unreadCount: Int,
)

data class MessageResponse(
    val message: String,
)
