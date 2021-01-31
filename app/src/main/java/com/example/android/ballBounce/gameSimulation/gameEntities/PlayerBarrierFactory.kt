package com.example.android.ballBounce.gameSimulation.gameEntities

const val PLAYER_BARRIER_WIDTH = 4.0

class PlayerBarrierFactory: BarrierFactory() {
    override fun create(): BarrierEntity {
        return BarrierEntity().apply {
            width = PLAYER_BARRIER_WIDTH
        }
    }
}