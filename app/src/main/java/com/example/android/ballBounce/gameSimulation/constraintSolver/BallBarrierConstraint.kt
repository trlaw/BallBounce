package com.example.android.ballBounce.gameSimulation.constraintSolver

import com.example.android.ballBounce.gameSimulation.BarrierEntity
import com.example.android.ballBounce.gameSimulation.gameEntities.BallEntity
import com.example.android.ballBounce.utility.Vector
import kotlin.math.pow
import kotlin.math.sqrt

const val BALL_BARRIER_BETA = 0.1

class BallBarrierConstraint(val ball: BallEntity, val barrier: BarrierEntity, idleLife: Double) :
    AbstractConstraint(idleLife) {


    override fun satisfied(): Boolean {
        TODO("Not yet implemented")
    }

    override fun evalC(): Double {
        TODO("Not yet implemented")
    }

    override fun evalCdot(): Double {
        TODO("Not yet implemented")
    }

    override fun evalImpulses(dt: Double) {
        TODO("Not yet implemented")
    }

    override fun applyCorrectiveImpulses() {
        TODO("Not yet implemented")
    }

    private val sBA: Vector by lazy { barrier.end!!.minus(barrier.start!!) }
    private val sAdotSb: Double by lazy { barrier.start!!.dot(barrier.end!!)}
    private val sAdotsBAsqr: Double by lazy { (barrier.start!!.dot(sBA)).pow(2) }
    private val vectorK: Vector by lazy {Vector(sBA.y,-1.0*sBA.x)}

    private fun jacobian(): Vector {
        return vectorK.times(gamma())
    }

    private fun gamma(): Double {
        return (rCrossSba() / (sBA.mag() * phi()))
    }

    private fun phi(): Double {
        return sqrt((rCrossSba()).pow(2) + sAdotsBAsqr)
    }

    private fun rCrossSba(): Double {
        return sBA.y * ball.position.x - sBA.x * ball.position.y
    }
}