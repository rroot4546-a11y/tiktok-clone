package com.tiktokclone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TikTokCloneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    "tiktok_clone_channel",
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    "messages_channel",
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    "live_channel",
                    "Live Streams",
                    NotificationManager.IMPORTANCE_HIGH
                ),
            )

            val manager = getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
