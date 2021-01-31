package com.example.android.ballBounce.gameSimulation.constraintSolver

import com.example.android.ballBounce.gameSimulation.CollisionGrid
import com.example.android.ballBounce.gameSimulation.gameEntities.BallEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.BarrierEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.CollidableEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.GameEntity

const val ITERATIONS_PER_CALL = 6
const val IDLE_CONSTRAINT_LIFETIME = 20.0

class ConstraintSolver {
    private val constraintCache =
        HashMap<Pair<CollidableEntity, CollidableEntity>, AbstractConstraint>()

    fun updateVelocities(collisionGrid: CollisionGrid, gameEntities: List<GameEntity>, dt: Double) {
        addRequiredConstraints(collisionGrid, gameEntities)
        constraintCache.onEach { it.value.resetInitialQuantities() }
        gameEntities.forEach { if (it is BallEntity) {it.applyGravityAcceleration(dt)} }
        applyCachedCorrections()
        repeat (ITERATIONS_PER_CALL) {
            constraintCache.onEach {
                if (!(it.value.satisfied())) {
                    it.value.evalImpulses(dt)
                }
            }
            constraintCache.onEach {
                if (!(it.value.satisfied())) {
                    it.value.applyCorrectiveImpulses()
                }
            }
        }
        ageAndPurgeIdles(dt)
    }

    private fun applyCachedCorrections() {
        constraintCache.onEach {
            if (!(it.value.satisfied())) {
                it.value.applyCorrectiveImpulses()
            }
        }
    }

    private fun addRequiredConstraints(
        collisionGrid: CollisionGrid, gameEntities: List<GameEntity>
    ) {
        gameEntities.forEach { gameEntity ->
            if (gameEntity is BallEntity) {
                gameEntity.getPotentialColliders(collisionGrid).forEach {
                    when (it) {
                        is BallEntity -> addConstraint(gameEntity, it)
                        is BarrierEntity -> addConstraint(gameEntity, it)
                    }
                }
            }
        }
    }

    private fun cacheContainsPair(
        collidableOne: CollidableEntity,
        collidableTwo: CollidableEntity
    ): Boolean {
        return (
                constraintCache.containsKey(Pair(collidableOne, collidableTwo)))
                || (constraintCache.containsKey(Pair(collidableTwo, collidableOne)))
    }

    private fun ageAndPurgeIdles(dt: Double) {
        val keysOfEntriesToPurge = mutableListOf<Pair<CollidableEntity, CollidableEntity>>()
        constraintCache.onEach {
            it.value.age(dt)
            if (it.value.timedOut()) {
                keysOfEntriesToPurge.add(it.key)
            }
        }
        keysOfEntriesToPurge.forEach {
            constraintCache.remove(it)
        }
    }

    //Constraint addition overloads
    fun addConstraint(ballEntityOne: BallEntity, ballEntityTwo: BallEntity) {
        if (!cacheContainsPair(ballEntityOne, ballEntityTwo)) {
            constraintCache[Pair(ballEntityOne, ballEntityTwo)] = BallBallConstraint(
                ballEntityOne, ballEntityTwo, IDLE_CONSTRAINT_LIFETIME
            )
        }
    }

    fun addConstraint(barrierEntity: BarrierEntity, ballEntity: BallEntity) {
        if (!cacheContainsPair(barrierEntity, ballEntity)) {
            constraintCache[Pair(barrierEntity, ballEntity)] = BallBarrierConstraint(
                ballEntity, barrierEntity, IDLE_CONSTRAINT_LIFETIME
            )
        }
    }

    fun addConstraint(ballEntity: BallEntity, barrierEntity: BarrierEntity) {
        addConstraint(barrierEntity, ballEntity)
    }
}