package com.example.android.ballBounce.gameSimulation.constraintSolver

import com.example.android.ballBounce.gameSimulation.gameEntities.BallEntity
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.utility.VectorN
import kotlin.math.min

const val BALL_BALL_BETA = 0.5
const val SLOP_FACTOR = 0.02

class BallBallConstraint(val ballA: BallEntity, val ballB: BallEntity, idleLife: Double) :
    AbstractConstraint(idleLife) {

    var cDotInit: Double = 0.0
    var impulses = VectorN(*Array(4) { 0.0 }.toDoubleArray())
    var cachedTotalImpulses = VectorN(*Array(4) { 0.0 }.toDoubleArray())

    override fun applyWarmStartImpulses() {
        ballA.velocity =
            ballA.velocity.plus(
                Vector(
                    cachedTotalImpulses.component(1),
                    cachedTotalImpulses.component(3)
                )
            )
        ballB.velocity =
            ballB.velocity.plus(
                Vector(
                    cachedTotalImpulses.component(0),
                    cachedTotalImpulses.component(2)
                )
            )

        //Reset cached impulse for next time step
        cachedTotalImpulses = VectorN(*Array(4) { 0.0 }.toDoubleArray())
    }

    override fun satisfied(): Boolean {
        return if (evalC() >= 0.0 || evalCdot() > 0.0) {
            true
        } else {
            ballA.reduceGravity = true
            ballB.reduceGravity = true
            false
        }
    }

    override fun evalC(): Double {
        return ballB.position.minus(ballA.position).mag() - (ballB.radius + ballA.radius)
    }

    override fun evalCdot(): Double {
        return deltaR().innerProduct(vN())
    }

    override fun evalImpulses(dt: Double) {
        val dR = deltaR()
        val thisC = evalC()
        val lambda =
            ((-1.0) * (evalCdot() + coeffRest() * cDotInit +
                    (BALL_BALL_BETA / dt)
                    * (if (thisC < (-1.0) * ballA.radius * SLOP_FACTOR)
                (thisC + ballA.radius * SLOP_FACTOR) else 0.0)) /
                    dR.twoNormSquared())
        impulses = dR.times(lambda)
    }

    override fun applyCorrectiveImpulses() {
        ballA.velocity =
            ballA.velocity.plus(Vector(impulses.component(1), impulses.component(3)))
        ballB.velocity =
            ballB.velocity.plus(Vector(impulses.component(0), impulses.component(2)))
        timeSinceLastActive = 0.0
        //Store accumulated impulse
        cachedTotalImpulses = cachedTotalImpulses.plus(impulses)!!
    }

    override fun resetInitialQuantities() {
        cDotInit = evalCdot()
    }

    private fun vN(): VectorN {
        return VectorN(ballB.velocity.x, ballA.velocity.x, ballB.velocity.y, ballA.velocity.y)
    }

    private fun deltaR(): VectorN {
        val rBA = ballB.position.minus(ballA.position)
        return VectorN(rBA.x, -rBA.x, rBA.y, -rBA.y).times(1.0 / rBA.mag())
    }

    private fun coeffRest(): Double {
        return min(ballA.cOr, ballB.cOr)
    }
}