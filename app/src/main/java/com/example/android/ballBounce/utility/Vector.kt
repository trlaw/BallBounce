package com.example.android.ballBounce.utility

import kotlin.math.*
import kotlin.random.Random.Default.nextFloat

class Vector(val x: Double, val y: Double) {
    var magCache: Double? = null
    fun plus(other: Vector): Vector {
        return Vector(this.x + other.x, this.y + other.y)
    }

    fun times(multiplier: Double): Vector {
        return Vector(this.x * multiplier, this.y * multiplier)
    }

    fun minus(other: Vector): Vector {
        return this.plus(other.times(-1.0))
    }

    fun dot(other: Vector): Double {
        return this.x * other.x + this.y * other.y
    }

    fun equalTo(other: Vector): Boolean {
        return ((this.x == other.x) && (this.y == other.y))
    }

    fun mag(): Double {
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
        if (y != 0.0) {
            return Vector(1.0, -(x / y)).unitScaled()
        }
        return Vector(0.0, 1.0)
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
            val angle: Double = (2.0) * (PI.toFloat()) * nextFloat()
            return Vector(cos(angle), sin(angle))
        }

        fun zero(): Vector {
            return Vector(0.0, 0.0)
        }
    }
}