package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.gameSimulation.gameEntities.BallEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.RectangleEntity
import com.example.android.ballBounce.utility.Vector
import kotlin.math.min
import kotlin.random.Random

const val BALL_INIT_V_MIN = 3.0
const val BALL_INIT_V_MAX = 6.0

class GameBoundary(dimensions: Vector) : RectangleEntity(Vector.zero(), dimensions) {

    fun isValidGameBoundary(): Boolean {
        return ((width() > 0f) && (height() > 0f))
    }

    fun getSpawnArea(): RectangleEntity {
        val offsetVector = Vector(width() / (4.0), height() / (4.0))
        return RectangleEntity(
            lowerBound.plus(offsetVector),
            upperBound.minus(offsetVector)
        )
    }

    fun setSpawnState(newBall: BallEntity) {
        newBall.apply {
            position = getRandomBallSpawnPosition()
            velocity = getRandomBallSpawnVelocity()
        }
    }

    private fun getRandomBallSpawnPosition(): Vector {
        return upperBound.minus(lowerBound).times(0.5).plus(
            lowerBound.plus(
                Vector.randomUnit()
                    .times(0.25 * min(height(), width()))
            )
        )
    }

    private fun getRandomBallSpawnVelocity(): Vector {
        return Vector.randomUnit()
            .times(BALL_INIT_V_MIN + Random.nextFloat() * (BALL_INIT_V_MAX - BALL_INIT_V_MIN))
    }

}