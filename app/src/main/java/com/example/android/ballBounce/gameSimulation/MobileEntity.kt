package com.example.android.ballBounce.gameSimulation

interface MobileEntity {
    fun travel(dt: Float)
    fun reactToCollisions(collisionGrid: CollisionGrid)
}