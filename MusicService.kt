package com.sujit.equalizermusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class MusicService : Service() {

    companion object {

        const val CHANNEL_ID = "sujit_music_channel"

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"

        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_PLAY -> {
                sendPlayerAction(ACTION_PLAY)
            }

            ACTION_PAUSE -> {
                sendPlayerAction(ACTION_PAUSE)
            }

            ACTION_NEXT -> {
                sendPlayerAction(ACTION_NEXT)
            }

            ACTION_PREVIOUS -> {
                sendPlayerAction(ACTION_PREVIOUS)
            }
        }

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        return START_STICKY
    }

    private fun sendPlayerAction(
        action: String
    ) {

        val intent =
            Intent(this, MainActivity::class.java)

        intent.action = action

        intent.addFlags(
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        startActivity(intent)
    }

    private fun createNotification(): Notification {

        val openIntent =
            Intent(this, MainActivity::class.java)

        val openPendingIntent =
            PendingIntent.getActivity(
                this,
                1,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        return if (Build.VERSION.SDK_INT >= 26) {

            Notification.Builder(
                this,
                CHANNEL_ID
            )
                .setContentTitle(
                    "SUJIT EQUALIZER MUSIC"
                )
                .setContentText(
                    "Music is playing"
                )
                .setSmallIcon(
                    android.R.drawable.ic_media_play
                )
                .setContentIntent(
                    openPendingIntent
                )
                .setOngoing(true)
                .build()

        } else {

            Notification.Builder(this)
                .setContentTitle(
                    "SUJIT EQUALIZER MUSIC"
                )
                .setContentText(
                    "Music is playing"
                )
                .setSmallIcon(
                    android.R.drawable.ic_media_play
                )
                .setContentIntent(
                    openPendingIntent
                )
                .setOngoing(true)
                .build()
        }
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= 26) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "SUJIT Music",
                    NotificationManager.IMPORTANCE_LOW
                )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
