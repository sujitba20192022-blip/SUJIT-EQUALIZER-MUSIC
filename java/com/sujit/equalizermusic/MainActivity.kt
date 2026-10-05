package com.sujit.equalizermusic

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screen = TextView(this)

        screen.text = "SUJIT EQUALIZER MUSIC\n\nApp successfully opened!"
        screen.textSize = 22f
        screen.setTextColor(Color.WHITE)
        screen.setBackgroundColor(Color.rgb(16, 20, 24))
        screen.gravity = Gravity.CENTER

        setContentView(screen)
    }
}
