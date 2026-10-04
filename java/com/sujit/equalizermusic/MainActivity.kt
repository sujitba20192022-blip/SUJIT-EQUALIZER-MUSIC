package com.sujit.equalizermusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore

class MusicService : Service() {

    companion object {

        const val CHANNEL_ID = "sujit_music_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"

        const val EXTRA_SONG_ID = "EXTRA_SONG_ID"
        const val EXTRA_TITLE = "EXTRA_TITLE"

        var mediaPlayer: MediaPlayer? = null

        var currentTitle: String = "Nothing Playing"

        var isPlaying: Boolean = false
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

                val songId =
                    intent.getLongExtra(
                        EXTRA_SONG_ID,
                        -1L
                    )

                val title =
                    intent.getStringExtra(
                        EXTRA_TITLE
                    ) ?: "Unknown Song"

                if (songId != -1L) {
                    playSong(songId, title)
                } else {
                    mediaPlayer?.let {

                        if (it.isPlaying) {
                            it.pause()
                            isPlaying = false
                        } else {
                            it.start()
                            isPlaying = true
                        }

                        updateNotification()
                    }
                }
            }

            ACTION_PAUSE -> {

                mediaPlayer?.let {

                    if (it.isPlaying) {
                        it.pause()
                        isPlaying = false
                    }
                }

                updateNotification()
            }

            ACTION_NEXT -> {
                sendActionToActivity(ACTION_NEXT)
            }

            ACTION_PREVIOUS -> {
                sendActionToActivity(ACTION_PREVIOUS)
            }
        }

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        return START_STICKY
    }

    private fun playSong(
        songId: Long,
        title: String
    ) {

        try {

            mediaPlayer?.release()

            val uri =
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songId
                )

            mediaPlayer =
                MediaPlayer.create(
                    this,
                    uri
                )

            if (mediaPlayer == null) {
                return
            }

            currentTitle = title

            mediaPlayer?.start()

            isPlaying = true

            mediaPlayer?.setOnCompletionListener {

                sendActionToActivity(
                    ACTION_NEXT
                )
            }

            updateNotification()

        } catch (_: Exception) {
        }
    }

    private fun sendActionToActivity(
        action: String
    ) {

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        intent.action = action

        intent.addFlags(
            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        startActivity(intent)
    }

    private fun createNotification(): Notification {

        val openIntent =
            Intent(
                this,
                MainActivity::class.java
            )

        val openPendingIntent =
            PendingIntent.getActivity(
                this,
                10,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val previousIntent =
            Intent(
                this,
                MusicService::class.java
            )

        previousIntent.action =
            ACTION_PREVIOUS

        val previousPendingIntent =
            PendingIntent.getService(
                this,
                11,
                previousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val playPauseIntent =
            Intent(
                this,
                MusicService::class.java
            )

        playPauseIntent.action =
            if (isPlaying)
                ACTION_PAUSE
            else
                ACTION_PLAY

        val playPausePendingIntent =
            PendingIntent.getService(
                this,
                12,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val nextIntent =
            Intent(
                this,
                MusicService::class.java
            )

        nextIntent.action =
            ACTION_NEXT

        val nextPendingIntent =
            PendingIntent.getService(
                this,
                13,
                nextIntent,
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
                    currentTitle
                )
                .setSmallIcon(
                    android.R.drawable.ic_media_play
                )
                .setContentIntent(
                    openPendingIntent
                )
                .setOngoing(true)
                .addAction(
                    Notification.Action.Builder(
                        android.graphics.drawable.Icon.createWithResource(
                            this,
                            android.R.drawable.ic_media_previous
                        ),
                        "Previous",
                        previousPendingIntent
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        android.graphics.drawable.Icon.createWithResource(
                            this,
                            if (isPlaying)
                                android.R.drawable.ic_media_pause
                            else
                                android.R.drawable.ic_media_play
                        ),
                        if (isPlaying)
                            "Pause"
                        else
                            "Play",
                        playPausePendingIntent
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        android.graphics.drawable.Icon.createWithResource(
                            this,
                            android.R.drawable.ic_media_next
                        ),
                        "Next",
                        nextPendingIntent
                    ).build()
                )
                .build()

        } else {

            Notification.Builder(this)
                .setContentTitle(
                    "SUJIT EQUALIZER MUSIC"
                )
                .setContentText(
                    currentTitle
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

    private fun updateNotification() {

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            createNotification()
        )
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

    override fun onDestroy() {

        mediaPlayer?.release()
        mediaPlayer = null

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}
