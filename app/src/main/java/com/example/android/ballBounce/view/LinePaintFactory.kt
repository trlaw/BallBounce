package com.example.android.ballBounce.view

import android.graphics.Paint

class LinePaintFactory(private val strokeColor: Int) {
    private var paintCache: HashMap<Double,Paint> = HashMap<Double,Paint>()

    fun getPaint(strokeWidth: Double): Paint {
        if (paintCache.containsKey(strokeWidth)) {
            return paintCache[strokeWidth]!!
        }
        paintCache[strokeWidth] = Paint().apply {
            color = strokeColor
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
        }
        paintCache[strokeWidth]!!.strokeWidth = strokeWidth.toFloat()
        return paintCache[strokeWidth]!!
    }
}
