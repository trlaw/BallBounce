package com.example.android.ballBounce.gameSimulation

interface CollidableEntity {
    fun collided(otherEntity: CollidableEntity): Boolean
    fun handleCollision(otherEntity: CollidableEntity): Unit
    fun markCollisionGrid(collisionGrid: CollisionGrid): Unit
    fun unMarkCollisionGrid(collisionGrid: CollisionGrid)
}