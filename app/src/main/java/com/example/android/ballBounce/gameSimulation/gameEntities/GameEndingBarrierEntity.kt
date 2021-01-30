package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.gameSimulation.BarrierEntity

class GameEndingBarrierEntity : BarrierEntity() {
    //var gameOverCallback: (() -> Unit) = { }
    var ballRemoveCallback: ((GameEntity) -> Unit) = {}
    override fun handleCollision(otherEntity: CollidableEntity, permittedDt: Double) {
        when(otherEntity) {
            is BallEntity -> ballRemoveCallback(otherEntity)
            else -> return
        }
    }
}