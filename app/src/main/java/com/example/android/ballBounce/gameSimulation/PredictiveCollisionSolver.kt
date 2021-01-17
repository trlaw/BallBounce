package com.example.android.ballBounce.gameSimulation

import java.util.*
import kotlin.Float.Companion.NEGATIVE_INFINITY
import kotlin.math.sqrt

class PredictiveCollisionSolver {

    companion object {
        fun simulateCollisions(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid,
            dt: Float
        ) {
            val minDt = calcMinDt(collidableList, collisionGrid)
            var dtProgress: Float = 0f
            while (dtProgress < dt) {
                val nextStep = if ((dt - dtProgress) < minDt) {
                    dt - dtProgress
                } else minDt
                subStepAdvance(collidableList, collisionGrid, nextStep)
                dtProgress += nextStep
            }
        }

        private fun subStepAdvance(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid,
            subDt: Float
        ) {
            var collisionQueue =
                PriorityQueue<CollisionEvent>(100, CollisionEvent.CollisionEventComparer)
            var dtProgress = 0f

            //Populate collision queue (initial pass)
            collidableList.forEach { collidableEntity ->
                if (collidableEntity is MobileEntity) {
                    collidableEntity.getPotentialColliders(collisionGrid)
                        .forEach { otherEntity ->
                            val timeRslt = collidableEntity.getCollisionTime(otherEntity)
                            if ((timeRslt >= 0f) && (timeRslt <= (subDt - dtProgress))) {
                                val candidateEvent = CollisionEvent(
                                    arrayOf(collidableEntity, otherEntity),
                                    dtProgress + timeRslt
                                )
                                if (collisionQueue.none { candidateEvent.equals(it) }) {
                                    if (!collisionQueue.offer(candidateEvent)) {
                                        val biggerQueue = PriorityQueue<CollisionEvent>(
                                            collisionQueue.size * 2,
                                            CollisionEvent.CollisionEventComparer
                                        )
                                        collisionQueue.forEach { existingEvent ->
                                            biggerQueue.add(
                                                existingEvent
                                            )
                                            collisionQueue = biggerQueue
                                            collisionQueue.add(candidateEvent)
                                        }
                                    }
                                }
                            }
                        }
                }
            }

        }

        private fun calcMinDt(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid
        ): Float {
            return (collisionGrid.getMinCellDimension() - getMaxRadius(collidableList)) / getMaxVelocity(
                collidableList
            )
        }

        private fun getMaxRadius(collidableList: List<CollidableEntity>): Float {
            return collidableList.fold(
                NEGATIVE_INFINITY,
                { maxR: Float, collidableEntity: CollidableEntity ->
                    if ((collidableEntity is BallEntity) && (collidableEntity.radius > maxR)) {
                        collidableEntity.radius
                    } else {
                        maxR
                    }
                })
        }

        private fun getMaxVelocity(collidableList: List<CollidableEntity>): Float {
            //sqrt(2) factor ensures that speed up from collision cannot result
            //in invalidation of the collision grid before its next update
            return sqrt(2f) * collidableList.fold(
                NEGATIVE_INFINITY,
                { maxV: Float, collidableEntity: CollidableEntity ->
                    if ((collidableEntity is BallEntity) && (collidableEntity.velocity.mag() > maxV)) {
                        collidableEntity.velocity.mag()
                    } else {
                        maxV
                    }
                })
        }

    }
}

