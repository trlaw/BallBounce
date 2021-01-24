package com.example.android.ballBounce.gameSimulation

import kotlin.Float.Companion.NEGATIVE_INFINITY
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.sqrt

class PredictiveCollisionSolver {

    companion object {
        fun simulateMotion(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid,
            dt: Float
        ) {
            val maxDt = calcMaxDt(collidableList, collisionGrid)
            var dtProgress: Float = 0f
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
            subDt: Float,
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
            subDt: Float
        ) {
            var potentialColliders = ballEntity.getPotentialColliders(collisionGrid)
            var earliestCollisionData = getNextCollision(ballEntity, potentialColliders)
            val permittedDt =
                if (earliestCollisionData.second < subDt) earliestCollisionData.second else subDt
            val oldPosition = ballEntity.position
            ballEntity.travel(permittedDt)
            val positionDelta = ballEntity.position.minus(oldPosition)
            ballEntity.refreshCollisionGridMark(collisionGrid)
            potentialColliders = ballEntity.getPotentialColliders(collisionGrid)
            var wayClear = true
            potentialColliders.forEach {
                if (ballEntity.collided(it) && it is BallEntity) {
                    wayClear = false
                    ballEntity.handleCollision(it, 0f)
                }
            }
            if (!wayClear) {
                ballEntity.position = ballEntity.position.minus(positionDelta)
                ballEntity.refreshCollisionGridMark(collisionGrid)
            }
            if (permittedDt < subDt) {
                ballEntity.handleCollision(earliestCollisionData.first!!, permittedDt)
            }
        }

        private fun applyGravityAcceleration(collidableList: List<CollidableEntity>, dt: Float) {
            for (collidableEntity in collidableList) {
                if (collidableEntity is BallEntity) {
                    collidableEntity.applyGravityAcceleration(dt)
                }
            }
        }

        private fun getNextCollision(
            ballEntity: BallEntity,
            potentialColliders: List<CollidableEntity>
        ): Pair<CollidableEntity?, Float> {
            var earliestCollider: CollidableEntity? = null
            var earliestCollisionTime: Float = POSITIVE_INFINITY
            for (potentialCollider in potentialColliders) {
                val collisionTime = ballEntity.getCollisionTime(potentialCollider)
                if ((collisionTime > -0.5f) && (collisionTime < earliestCollisionTime)) {
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
        ): Float {
            return ((collisionGrid.getMinCellDimension() / (2.0f) - getMaxRadius(collidableList)) / getMaxVelocity(
                collidableList
            )) * (1f)
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
                    if ((collidableEntity is BallEntity) && (collidableEntity.maxSpeed > maxV)) {
                        collidableEntity.maxSpeed
                    } else {
                        maxV
                    }
                })
        }
    }
}


