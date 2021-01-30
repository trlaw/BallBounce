package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.gameSimulation.CollisionGrid

interface CollidableEntity {
    fun collided(otherEntity: CollidableEntity): Boolean
    fun handleCollision(otherEntity: CollidableEntity, permittedDt: Double): Unit
    fun markCollisionGrid(collisionGrid: CollisionGrid): Unit
    fun unMarkCollisionGrid(collisionGrid: CollisionGrid)

}