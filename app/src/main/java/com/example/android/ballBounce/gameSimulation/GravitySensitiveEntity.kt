package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.utility.Vector

interface GravitySensitiveEntity
{
    fun applyGravityDeltaV(gravityDeltaV: Vector)
}