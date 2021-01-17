package com.example.android.ballBounce.gameSimulation

import java.util.*
import kotlin.Float.Companion.NEGATIVE_INFINITY

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
                val nextStep = if ((dt-dtProgress) < minDt) {dt-dtProgress} else minDt
                subStepAdvance(collidableList,collisionGrid,nextStep)
                dtProgress += nextStep
            }
        }

        private fun subStepAdvance(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid,
            subDt: Float
        ) {
            val collisionQueue =
                PriorityQueue<CollisionEvent>(50, CollisionEvent.CollisionEventComparer)
            var dtProgress = 0f

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
            return collidableList.fold(
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

