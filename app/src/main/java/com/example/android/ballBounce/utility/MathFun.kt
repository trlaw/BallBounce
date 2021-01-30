package com.example.android.ballBounce.utility

import android.view.Surface
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

fun randDoubleInRange(dblMin: Float, dblMax: Float): Double {
    return dblMin + Random.nextDouble() * (dblMax - dblMin)
}

fun rotateSensorToDisplayCoords(rotConstant: Int, inputVector: Vector): Vector? {
    return when (rotConstant) {
        Surface.ROTATION_0 -> inputVector
        Surface.ROTATION_90 -> Vector(-1f * inputVector.y, inputVector.x)
        Surface.ROTATION_180 -> inputVector.times(-1.0)
        Surface.ROTATION_270 -> Vector(inputVector.y, -1f * inputVector.x)
        else -> null
    }
}

fun invokeAllPairs(collectionSize: Int, funToInvoke: (Int, Int) -> Unit) {
    for (i in 0 until collectionSize) {
        for (j in 0 until i) {
            funToInvoke(i, j)
        }
    }
}

fun invokeAllOrderedPairs(indexOneSize: Int, indexTwoSize: Int, funToInvoke: (Int, Int) -> Unit) {
    for (i in 0 until indexOneSize) {
        for (j in 0 until indexTwoSize) {
            funToInvoke(i,j)
        }
    }
}

fun vectorMidpoint(v1:Vector,v2:Vector): Vector {
    return Vector((v1.x+v2.x)/(2f),(v1.y+v2.y)/(2f))
}

fun quadraticSolution(a: Double, b: Double, c: Double) : Pair<Double?,Double?> {
    var outPair: Pair<Double?,Double?> = Pair(null,null)
    val discriminant = b.pow(2)-4*a*c
    if (discriminant > 0) {
        outPair = Pair<Double?,Double?>(((-b)+(if (b >= 0f) -1f else 1f)*sqrt(discriminant))/((2f)*a),
            ((2f)*c)/((-b)+(if (b >= 0f) -1f else 1f)*sqrt(discriminant)))
    }
    return outPair
}

fun saferMinus(a:Double,b:Double): Double {
    return (a.pow(2)-b.pow(2))/(a+b)
}