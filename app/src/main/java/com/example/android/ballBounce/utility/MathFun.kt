package com.example.android.ballBounce.utility

import android.view.Surface
import kotlin.random.Random

fun randFloatInRange(floatMin: Float, floatMax: Float): Float {
    return floatMin + Random.nextFloat() * (floatMax - floatMin)
}

fun rotateSensorToDisplayCoords(rotConstant: Int, inputVector: Vector): Vector? {
    return when (rotConstant) {
        Surface.ROTATION_0 -> inputVector
        Surface.ROTATION_90 -> Vector(-1f * inputVector.y, inputVector.x)
        Surface.ROTATION_180 -> inputVector.times(-1f)
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