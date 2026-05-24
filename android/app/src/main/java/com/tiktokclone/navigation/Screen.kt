package com.tiktokclone.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Main : Screen("main")
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Upload : Screen("upload")
    object Inbox : Screen("inbox")
    object Profile : Screen("profile")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object VideoDetail : Screen("video_detail/{videoId}") {
        fun createRoute(videoId: String) = "video_detail/$videoId"
    }
    object Comments : Screen("comments/{videoId}") {
        fun createRoute(videoId: String) = "comments/$videoId"
    }
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Followers : Screen("followers/{userId}") {
        fun createRoute(userId: String) = "followers/$userId"
    }
    object Following : Screen("following/{userId}") {
        fun createRoute(userId: String) = "following/$userId"
    }
    object LiveStream : Screen("live/{streamId}") {
        fun createRoute(streamId: String) = "live/$streamId"
    }
    object Search : Screen("search")
    object Notifications : Screen("notifications")
    object AiVideo : Screen("ai_video")
    object HashtagVideos : Screen("hashtag/{tag}") {
        fun createRoute(tag: String) = "hashtag/$tag"
    }
}
