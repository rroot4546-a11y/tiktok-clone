package com.tiktokclone.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tiktokclone.data.models.Notification
import com.tiktokclone.utils.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        TopAppBar(
            title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                if (uiState.unreadCount > 0) {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Mark all read", color = Color(0xFFFE2C55), fontSize = 13.sp)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
        )

        if (uiState.notifications.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications yet", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.notifications) { notification ->
                NotificationItem(notification = notification)
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val icon = when (notification.type) {
        "like" -> Icons.Default.Favorite
        "comment" -> Icons.Default.ChatBubble
        "follow" -> Icons.Default.PersonAdd
        "mention" -> Icons.Default.AlternateEmail
        "live" -> Icons.Default.LiveTv
        else -> Icons.Default.Notifications
    }

    val iconColor = when (notification.type) {
        "like" -> Color(0xFFFE2C55)
        "comment" -> Color(0xFF25F4EE)
        "follow" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    val actionText = when (notification.type) {
        "like" -> "liked your video"
        "comment" -> "commented: ${notification.message ?: ""}"
        "follow" -> "started following you"
        "mention" -> "mentioned you"
        "live" -> "is live now!"
        else -> notification.message ?: ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) Color(0xFF0A0A15) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = notification.sender.avatar.ifEmpty { null },
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2A2A)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                        append(notification.sender.username)
                    }
                    append(" ")
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                        append(actionText)
                    }
                },
                fontSize = 14.sp,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.createdAt.timeAgo(),
                color = Color.Gray,
                fontSize = 12.sp,
            )
        }

        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
    }
}
