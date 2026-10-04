package com.sujit.equalizermusic

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import kotlin.random.Random

class MainActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private lateinit var nowPlaying: TextView
    private lateinit var progressBar: SeekBar
    private lateinit var playButton: Button
    private lateinit var equalizerLayout: LinearLayout
    private lateinit var shuffleButton: Button
    private lateinit var repeatButton: Button

    private val songIds = ArrayList<Long>()
    private val songTitles = ArrayList<String>()

    private var currentSong = -1

    private var shuffleEnabled = false
    private var repeatEnabled = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createScreen()

        if (Build.VERSION.SDK_INT >= 33) {

            if (
                checkSelfPermission(
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
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

                    if (fromUser) {

                        val player = mediaPlayer

                        if (player != null) {

                            try {

                                player.seekTo(
                                    player.duration *
                                            progress / 100
                                )

                            } catch (_: Exception) {
                            }
                        }
                    }
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {
                }
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

        val modeButtons = LinearLayout(this)

        modeButtons.gravity = Gravity.CENTER

        shuffleButton = Button(this)

        shuffleButton.text = "🔀 SHUFFLE OFF"

        shuffleButton.setOnClickListener {
            shuffleEnabled = !shuffleEnabled
            updateModeButtons()
        }

        repeatButton = Button(this)

        repeatButton.text = "🔁 REPEAT OFF"

        repeatButton.setOnClickListener {
            repeatEnabled = !repeatEnabled
            updateModeButtons()
        }

        modeButtons.addView(shuffleButton)
        modeButtons.addView(repeatButton)

        root.addView(modeButtons)

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

        bassButton.text =
            "🔊 BASS BOOST OFF"

        bassButton.setOnClickListener {
            toggleBassBoost(bassButton)
        }

        root.addView(bassButton)

        val presetButton = Button(this)

        presetButton.text = "🎛️ PRESETS"

        presetButton.setOnClickListener {
            showPresets()
        }

        root.addView(presetButton)

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

    private fun updateModeButtons() {

        if (shuffleEnabled) {

            shuffleButton.text =
                "🔀 SHUFFLE ON"

        } else {

            shuffleButton.text =
                "🔀 SHUFFLE OFF"
        }

        if (repeatEnabled) {

            repeatButton.text =
                "🔁 REPEAT ON"

        } else {

            repeatButton.text =
                "🔁 REPEAT OFF"
        }
    }

    private fun setupEqualizer() {

        val player = mediaPlayer ?: return

        try {

            equalizer?.release()

            equalizer = Equalizer(
                0,
                player.audioSessionId
            )

            equalizer?.enabled = true

            createEqualizerSliders()

            bassBoost?.release()

            bassBoost = BassBoost(
                0,
                player.audioSessionId
            )

            bassBoost?.enabled = false

        } catch (_: Exception) {
        }
    }

    private fun createEqualizerSliders() {

        equalizerLayout.removeAllViews()

        val eq = equalizer ?: return

        val bands =
            eq.numberOfBands.toInt()

        val lower =
            eq.bandLevelRange[0].toInt()

        val upper =
            eq.bandLevelRange[1].toInt()

        for (i in 0 until bands) {

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
                object :
                    SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {

                        if (fromUser) {

                            try {

                                val level =
                                    (progress + lower)
                                        .toShort()

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
                    ) {
                    }

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {
                    }
                }
            )

            val frequency = TextView(this)

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

    private fun toggleBassBoost(
        button: Button
    ) {

        val bass = bassBoost ?: return

        try {

            if (bass.enabled) {

                bass.enabled = false

                button.text =
                    "🔊 BASS BOOST OFF"

            } else {

                bass.setStrength(
                    700.toShort()
                )

                bass.enabled = true

                button.text =
                    "🔊 BASS BOOST ON"
            }

        } catch (_: Exception) {
        }
    }

    private fun showPresets() {

        val presets = arrayOf(
            "Normal",
            "Rock",
            "Pop",
            "Jazz",
            "Classical"
        )

        AlertDialog.Builder(this)
            .setTitle("Equalizer Preset")
            .setItems(presets) { _, which ->
                applyPreset(which)
            }
            .show()
    }

    private fun applyPreset(
        preset: Int
    ) {

        val eq = equalizer ?: return

        val bands =
            eq.numberOfBands.toInt()

        val range =
            eq.bandLevelRange

        val min =
            range[0].toInt()

        val max =
            range[1].toInt()

        for (i in 0 until bands) {

            val level: Short

            when (preset) {

                0 -> {
                    level = 0
                }

                1 -> {
                    level = when {

                        i < bands / 3 ->
                            (max * 0.60)
                                .toInt()
                                .toShort()

                        i > bands * 2 / 3 ->
                            (max * 0.35)
                                .toInt()
                                .toShort()

                        else ->
                            (max * 0.10)
                                .toInt()
                                .toShort()
                    }
                }

                2 -> {
                    level = when {

                        i < bands / 3 ->
                            (max * 0.45)
                                .toInt()
                                .toShort()

                        i > bands * 2 / 3 ->
                            (max * 0.40)
                                .toInt()
                                .toShort()

                        else ->
                            (max * 0.05)
                                .toInt()
                                .toShort()
                    }
                }

                3 -> {
                    level = when {

                        i < bands / 3 ->
                            (max * 0.20)
                                .toInt()
                                .toShort()

                        i > bands * 2 / 3 ->
                            (max * 0.20)
                                .toInt()
                                .toShort()

                        else ->
                            (max * 0.25)
                                .toInt()
                                .toShort()
                    }
                }

                4 -> {
                    level = when {

                        i < bands / 3 ->
                            (max * 0.30)
                                .toInt()
                                .toShort()

                        i > bands / 2 ->
                            (max * 0.45)
                                .toInt()
                                .toShort()

                        else ->
                            (max * 0.15)
                                .toInt()
                                .toShort()
                    }
                }

                else -> {
                    level = 0
                }
            }

            try {

                val safeLevel =
                    level.toInt()
                        .coerceIn(min, max)
                        .toShort()

                eq.setBandLevel(
                    i.toShort(),
                    safeLevel
                )

            } catch (_: Exception) {
            }
        }

        createEqualizerSliders()
    }

    private fun getMusicList(): LinearLayout {
        return findViewById(12345)
    }

    private fun loadMusic() {

        val musicList =
            getMusicList()

        musicList.removeAllViews()

        songIds.clear()
        songTitles.clear()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE
        )

        val cursor =
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

        if (cursor == null) {

            showMessage(
                "Music नहीं मिली"
            )

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

            song.text =
                "▶  $title"

            song.textSize = 17f

            song.setTextColor(
                Color.WHITE
            )

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

    private fun playSong(
        position: Int
    ) {

        if (
            position < 0 ||
            position >= songIds.size
        ) return

        try {

            mediaPlayer?.release()

            equalizer?.release()
            bassBoost?.release()

            val uri =
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songIds[position]
                )

            mediaPlayer =
                MediaPlayer.create(
                    this,
                    uri
                )

            if (mediaPlayer == null) {

                nowPlaying.text =
                    "Song play नहीं हो पाया"

                return
            }

            currentSong = position

            nowPlaying.text =
                "🎵 ${songTitles[position]}"

            setupEqualizer()

            mediaPlayer?.start()

            playButton.text = "⏸"

            mediaPlayer?.setOnCompletionListener {

                if (repeatEnabled) {

                    playSong(currentSong)

                } else {

                    playNext()
                }
            }

            updateProgress()

        } catch (_: Exception) {

            nowPlaying.text =
                "Song play नहीं हो पाया"
        }
    }

    private fun togglePlay() {

        val player =
            mediaPlayer ?: return

        try {

            if (player.isPlaying) {

                player.pause()

                playButton.text = "▶"

            } else {

                player.start()

                playButton.text = "⏸"

                updateProgress()
            }

        } catch (_: Exception) {
        }
    }

    private fun playNext() {

        if (songIds.isEmpty()) return

        val next: Int

        if (shuffleEnabled && songIds.size > 1) {

            do {

                next = Random.nextInt(
                    songIds.size
                )

            } while (next == currentSong)

        } else {

            next =
                if (
                    currentSong + 1 <
                    songIds.size
                ) {
                    currentSong + 1
                } else {
                    0
                }
        }

        playSong(next)
    }

    private fun playPrevious() {

        if (songIds.isEmpty()) return

        val previous =
            if (currentSong - 1 >= 0) {
                currentSong - 1
            } else {
                songIds.size - 1
            }

        playSong(previous)
    }

    private fun updateProgress() {

        val player =
            mediaPlayer ?: return

        try {

            if (!player.isPlaying) return

            if (player.duration > 0) {

                progressBar.progress =
                    player.currentPosition *
                            100 /
                            player.duration
            }

        } catch (_: Exception) {
            return
        }

        handler.postDelayed(
            { updateProgress() },
            500
        )
    }

    private fun showMessage(
        message: String
    ) {

        val text =
            TextView(this)

        text.text = message
        text.textSize = 17f
        text.setTextColor(Color.LTGRAY)
        text.gravity = Gravity.CENTER

        getMusicList()
            .addView(text)
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

        if (
            requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            loadMusic()

        } else {

            showMessage(
                "Music permission की जरूरत है"
            )
        }
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(
            null
        )

        equalizer?.release()
        bassBoost?.release()

        equalizer = null
        bassBoost = null

        mediaPlayer?.release()
        mediaPlayer = null

        super.onDestroy()
    }
}
