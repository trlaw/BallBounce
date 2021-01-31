package com.example.android.ballBounce.gameSimulation.gameEntities

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