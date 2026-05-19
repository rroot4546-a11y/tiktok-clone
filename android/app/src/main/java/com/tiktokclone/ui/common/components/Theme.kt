package com.tiktokclone.ui.common.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class TikTokColors(
    val background: Color = Color(0xFF000000),
    val surface: Color = Color(0xFF161823),
    val surfaceVariant: Color = Color(0xFF1E1E2E),
    val primary: Color = Color(0xFFFE2C55),
    val primaryBlue: Color = Color(0xFF25F4EE),
    val onBackground: Color = Color.White,
    val onSurface: Color = Color.White,
    val onSurfaceVariant: Color = Color(0xFF8A8B91),
    val divider: Color = Color(0xFF2F2F2F),
    val error: Color = Color(0xFFFF4444),
    val success: Color = Color(0xFF4CAF50),
    val likeRed: Color = Color(0xFFFE2C55),
    val commentBlue: Color = Color(0xFF25F4EE),
    val cardBackground: Color = Color(0xFF121212),
    val searchBar: Color = Color(0xFF2A2A2A),
)

val LocalTikTokColors = staticCompositionLocalOf { TikTokColors() }

object TikTokTheme {
    val colors: TikTokColors
        @Composable
        get() = LocalTikTokColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFE2C55),
    secondary = Color(0xFF25F4EE),
    background = Color(0xFF000000),
    surface = Color(0xFF161823),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun TikTokTheme(
    content: @Composable () -> Unit
) {
    val tiktokColors = TikTokColors()

    CompositionLocalProvider(
        LocalTikTokColors provides tiktokColors
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography(
                headlineLarge = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White,
                ),
                headlineMedium = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                ),
                titleLarge = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.White,
                ),
                titleMedium = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.White,
                ),
                bodyLarge = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                ),
                bodyMedium = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White,
                ),
                bodySmall = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF8A8B91),
                ),
                labelLarge = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                ),
            ),
            content = content,
        )
    }
}
