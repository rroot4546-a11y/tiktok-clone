package com.tiktokclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tiktokclone.navigation.TikTokNavHost
import com.tiktokclone.ui.common.components.TikTokTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TikTokTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TikTokTheme.colors.background
                ) {
                    TikTokNavHost()
                }
            }
        }
    }
}
