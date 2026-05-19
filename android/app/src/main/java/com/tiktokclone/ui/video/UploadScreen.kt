package com.tiktokclone.ui.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onUploadSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var caption by remember { mutableStateOf("") }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var allowComments by remember { mutableStateOf(true) }
    var allowDuet by remember { mutableStateOf(true) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedVideoUri = uri }

    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) onUploadSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        TopAppBar(
            title = { Text("New Post", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // Video selection area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E2E))
                    .border(2.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { videoLauncher.launch("video/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (selectedVideoUri != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Video selected", color = Color.White, fontSize = 16.sp)
                        Text("Tap to change", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = Color(0xFFFE2C55),
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select Video", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Tap to choose from gallery", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Record button
            OutlinedButton(
                onClick = { /* Camera capture */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Video")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caption
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Caption") },
                placeholder = { Text("Describe your video...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFE2C55),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFFE2C55),
                    focusedLabelColor = Color(0xFFFE2C55),
                    unfocusedLabelColor = Color.Gray,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hashtags hint
            Text(
                text = "Add hashtags with # to reach more people",
                color = Color.Gray,
                fontSize = 12.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Allow Comments", color = Color.White, fontSize = 14.sp)
                Switch(
                    checked = allowComments,
                    onCheckedChange = { allowComments = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFFE2C55)),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Allow Duet", color = Color.White, fontSize = 14.sp)
                Switch(
                    checked = allowDuet,
                    onCheckedChange = { allowDuet = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFFE2C55)),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Post button
            Button(
                onClick = {
                    selectedVideoUri?.let { uri ->
                        viewModel.uploadVideo(uri, caption)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55)),
                shape = RoundedCornerShape(4.dp),
                enabled = selectedVideoUri != null && !uiState.isUploading,
            ) {
                if (uiState.isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading ${(uiState.uploadProgress * 100).toInt()}%")
                } else {
                    Text("Post", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
