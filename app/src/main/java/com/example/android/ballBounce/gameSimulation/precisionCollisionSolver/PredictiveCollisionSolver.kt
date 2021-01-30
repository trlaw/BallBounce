package com.example.android.ballBounce.gameSimulation.precisionCollisionSolver

import com.example.android.ballBounce.gameSimulation.BallEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.CollidableEntity
import com.example.android.ballBounce.gameSimulation.CollisionGrid
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.sqrt

class PredictiveCollisionSolver {

    companion object {
        fun simulateMotion(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid,
            dt: Double
        ) {
            val maxDt = calcMaxDt(collidableList, collisionGrid)
            var dtProgress: Double = 0.0
            while (dtProgress < dt) {
                val nextStep = if ((dt - dtProgress) < maxDt) {
                    dt - dtProgress
                } else maxDt
                subStepAdvance(collidableList, collisionGrid, nextStep)
                refreshCollisionGrid(collidableList, collisionGrid)
                dtProgress += nextStep
            }
        }

        private fun subStepAdvance(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid,
            subDt: Double,
        ) {
            //Update each mobile entity according to policy
            collidableList.forEach { collidableEntity ->
                if (collidableEntity is BallEntity) {
                    advanceBallState(collidableEntity, collisionGrid, subDt)
                }
            }
            applyGravityAcceleration(collidableList, subDt)
        }

        private fun advanceBallState(
            ballEntity: BallEntity,
            collisionGrid: CollisionGrid,
            subDt: Double
        ) {
            var potentialColliders = ballEntity.getPotentialColliders(collisionGrid)
//            potentialColliders.forEach {
//                if (it is BallEntity) {
//                    if (ballEntity.position.minus(it.position).mag() < (2.5 * ballEntity.radius)) {
//                        Log.d(
//                            "BallPositions",
//                            "Ball A:${ballEntity.colorIndex} (${ballEntity.position.x},${ballEntity.position.y})"
//                        )
//                        Log.d(
//                            "BallPositions",
//                            "Ball B:${it.colorIndex} (${it.position.x},${it.position.y})"
//                        )
//                        Log.d(
//                            "BallPositions",
//                            "Distance: ${ballEntity.position.minus(it.position).mag()}"
//                        )
//                    }
//                }
//            }
//            Log.d(
//                "BallPositions",
//                 "Number of potential ball colliders = ${potentialColliders.count { it is BallEntity }}"
//            )
            var earliestCollisionData = getNextCollision(ballEntity, potentialColliders)
            val permittedDt =
                if (earliestCollisionData.second < subDt) earliestCollisionData.second else subDt

            ballEntity.travel(permittedDt)
            ballEntity.refreshCollisionGridMark(collisionGrid)

            if (permittedDt < subDt) {
                ballEntity.handleCollision(earliestCollisionData.first!!, permittedDt)
                earliestCollisionData = getNextCollision(ballEntity, potentialColliders)
                if ((earliestCollisionData.second >= 0) && (earliestCollisionData.second < (subDt - permittedDt))) {
                    ballEntity.travel((subDt-permittedDt))
                    ballEntity.refreshCollisionGridMark(collisionGrid)
                }
            }
        }

        private fun applyGravityAcceleration(collidableList: List<CollidableEntity>, dt: Double) {
            for (collidableEntity in collidableList) {
                if (collidableEntity is BallEntity) {
                    collidableEntity.applyGravityAcceleration(dt)
                }
            }
        }

        private fun getNextCollision(
            ballEntity: BallEntity,
            potentialColliders: List<CollidableEntity>
        ): Pair<CollidableEntity?, Double> {
            var earliestCollider: CollidableEntity? = null
            var earliestCollisionTime: Double = POSITIVE_INFINITY
            for (potentialCollider in potentialColliders) {
                val collisionTime = ballEntity.getCollisionTime(potentialCollider)
                if ((collisionTime >= 0.0) && (collisionTime < earliestCollisionTime)) {
                    earliestCollider = potentialCollider
                    earliestCollisionTime = collisionTime
                }
            }
            return Pair(earliestCollider, earliestCollisionTime)
        }

        private fun refreshCollisionGrid(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid
        ) {
            for (collidableEntity in collidableList) {
                if (collidableEntity is BallEntity) {
                    collidableEntity.refreshCollisionGridMark(collisionGrid)
                }
            }
        }

        private fun calcMaxDt(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid
        ): Double {
            return ((collisionGrid.getMinCellDimension() / (2.0) - getMaxRadius(collidableList))
                    / getMaxVelocity(collidableList))
        }

        private fun getMaxRadius(collidableList: List<CollidableEntity>): Double {
            return collidableList.fold(
                NEGATIVE_INFINITY,
                { maxR: Double, collidableEntity: CollidableEntity ->
                    if ((collidableEntity is BallEntity) && (collidableEntity.radius > maxR)) {
                        collidableEntity.radius
                    } else {
                        maxR
                    }
                })
        }

        private fun getMaxVelocity(collidableList: List<CollidableEntity>): Double {
            //sqrt(2) factor ensures that speed up from collision cannot result
            //in invalidation of the collision grid before its next update
            return sqrt(2.0) * collidableList.fold(
                NEGATIVE_INFINITY,
                { maxV: Double, collidableEntity: CollidableEntity ->
                    if ((collidableEntity is BallEntity) && (collidableEntity.maxSpeed > maxV)) {
                        collidableEntity.maxSpeed
                    } else {
                        maxV
                    }
                })
        }
    }
}


