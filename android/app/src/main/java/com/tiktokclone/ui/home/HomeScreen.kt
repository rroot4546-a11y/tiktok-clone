package com.tiktokclone.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tiktokclone.data.models.Video
import com.tiktokclone.utils.formatCount

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToUser: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showComments by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (uiState.videos.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { uiState.videos.size })

            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage >= uiState.videos.size - 3) {
                    viewModel.loadMore()
                }
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val video = uiState.videos[page]
                VideoItem(
                    video = video,
                    isCurrentPage = pagerState.currentPage == page,
                    onLike = { viewModel.likeVideo(video.id) },
                    onComment = { showComments = video.id },
                    onShare = { viewModel.shareVideo(video.id) },
                    onSave = { viewModel.saveVideo(video.id) },
                    onUserClick = { onNavigateToUser(video.user.id) },
                )
            }
        } else if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Following",
                color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.6f),
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 17.sp,
                modifier = Modifier.clickable { selectedTab = 0; viewModel.loadFollowingFeed() },
            )

            Text(
                text = "  |  ",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 17.sp,
            )

            Text(
                text = "For You",
                color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.6f),
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                fontSize = 17.sp,
                modifier = Modifier.clickable { selectedTab = 1; viewModel.loadFeed() },
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onNavigateToSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        }

        showComments?.let { videoId ->
            CommentsSheet(
                videoId = videoId,
                onDismiss = { showComments = null },
            )
        }
    }
}

@Composable
fun VideoItem(
    video: Video,
    isCurrentPage: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onUserClick: () -> Unit,
) {
    var isLiked by remember(video.isLiked) { mutableStateOf(video.isLiked) }
    var likesCount by remember(video.likesCount) { mutableIntStateOf(video.likesCount) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Video placeholder with thumbnail
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (video.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp),
            )
        }

        // Double tap like animation
        AnimatedVisibility(
            visible = showDoubleTapHeart,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFFE2C55),
                modifier = Modifier.size(100.dp),
            )
        }

        // Right side action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onUserClick() },
            ) {
                AsyncImage(
                    model = video.user.avatar.ifEmpty { null },
                    contentDescription = video.user.username,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            // Like
            ActionButton(
                icon = Icons.Default.Favorite,
                count = likesCount.formatCount(),
                tint = if (isLiked) Color(0xFFFE2C55) else Color.White,
                onClick = {
                    isLiked = !isLiked
                    likesCount += if (isLiked) 1 else -1
                    onLike()
                },
            )

            // Comment
            ActionButton(
                icon = Icons.Default.ChatBubble,
                count = video.commentsCount.formatCount(),
                onClick = onComment,
            )

            // Share
            ActionButton(
                icon = Icons.Default.Share,
                count = video.sharesCount.formatCount(),
                onClick = onShare,
            )

            // Save
            ActionButton(
                icon = if (video.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                count = video.savesCount.formatCount(),
                onClick = onSave,
            )

            // Music disc
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .border(2.dp, Color.Gray, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 80.dp, end = 80.dp),
        ) {
            // Username
            Text(
                text = "@${video.user.username}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onUserClick() },
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Caption
            if (video.caption.isNotEmpty()) {
                Text(
                    text = video.caption,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Music
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${video.music.name} - ${video.music.artist.ifEmpty { video.user.username }}",
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
        }
        Text(text = count, color = Color.White, fontSize = 12.sp)
    }
}
