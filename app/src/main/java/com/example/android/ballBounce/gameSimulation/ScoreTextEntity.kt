package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.utility.Vector

const val SCORE_TEXT_X = 240f
const val SCORE_TEXT_Y = 60f

class ScoreTextEntity : TextEntity(Vector(SCORE_TEXT_X, SCORE_TEXT_Y)) {
    var score = 0

    override fun getPaintableShape(): PaintableShape {
        return super.getPaintableShape().apply {
            text = "Score: $score"
        }
    }
}