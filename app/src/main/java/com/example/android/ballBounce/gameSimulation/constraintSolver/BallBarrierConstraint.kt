package com.example.android.ballBounce.gameSimulation.constraintSolver

import com.example.android.ballBounce.gameSimulation.gameEntities.BallEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.BarrierEntity
import com.example.android.ballBounce.utility.Vector
import kotlin.math.abs
import kotlin.math.pow

const val BALL_BARRIER_BETA = 0.00025

class BallBarrierConstraint(
    private val ball: BallEntity,
    private val barrier: BarrierEntity,
    idleLife: Double
) :
    AbstractConstraint(idleLife) {

    private var vInit: Vector = Vector.zero()
    var impulse = Vector.zero()

    override fun satisfied(): Boolean {
        // Log.d("ConstraintState", "Ball Position: ${ball.position.x}, ${ball.position.y}")
        //Log.d("ConstraintState", "Ball Velocity: ${ball.velocity.x}, ${ball.velocity.y}")
        //Log.d("ConstraintState", "C: ${evalC()}")
        //Log.d("ConstraintState", "Cdot: ${evalCdot()}")
        val locationDot = ball.position.dot(sBA)
        //Check if ball center inside normal projection of barrier
        if ((locationDot < lowerDotRange) || (locationDot > upperDotRange)) {
            return true
        }
        return if ((evalC() < 0) && (evalCdot() < 0)) {
            ball.reduceGravity = true
            false
        } else {
            true
        }
    }

    override fun evalC(): Double {
        return abs(
            ball.position.minus(barrier.start!!).dot(sBAnormal)
        ) - (barrier.width / 2 + ball.radius)
    }

    override fun evalCdot(): Double {
        return jacobian().dot(ball.velocity)
    }

    override fun evalImpulses(dt: Double) {
        val lambda =
            (-1.0) *
                    ((jacobian().dot(ball.velocity.plus(vInit.times(ball.cOr))) +
                            (BALL_BARRIER_BETA / dt) * evalC()) / (jacobian().mag().pow(2)))
        impulse = jacobian().times(lambda)
    }

    override fun resetInitialQuantities() {
        vInit = ball.velocity
    }

    override fun applyCorrectiveImpulses() {
        ball.velocity = ball.velocity.plus(impulse)
        timeSinceLastActive = 0.0
    }

    private val sBA: Vector by lazy { barrier.end!!.minus(barrier.start!!) }
    private val gamma: Double by lazy { sBA.y / sBA.mag() }
    private val sBAnormal: Vector by lazy {
        Vector(
            gamma,
            if (sBA.y == 0.0) 1.0 else (-1.0) * gamma * (sBA.x / sBA.y)
        )
    }
    private val sAdotSb: Double by lazy { barrier.start!!.dot(barrier.end!!) }
    private val lowerDotRange: Double by lazy {
        (-1.0) * (barrier.start!!.dot(barrier.start!!) - sAdotSb)
    }
    private val upperDotRange: Double by lazy {
        barrier.end!!.dot(barrier.end!!) - sAdotSb
    }

    private fun jacobian(): Vector {
        return sBAnormal.times(
            if (ball.position.minus(barrier.start!!).dot(sBAnormal) < 0.0) -1.0 else 1.0
        )
    }

}