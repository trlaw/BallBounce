package com.example.android.ballBounce.view

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class TextPaintFactory {
    private var paintCache: Paint = Paint().apply {
        typeface = Typeface.SANS_SERIF
        textSize = 50f
        color = Color.BLACK
    }

    fun getPaint():Paint {
        return paintCache
    }
}