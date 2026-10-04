package com.sujit.equalizermusic

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var nowPlaying: TextView
    private lateinit var progressBar: SeekBar
    private lateinit var playButton: Button

    private val songIds = ArrayList<Long>()
    private val songTitles = ArrayList<String>()

    private var currentSong = -1

    private val handler = Handler(Looper.getMainLooper())

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

        nowPlaying = TextView(this)

        nowPlaying.text = "\n🎵 Nothing Playing"
        nowPlaying.textSize = 18f
        nowPlaying.setTextColor(Color.LTGRAY)
        nowPlaying.gravity = Gravity.CENTER

        root.addView(nowPlaying)

        progressBar = SeekBar(this)

        progressBar.max = 100
        progressBar.progress = 0

        root.addView(progressBar)

        progressBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    if (fromUser && mediaPlayer != null) {

                        val duration =
                            mediaPlayer!!.duration

                        mediaPlayer!!.seekTo(
                            duration * progress / 100
                        )
                    }
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        val buttons = LinearLayout(this)

        buttons.gravity = Gravity.CENTER

        val previous = Button(this)

        previous.text = "⏮"

        previous.setOnClickListener {

            playPrevious()
        }

        playButton = Button(this)

        playButton.text = "▶"

        playButton.setOnClickListener {

            togglePlay()
        }

        val next = Button(this)

        next.text = "⏭"

        next.setOnClickListener {

            playNext()
        }

        buttons.addView(previous)
        buttons.addView(playButton)
        buttons.addView(next)

        root.addView(buttons)

        val heading = TextView(this)

        heading.text = "\n🎵 MY MUSIC"

        heading.textSize = 20f
        heading.setTextColor(Color.WHITE)

        root.addView(heading)

        val listScroll = ScrollView(this)

        val musicList = LinearLayout(this)

        musicList.orientation =
            LinearLayout.VERTICAL

        musicList.id = 12345

        listScroll.addView(musicList)

        root.addView(
            listScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun getMusicList(): LinearLayout {

        return findViewById(12345)
    }

    private fun loadMusic() {

        val musicList = getMusicList()

        musicList.removeAllViews()

        songIds.clear()
        songTitles.clear()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE
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
            cursor.getColumnIndex(
                MediaStore.Audio.Media._ID
            )

        val titleIndex =
            cursor.getColumnIndex(
                MediaStore.Audio.Media.TITLE
            )

        while (cursor.moveToNext()) {

            val id =
                cursor.getLong(idIndex)

            val title =
                cursor.getString(titleIndex)
                    ?: "Unknown Song"

            songIds.add(id)
            songTitles.add(title)

            val song = TextView(this)

            song.text = "▶  $title"

            song.textSize = 17f

            song.setTextColor(Color.WHITE)

            song.setPadding(
                15,
                20,
                15,
                20
            )

            val position =
                songIds.size - 1

            song.setOnClickListener {

                playSong(position)
            }

            musicList.addView(song)
        }

        cursor.close()

        if (songIds.isEmpty()) {

            showMessage(
                "Phone में कोई music file नहीं मिली"
            )
        }
    }

    private fun playSong(position: Int) {

       
