package com.tiktokclone.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tiktokclone.data.models.Comment
import com.tiktokclone.utils.formatCount
import com.tiktokclone.utils.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    videoId: String,
    viewModel: CommentsViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(videoId) {
        viewModel.loadComments(videoId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161823),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) },
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.7f)) {
            Text(
                text = "${uiState.total} Comments",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp),
            )

            Divider(color = Color.Gray.copy(alpha = 0.3f))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(uiState.comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onLike = { viewModel.likeComment(comment.id) },
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.3f))

            // Comment input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Add a comment...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFE2C55),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFFE2C55),
                    ),
                    shape = RoundedCornerShape(20.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(videoId, commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank(),
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (commentText.isNotBlank()) Color(0xFFFE2C55) else Color.Gray,
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onLike: () -> Unit,
) {
    var isLiked by remember(comment.isLiked) { mutableStateOf(comment.isLiked) }
    var likesCount by remember(comment.likesCount) { mutableIntStateOf(comment.likesCount) }

    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = comment.user.avatar.ifEmpty { null },
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.user.username,
                color = Color.Gray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.text,
                color = Color.White,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.createdAt.timeAgo(),
                    color = Color.Gray,
                    fontSize = 12.sp,
                )
                if (comment.repliesCount > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "View ${comment.repliesCount} replies",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = {
                    isLiked = !isLiked
                    likesCount += if (isLiked) 1 else -1
                    onLike()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFFE2C55) else Color.Gray,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = likesCount.formatCount(),
                color = Color.Gray,
                fontSize = 11.sp,
            )
        }
    }
}
