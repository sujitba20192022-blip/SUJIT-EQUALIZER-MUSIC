package com.sujit.equalizermusic

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var musicList: LinearLayout
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createScreen()

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    100
                )
            } else {
                loadMusic()
            }
        } else {
            loadMusic()
        }
    }

    private fun createScreen() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(20, 30, 20, 20)
        root.setBackgroundColor(Color.rgb(16, 20, 24))

        val title = TextView(this)
        title.text = "SUJIT EQUALIZER MUSIC"
        title.textSize = 23f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        root.addView(title)

        val nowPlaying = TextView(this)
        nowPlaying.text = "\nNow Playing: Nothing"
        nowPlaying.textSize = 18f
        nowPlaying.setTextColor(Color.LTGRAY)
        nowPlaying.gravity = Gravity.CENTER

        root.addView(nowPlaying)

        val heading = TextView(this)
        heading.text = "\n🎵 MY MUSIC"
        heading.textSize = 20f
        heading.setTextColor(Color.WHITE)

        root.addView(heading)

        musicList = LinearLayout(this)
        musicList.orientation = LinearLayout.VERTICAL

        root.addView(
            musicList,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun loadMusic() {

        musicList.removeAllViews()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        if (cursor == null) {
            showMessage("Music नहीं मिली")
            return
        }

        val idIndex =
            cursor.getColumnIndex(MediaStore.Audio.Media._ID)

        val titleIndex =
            cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)

        val artistIndex =
            cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)

        while (cursor.moveToNext()) {

            val id = cursor.getLong(idIndex)

            val songTitle =
                cursor.getString(titleIndex) ?: "Unknown Song"

            val artist =
                cursor.getString(artistIndex) ?: "Unknown Artist"

            val song = TextView(this)

            song.text = "▶  $songTitle\n    $artist"
            song.textSize = 17f
            song.setTextColor(Color.WHITE)
            song.setPadding(15, 18, 15, 18)

            song.setOnClickListener {
                playSong(id)
            }

            musicList.addView(song)
        }

        cursor.close()

        if (musicList.childCount == 0) {
            showMessage("Phone में कोई music file नहीं मिली")
        }
    }

    private fun playSong(id: Long) {

        try {

            mediaPlayer?.release()

            val uri = android.content.ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id
            )

            mediaPlayer = MediaPlayer.create(this, uri)

            mediaPlayer?.start()

        } catch (e: Exception) {

            showMessage("Song play नहीं हो पाया")
        }
    }

    private fun showMessage(message: String) {

        val text = TextView(this)

        text.text = message
        text.textSize = 17f
        text.setTextColor(Color.LTGRAY)
        text.gravity = Gravity.CENTER
        text.setPadding(10, 40, 10, 40)

        musicList.addView(text)
    }

    override fun onDestroy() {

        mediaPlayer?.release()
        mediaPlayer = null

        super.onDestroy()
    }
}
