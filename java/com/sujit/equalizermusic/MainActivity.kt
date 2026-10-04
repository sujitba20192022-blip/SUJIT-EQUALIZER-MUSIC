package com.sujit.equalizermusic

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.rgb(16, 20, 24))

        val title = TextView(this)
        title.text = "SUJIT EQUALIZER MUSIC"
        title.textSize = 24f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "Music • Equalizer • Player"
        subtitle.textSize = 16f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        layout.addView(title)
        layout.addView(subtitle)

        setContentView(layout)
    }
}
