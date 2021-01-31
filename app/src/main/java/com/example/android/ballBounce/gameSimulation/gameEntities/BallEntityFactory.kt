package com.example.android.ballBounce.gameSimulation.gameEntities

const val NUM_COLORS = 4
const val BASE_COR = 0.5
const val BASE_BALL_RADIUS = 30.0
const val MAX_SPEED = BASE_BALL_RADIUS / (5.0)

open class BallEntityFactory {
    var nextColorIndex = 0
    open fun create(): BallEntity {
        return BallEntity().apply {
            cOr = BASE_COR
            radius = BASE_BALL_RADIUS
            this.colorIndex = nextColorIndex++
            nextColorIndex %= NUM_COLORS
        }
    }
}