package com.example.android.ballBounce.gameSimulation

class GameEndingBarrierEntity : BarrierEntity() {
    //var gameOverCallback: (() -> Unit) = { }
    var ballRemoveCallback: ((GameEntity) -> Unit) = {}
    override fun handleCollision(otherEntity: CollidableEntity) {
        when(otherEntity) {
            is BallEntity -> ballRemoveCallback(otherEntity)
            else -> return
        }
    }
}