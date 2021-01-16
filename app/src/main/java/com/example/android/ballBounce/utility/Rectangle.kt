package com.example.android.ballBounce.utility

class Rectangle(val lowerBounds: Vector,val upperBounds: Vector) {

    fun width(): Float {
        return upperBounds.x - lowerBounds.x
    }

    fun height(): Float {
        return upperBounds.y - lowerBounds.y
    }

    fun equalTo(other: Rectangle): Boolean {
        return ((this.lowerBounds.equalTo(other.lowerBounds))
                && (this.upperBounds.equalTo(other.upperBounds)))
    }

    fun isValid(): Boolean {
        return ((this.width() != 0f) && (this.height() != 0f))
    }

    companion object {
        fun zero(): Rectangle {
            return Rectangle(Vector.zero(), Vector.zero())
        }
    }
}