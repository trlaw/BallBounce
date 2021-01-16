package com.example.android.ballBounce.utility

import kotlin.math.*
import kotlin.random.Random.Default.nextFloat

class Vector(val x: Float, val y: Float) {
    var magCache: Float? = null
    fun plus(other: Vector): Vector {
        return Vector(this.x + other.x, this.y + other.y)
    }

    fun times(multiplier: Float): Vector {
        return Vector(this.x * multiplier, this.y * multiplier)
    }

    fun minus(other: Vector): Vector {
        return this.plus(other.times(-1f))
    }

    fun dot(other: Vector): Float {
        return this.x * other.x + this.y * other.y
    }

    fun equalTo(other: Vector): Boolean {
        return ((this.x == other.x) && (this.y == other.y))
    }

    fun mag(): Float {
        if (magCache == null) {
            magCache = sqrt(this.x.pow(2) + this.y.pow(2)) //Somewhat expensive op
        }
        return magCache!!
    }

    fun copyVector(): Vector {
        return Vector(this.x, this.y)
    }

    fun unitScaled(): Vector {
        return this.times((1f) / this.mag())
    }

    fun unitNormal(): Vector {
        if (y != 0f) {
            return Vector(1f, -(x / y)).unitScaled()
        }
        return Vector(0f, 1f)
    }

    companion object {
        fun randomVectorInBox(lowerBound: Vector, upperBound: Vector): Vector {
            val differenceVector = upperBound.minus(lowerBound)
            return Vector(
                lowerBound.x + nextFloat() * differenceVector.x,
                lowerBound.y + nextFloat() * differenceVector.y
            )
        }

        fun randomUnit(): Vector {
            val angle: Float = (2f) * (PI.toFloat()) * nextFloat()
            return Vector(cos(angle), sin(angle))
        }

        fun zero(): Vector {
            return Vector(0f, 0f)
        }
    }
}