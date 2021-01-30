package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.gameSimulation.CollisionGrid

interface MobileEntity {

    fun getPotentialColliders(collisionGrid: CollisionGrid): List<CollidableEntity>

    //Return negative result if no collision will happen
    fun getCollisionTime(other: CollidableEntity): Double

    fun travel(dt: Double)

}