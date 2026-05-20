package com.tiktokclone.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tiktokclone.data.models.Video
import com.tiktokclone.utils.formatCount
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToUser: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val topTabs = listOf("Explore", "Following", "For You")
    var selectedTab by remember { mutableIntStateOf(2) }
    var showComments by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
        } else {
            // Empty state
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No videos yet",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload a video to get started",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                )
            }
        }

        // Top bar - TikTok style
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .align(Alignment.TopCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // LIVE button
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Outlined.LiveTv,
                        contentDescription = "LIVE",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }

                // Center tabs
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    topTabs.forEachIndexed { index, tab ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    selectedTab = index
                                    when (index) {
                                        1 -> viewModel.loadFollowingFeed()
                                        2 -> viewModel.loadFeed()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = tab,
                                color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.55f),
                                fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Normal,
                                fontSize = if (selectedTab == index) 17.sp else 15.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        blurRadius = 4f,
                                    )
                                ),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (selectedTab == index) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(2.5.dp))
                            }
                        }
                    }
                }

                // Search icon
                IconButton(onClick = onNavigateToSearch) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
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
                .padding(end = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // User avatar with + follow button
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable { onUserClick() },
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                ) {
                    AsyncImage(
                        model = video.user.avatar.ifEmpty { null },
                        contentDescription = video.user.username,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                // Red + follow button
                Box(
                    modifier = Modifier
                        .offset(y = 10.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFE2C55)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Follow",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
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

        // Bottom info overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp, end = 80.dp),
        ) {
            // Username
            Text(
                text = "@${video.user.username}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onUserClick() },
                style = TextStyle(
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 4f)
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Caption with hashtags
            if (video.caption.isNotEmpty()) {
                Text(
                    text = video.caption,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 4f)
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Hashtags row
            if (video.hashtags.isNotEmpty()) {
                Text(
                    text = video.hashtags.joinToString(" ") { "#$it" },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 4f)
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Music marquee
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${video.music.name} - ${video.music.artist.ifEmpty { video.user.username }}",
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 4f)
                    ),
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
        IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
        }
        Text(
            text = count,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(
                shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 4f)
            ),
        )
    }
}
