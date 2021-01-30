package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.utility.Vector

const val LOST_BALL_TEXT_X = 40.0
const val LOST_BALL_TEXT_Y = 60.0

class LostBallsTextEntity(): TextEntity(Vector(LOST_BALL_TEXT_X, LOST_BALL_TEXT_Y)) {
    var lostBallCount = 0
    override fun getPaintableShape(): PaintableShape {
        return super.getPaintableShape().apply {
            text = "Lost: $lostBallCount"
        }
    }
}