package com.tiktokclone.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tiktokclone.data.models.Video
import com.tiktokclone.utils.formatCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onNavigateToUser: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A))
                .clickable { onNavigateToSearch() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search", color = Color.Gray, fontSize = 15.sp)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Trending hashtags
            item {
                Text(
                    text = "Trending",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp),
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.trendingHashtags) { hashtag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text("#${hashtag.name}", color = Color.White) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF2A2A2A),
                            ),
                        )
                    }
                }
            }

            // Suggested users
            if (uiState.suggestedUsers.isNotEmpty()) {
                item {
                    Text(
                        text = "Suggested Accounts",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.suggestedUsers) { user ->
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable { onNavigateToUser(user.id) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    AsyncImage(
                                        model = user.avatar.ifEmpty { null },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2A2A2A)),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = user.username,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${user.followersCount.formatCount()} followers",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55)),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                    ) {
                                        Text("Follow", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Trending videos grid
            item {
                Text(
                    text = "Trending Videos",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp),
                )
            }

            // Videos grid embedded in LazyColumn
            val videos = uiState.trendingVideos
            val rows = videos.chunked(2)
            items(rows) { rowVideos ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    rowVideos.forEach { video ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
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
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(video.viewsCount.formatCount(), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    if (rowVideos.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
