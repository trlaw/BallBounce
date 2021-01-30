package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.utility.Vector

open class RectangleEntity(val lowerBound: Vector, val upperBound: Vector) : GameEntity() {

    fun width(): Double {
        return upperBound.x - lowerBound.x
    }

    fun height(): Double {
        return upperBound.y - lowerBound.y
    }

    fun equalTo(other: RectangleEntity): Boolean {
        return ((this.lowerBound.equalTo(other.lowerBound))
                && (this.upperBound.equalTo(other.upperBound)))
    }


}