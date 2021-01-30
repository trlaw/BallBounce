package com.example.android.ballBounce.utility

import kotlin.math.pow
import kotlin.math.sqrt

class VectorN(vararg components: Double) {
    private val vectorValues = components

    private val dimension: Int
        get() {
            return vectorValues.size
        }

    fun component(i: Int): Double {
        return if ((i >= 0) && (i < vectorValues.size)) {
            vectorValues[i]
        } else {
            Double.NaN
        }
    }

    fun plus(other: VectorN): VectorN? {
        return if (this.dimension == other.dimension) {
            VectorN(
                *this.vectorValues.zip(other.vectorValues).map { a -> a.first + a.second }
                    .toDoubleArray())
        } else {
            null
        }
    }

    fun times(multiplier: Double): VectorN {
        return VectorN(*this.vectorValues.map { it -> multiplier * it }.toDoubleArray())
    }

    fun minus(other: VectorN): VectorN? {
        return if (this.dimension == other.dimension) {
            this.plus(other.times(-1.0))
        } else {
            null
        }
    }

    fun innerProduct(other: VectorN): Double {
        return if (this.dimension == other.dimension) {
            this.vectorValues.zip(other.vectorValues)
                .fold(0.0, { acc, valPair -> acc + valPair.first * valPair.second })
        } else {
            Double.NaN
        }
    }

    fun twoNormSquared(): Double {
        return vectorValues.fold(0.0) { acc, it -> acc + it.pow(2) }
    }

    fun twoNorm(): Double {
        return sqrt(twoNormSquared())
    }
}