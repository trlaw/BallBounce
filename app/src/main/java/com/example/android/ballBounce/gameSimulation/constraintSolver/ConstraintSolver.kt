package com.example.android.ballBounce.gameSimulation.constraintSolver

import android.util.Log
import com.example.android.ballBounce.gameSimulation.CollisionGrid
import com.example.android.ballBounce.gameSimulation.gameEntities.BallEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.BarrierEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.CollidableEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.GameEntity
import kotlin.math.roundToLong
import kotlin.system.measureNanoTime

const val ITERATIONS_PER_CALL = 2
const val IDLE_CONSTRAINT_LIFETIME = 10.0
const val REPORT_INTERVAL_SOLVER = 50

class ConstraintSolver {
    private val constraintCache =
        HashMap<Pair<CollidableEntity, CollidableEntity>, AbstractConstraint>()

    private var reportCount = 0
    private var ballCount = 0
    private var constraintAddTime = 0.0
    private var impulseEvaluateTime = 0.0
    private var impulseApplicationTime = 0.0
    private var constraintPurgeTime = 0.0

    fun updateVelocities(collisionGrid: CollisionGrid, gameEntities: List<GameEntity>, dt: Double) {

        if (reportCount == REPORT_INTERVAL_SOLVER) {
            Log.d(
                "Ball Performance", "${ballCount} balls: " +
                        "${(constraintAddTime / (ballCount * REPORT_INTERVAL_SOLVER)).roundToLong()} adding constraints ns/ball " +
                        "${(impulseEvaluateTime / (ballCount * REPORT_INTERVAL_SOLVER)).roundToLong()} impulse compute ns/ball"
            )
            Log.d(
                "Ball Performance", "${ballCount} balls: " +
                        "${(impulseApplicationTime / (ballCount * REPORT_INTERVAL_SOLVER)).roundToLong()} impulse apply ns/ball " +
                        "${(constraintPurgeTime / (ballCount * REPORT_INTERVAL_SOLVER)).roundToLong()} constraint purge ns/ball"
            )
            reportCount = 0
            constraintAddTime = 0.0
            impulseEvaluateTime = 0.0
            impulseApplicationTime = 0.0
            constraintPurgeTime = 0.0
        }

        constraintAddTime += measureNanoTime { addRequiredConstraints(collisionGrid, gameEntities) }
        constraintCache.onEach { it.value.resetInitialQuantities() }
        ballCount = 0
        gameEntities.forEach {
            if (it is BallEntity) {
                it.applyGravityAcceleration(dt)
                ballCount++
            }
        }
        //applyCachedCorrections()
        repeat(ITERATIONS_PER_CALL) {
            var constraintsToApply = mutableListOf<AbstractConstraint>()
            constraintCache.onEach {if (!(it.value.satisfied())) {constraintsToApply.add(it.value)}  }

            impulseEvaluateTime += measureNanoTime {
                constraintsToApply.forEach {
                        it.evalImpulses(dt)
                }
            }

            impulseApplicationTime += measureNanoTime {
                constraintsToApply.forEach {
                        it.applyCorrectiveImpulses()
                }
            }
        }
        constraintPurgeTime += measureNanoTime { ageAndPurgeIdles(dt) }
        reportCount++
    }

    private fun applyCachedCorrections() {
        constraintCache.onEach {
            if (!(it.value.satisfied())) {
                it.value.applyWarmStartImpulses()
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