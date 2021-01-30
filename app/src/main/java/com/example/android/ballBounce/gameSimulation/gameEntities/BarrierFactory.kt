package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.gameSimulation.BarrierEntity

abstract class BarrierFactory {
    abstract fun create(): BarrierEntity
}