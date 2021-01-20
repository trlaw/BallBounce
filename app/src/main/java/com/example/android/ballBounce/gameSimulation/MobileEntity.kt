package com.example.android.ballBounce.gameSimulation

interface MobileEntity {

    fun getPotentialColliders(collisionGrid: CollisionGrid): List<CollidableEntity>

    //Return negative result if no collision will happen
    fun getCollisionTime(other: CollidableEntity): Float

    fun travel(dt: Float)
}