package com.example.android.ballBounce.view

import android.graphics.Paint

const val OUTLINE_WIDTH = 3f

class CirclePaintFactory(private val ballColors: IntArray, private val strokeColor: Int) {

    private var ballPaints: Array<Paint>
    private var outlinePaint: Paint

    init {
        ballPaints = Array<Paint>(ballColors.size) { i ->
            Paint().apply {
                color = ballColors[i]
                isAntiAlias = true
                isDither = true
                style = Paint.Style.FILL
            }
        }
        outlinePaint = Paint().apply {
            color = strokeColor
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeWidth = OUTLINE_WIDTH
        }
    }

    fun getOutlinePaint(): Paint {
        return outlinePaint
    }

    fun getFillPaint(colorIndex: Int): Paint {
        return ballPaints[colorIndex.rem(ballColors.size)]
    }
}