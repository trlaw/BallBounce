package com.example.android.ballBounce.gameSimulation

import java.util.*

class CollisionEvent(val collidables: Array<CollidableEntity>, val collisionTime: Float) {

    companion object CollisionEventComparer : Comparator<CollisionEvent> {
        override fun compare(o1: CollisionEvent?, o2: CollisionEvent?): Int {
            if ((o1 == null) || (o2 == null)) {
                return 0
            }
            return if (o1.collisionTime < o2.collisionTime) {
                -1
            } else if (o1.collisionTime == o2.collisionTime) {
                0
            } else {
                1
            }
        }
    }
}