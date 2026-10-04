package com.sujit.equalizermusic

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private lateinit var nowPlaying: TextView
    private lateinit var progressBar: SeekBar
    private lateinit var playButton: Button
    private lateinit var equalizerLayout: LinearLayout

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
        root.setPadding(20, 25, 20, 15)
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

        root.addView(progressBar)

        progressBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser && mediaPlayer != null) {
                        mediaPlayer!!.seekTo(
                            mediaPlayer!!.duration *
                                    progress / 100
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

        val eqTitle = TextView(this)

        eqTitle.text = "🎚️ EQUALIZER"
        eqTitle.textSize = 20f
        eqTitle.setTextColor(Color.WHITE)
        eqTitle.gravity = Gravity.CENTER

        root.addView(eqTitle)

        val eqScroll = HorizontalScrollView(this)

        equalizerLayout = LinearLayout(this)

        equalizerLayout.orientation =
            LinearLayout.HORIZONTAL

        equalizerLayout.gravity =
            Gravity.CENTER

        eqScroll.addView(equalizerLayout)

        root.addView(
            eqScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                230
            )
        )

        val bassButton = Button(this)

        bassButton.text = "🔊 BASS BOOST"

        bassButton.setOnClickListener {
            toggleBassBoost(bassButton)
        }

        root.addView(bassButton)

        val heading = TextView(this)

        heading.text = "🎵 MY MUSIC"
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

    private fun setupEqualizer() {

        val player = mediaPlayer ?: return

        try {

            equalizer?.release()

            equalizer =
                Equalizer(0, player.audioSessionId)

            equalizer?.enabled = true

            createEqualizerSliders()

            bassBoost?.release()

            bassBoost =
                BassBoost(0, player.audioSessionId)

            bassBoost?.enabled = true

        } catch (e: Exception) {

            // Audio effects unavailable
        }
    }

    private fun createEqualizerSliders() {

        equalizerLayout.removeAllViews()

        val eq = equalizer ?: return

        val numberOfBands =
            eq.numberOfBands.toInt()

        val lower =
            eq.bandLevelRange[0].toInt()

        val upper =
            eq.bandLevelRange[1].toInt()

        for (i in 0 until numberOfBands) {

            val band = i.toShort()

            val column = LinearLayout(this)

            column.orientation =
                LinearLayout.VERTICAL

            column.gravity =
                Gravity.CENTER

            val seekBar = SeekBar(this)

            seekBar.max =
                upper - lower

            seekBar.progress =
                -lower

            seekBar.rotation = -90f

            seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {

                        if (fromUser) {

                            val level =
                                (progress + lower)
                                    .toShort()

                            try {
                                eq.setBandLevel(
                                    band,
                                    level
                                )
                            } catch (_: Exception) {
                            }
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

            val frequency =
                TextView(this)

            frequency.text =
                formatFrequency(
                    eq.getCenterFreq(band)
                )

            frequency.textSize = 12f
            frequency.setTextColor(Color.WHITE)
            frequency.gravity = Gravity.CENTER

            column.addView(
                seekBar,
                LinearLayout.LayoutParams(
                    180,
                    70
                )
            )

            column.addView(frequency)

            equalizerLayout.addView(
                column,
                LinearLayout.LayoutParams(
                    75,
                    210
                )
            )
        }
    }

    private fun formatFrequency(
        frequency: Int
    ): String {

        val hz = frequency / 1000

        return if (hz >= 1000) {
            "${hz / 1000}.${(hz % 1000) / 100}kHz"
        } else {
            "${hz}Hz"
        }
    }

    private fun toggleBassBoost(button: Button) {

        val bass = bassBoost ?: return

        try {

            if (bass.enabled
