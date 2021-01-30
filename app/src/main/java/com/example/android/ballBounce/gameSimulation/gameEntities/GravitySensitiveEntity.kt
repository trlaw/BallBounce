package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.utility.Vector

interface GravitySensitiveEntity
{
    fun applyGravity(gravity: Vector)
}