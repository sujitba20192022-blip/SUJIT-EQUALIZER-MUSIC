package com.sujit.equalizermusic

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var musicList: LinearLayout

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
        root.setPadding(25, 30, 25, 20)
        root.setBackgroundColor(Color.rgb(16, 20, 24))

        val title = TextView(this)
        title.text = "SUJIT EQUALIZER MUSIC"
        title.textSize = 23f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        root.addView(title)

        val heading = TextView(this)
        heading.text = "\n🎵 MY MUSIC"
        heading.textSize = 20f
        heading.setTextColor(Color.WHITE)

        root.addView(heading)

        musicList = LinearLayout(this)
        musicList.orientation = LinearLayout.VERTICAL

        root.addView(musicList)

        setContentView(root)
    }

    private fun loadMusic() {

        musicList.removeAllViews()

        val projection = arrayOf(
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

        val titleIndex =
            cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)

        val artistIndex =
            cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)

        while (cursor.moveToNext()) {

            val songTitle = cursor.getString(titleIndex)
                ?: "Unknown Song"

            val artist = cursor.getString(artistIndex)
                ?: "Unknown Artist"

            val song = TextView(this)

            song.text = "🎵  $songTitle\n     $artist"
            song.textSize = 17f
            song.setTextColor(Color.WHITE)
            song.setPadding(15, 20, 15, 20)

            musicList.addView(song)
        }

        cursor.close()

        if (musicList.childCount == 0) {
            showMessage("Phone में कोई music file नहीं मिली")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadMusic()
        } else {
            showMessage("Music permission की जरूरत है")
        }
    }
}
