package com.sujit.equalizermusic

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(30, 40, 30, 30)
        root.setBackgroundColor(Color.rgb(16, 20, 24))

        // App title
        val title = TextView(this)
        title.text = "SUJIT EQUALIZER MUSIC"
        title.textSize = 24f
        title.setTextColor(Color.WHITE)
        title.typeface = Typeface.DEFAULT_BOLD
        title.gravity = Gravity.CENTER

        root.addView(title)

        // Now Playing
        val nowPlaying = TextView(this)
        nowPlaying.text = "\nNow Playing\n\nNo song selected"
        nowPlaying.textSize = 20f
        nowPlaying.setTextColor(Color.LTGRAY)
        nowPlaying.gravity = Gravity.CENTER

        root.addView(nowPlaying)

        // Progress
        val progress = SeekBar(this)
        progress.max = 100
        progress.progress = 0

        root.addView(progress)

        // Time
        val time = TextView(this)
        time.text = "00:00                         00:00"
        time.textSize = 14f
        time.setTextColor(Color.LTGRAY)

        root.addView(time)

        // Player buttons
        val buttons = LinearLayout(this)
        buttons.gravity = Gravity.CENTER
        buttons.orientation = LinearLayout.HORIZONTAL

        val previous = Button(this)
        previous.text = "⏮"

        val play = Button(this)
        play.text = "▶"

        val next = Button(this)
        next.text = "⏭"

        buttons.addView(previous)
        buttons.addView(play)
        buttons.addView(next)

        root.addView(buttons)

        // Equalizer button
        val equalizer = Button(this)
        equalizer.text = "🎚  EQUALIZER"

        root.addView(equalizer)

        // Music list button
        val music = Button(this)
        music.text = "🎵  MY MUSIC"

        root.addView(music)

        setContentView(root)
    }
}
