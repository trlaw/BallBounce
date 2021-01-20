package com.example.android.ballBounce.gameSimulation

import java.util.*
import kotlin.Float.Companion.NEGATIVE_INFINITY
import kotlin.math.sqrt

const val MAX_SIMULTANEOUS = 10 //Max collisions to process at any one time step

class PredictiveCollisionSolver {

    companion object {
        fun simulateCollisions(
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
                subStepAdvance(collidableList, collisionGrid, nextStep, dt)
                refreshCollisionGrid(collidableList, collisionGrid)
                dtProgress += nextStep
            }
        }

        private fun subStepAdvance(
            collidableList: List<CollidableEntity>,
            collisionGrid: CollisionGrid,
            subDt: Float,
            dt: Float
        ) {
            var collisionQueue =
                PriorityQueue<CollisionEvent>(500, CollisionEvent.CollisionEventComparer)
            var dtProgress = 0f

            //Populate collision queue (initial pass)
            collidableList.forEach { collidableEntity ->
                if (collidableEntity is MobileEntity) {
                    collisionQueue = addCollisionEvents(
                        collidableEntity,
                        collisionGrid,
                        subDt,
                        dtProgress,
                        collisionQueue
                    )
                }
            }

            var lastEvent: CollisionEvent? = null
            var nextEvent = collisionQueue.poll()
            var repeatsThisTime = 0
            while (nextEvent != null) {

                if ((lastEvent != null) && (lastEvent.collisionTime == nextEvent.collisionTime)) {
                    repeatsThisTime++
                } else {
                    repeatsThisTime = 0
                }

                //Avoid getting stuck on collision repair cycles
                if (repeatsThisTime > MAX_SIMULTANEOUS) {
                    val simultaneousEvents =
                        collisionQueue.filter { it.collisionTime == nextEvent.collisionTime }
                    simultaneousEvents.forEach { collisionQueue.remove(it) }
                    lastEvent = nextEvent
                    nextEvent = collisionQueue.poll()
                    continue
                }

                if ((lastEvent != null) && (lastEvent.equals(nextEvent))) {
                    lastEvent = nextEvent
                    nextEvent = collisionQueue.poll()
                    continue
                }


                //Update positions
                travelWithoutCollisions(collidableList, nextEvent.collisionTime, dtProgress)

                //Apply collision conditions
                nextEvent.collidables[0].handleCollision(nextEvent.collidables[1],nextEvent.collisionTime-dtProgress)

                //Update time
                dtProgress = nextEvent.collisionTime

                //Remove events from queue in which mobile entities participated.  Add any
                //newly created events
                for (i in 0..1) {
                    if (nextEvent.collidables[i] is MobileEntity) {
                        val eventsToPurge = mutableListOf<CollisionEvent>()
                        collisionQueue.forEach { collisionEvent ->
                            if (collisionEvent.collidables.contains(nextEvent.collidables[i])) {
                                eventsToPurge.add(collisionEvent)
                            }
                        }
                        eventsToPurge.forEach { collisionQueue.remove(it) }
                        collisionQueue = addCollisionEvents(
                            nextEvent.collidables[i] as MobileEntity,
                            collisionGrid,
                            subDt,
                            dtProgress,
                            collisionQueue,
                        )
                    }
                }
                lastEvent = nextEvent
                nextEvent = collisionQueue.poll()
            }
            travelWithoutCollisions(collidableList, subDt, dtProgress)
        }

        private fun travelWithoutCollisions(
            collidableList: List<CollidableEntity>,
            endTime: Float,
            dtProgress: Float
        ) {
            if (endTime > dtProgress) {
                for (collidableEntity in collidableList) {
                    if (collidableEntity is MobileEntity) {
                        collidableEntity.travel(endTime - dtProgress)
                    }
                }
            }
        }

        private fun addCollisionEvents(
            collidableEntity: MobileEntity,
            collisionGrid: CollisionGrid,
            subDt: Float,
            dtProgress: Float,
            collisionQueue: PriorityQueue<CollisionEvent>,
        ): PriorityQueue<CollisionEvent> {
            var modifiedQueue = collisionQueue
            collidableEntity.getPotentialColliders(collisionGrid)
                .forEach { otherEntity ->
                    val timeRslt = collidableEntity.getCollisionTime(otherEntity)
                    if ((timeRslt >= 0f) && (timeRslt <= (subDt - dtProgress))) {
                        val candidateEvent = CollisionEvent(
                            arrayOf(collidableEntity as CollidableEntity, otherEntity),
                            dtProgress + timeRslt
                        )
                        if (modifiedQueue.none { candidateEvent.equals(it) }) {
                            if (!modifiedQueue.offer(candidateEvent)) {
                                modifiedQueue = resizeQueue(modifiedQueue)
                                modifiedQueue.add(candidateEvent)
                            }
                        }

                    }
                }
            return modifiedQueue
        }

        private fun resizeQueue(inputQueue: PriorityQueue<CollisionEvent>): PriorityQueue<CollisionEvent> {
            val biggerQueue = PriorityQueue<CollisionEvent>(
                inputQueue.size * 2,
                CollisionEvent.CollisionEventComparer
            )
            inputQueue.forEach { existingEvent ->
                biggerQueue.add(
                    existingEvent
                )
            }
            return biggerQueue
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
            ))*(1f)
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


