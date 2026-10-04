package com.sujit.equalizermusic

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)
        text.text = "SUJIT EQUALIZER MUSIC\n\nApp is working"
        text.textSize = 22f
        text.setTextColor(Color.WHITE)
        text.gravity = Gravity.CENTER
        text.setBackgroundColor(Color.rgb(16, 20, 24))

        setContentView(text)
    }
}
