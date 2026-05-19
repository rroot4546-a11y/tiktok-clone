package com.tiktokclone.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tiktokclone.data.models.Video
import com.tiktokclone.utils.formatCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    userId: String? = null,
    isCurrentUser: Boolean = false,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToEditProfile: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToChat: ((String) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(userId) {
        if (isCurrentUser) viewModel.loadCurrentUser()
        else userId?.let { viewModel.loadUser(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        TopAppBar(
            title = {
                Text(
                    uiState.user?.username ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                if (!isCurrentUser) {
                    IconButton(onClick = { onNavigateBack?.invoke() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            },
            actions = {
                if (isCurrentUser) {
                    IconButton(onClick = { onLogout?.invoke() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
        )

        uiState.user?.let { user ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Avatar
                AsyncImage(
                    model = user.avatar.ifEmpty { null },
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A)),
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Display name
                Text(
                    text = "@${user.username}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem(count = user.followingCount.formatCount(), label = "Following")
                    StatItem(count = user.followersCount.formatCount(), label = "Followers")
                    StatItem(count = user.likesCount.formatCount(), label = "Likes")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                if (isCurrentUser) {
                    Button(
                        onClick = { onNavigateToEditProfile?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Edit Profile", color = Color.White, fontSize = 14.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { userId?.let { viewModel.toggleFollow(it) } },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isFollowing) Color(0xFF2A2A2A) else Color(0xFFFE2C55)
                            ),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                if (user.isFollowing) "Following" else "Follow",
                                color = Color.White,
                                fontSize = 14.sp,
                            )
                        }

                        OutlinedButton(
                            onClick = { /* Message */ },
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Icon(Icons.Default.Mail, contentDescription = "Message", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Bio
                if (user.bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.bio,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = {
                    TabRowDefaults.Indicator(
                        color = Color.White,
                    )
                },
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Videos") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Liked") },
                )
            }

            // Video grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                items(uiState.videos) { video ->
                    VideoGridItem(video = video)
                }
            }
        } ?: run {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun VideoGridItem(video: Video) {
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .background(Color(0xFF1E1E2E)),
    ) {
        if (video.thumbnailUrl.isNotEmpty()) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = video.viewsCount.formatCount(), color = Color.White, fontSize = 12.sp)
        }
    }
}
