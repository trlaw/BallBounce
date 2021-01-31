package com.example.android.ballBounce.gameSimulation.gameEntities

import com.example.android.ballBounce.gameSimulation.CollisionGrid
import com.example.android.ballBounce.paintableShapes.PaintableCircle
import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.utility.quadraticSolution
import com.example.android.ballBounce.utility.saferMinus
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.min
import kotlin.math.pow

const val NEGATIVE_TIME_EPS = -0.0001
const val GRAVITY_REDUCTION_ON_COLLISION = 0.01

class BallEntity() : GameEntity(), CollidableEntity, PaintableEntity, MobileEntity,
    GravitySensitiveEntity {

    lateinit var position: Vector
    lateinit var velocity: Vector
    var radius: Double = 0.0
    var colorIndex: Int = 0
    var cOr: Double = 1.0 //Bounciness factor
    var collisionGridCell: Pair<Int, Int>? = null
    val maxSpeed = 20.0
    var gravity = Vector(0.0, 0.0)
    var reduceGravity = false

    override fun travel(dt: Double): Unit {

        if (velocity.mag() > maxSpeed) {
            velocity = velocity.times(maxSpeed / velocity.mag())
        }
        //if (velocity.times(dt).mag() > (0.01*radius)) {
        position = position.plus(velocity.times(dt))
        //}
    }

    fun applyGravityAcceleration(dt: Double) {
        velocity =
            velocity.plus(
                gravity.times(
                    dt * (if (reduceGravity) GRAVITY_REDUCTION_ON_COLLISION else 1.0)
                )
            )
        reduceGravity = false
    }

    override fun handleCollision(otherEntity: CollidableEntity, basedOnDt: Double) {
        when (otherEntity) {
            is BallEntity -> handleBallCollision(otherEntity, basedOnDt)
            is BarrierEntity -> otherEntity.handleCollision(this, basedOnDt)
            else -> return
        }
    }

    private fun handleBallCollision(otherBall: BallEntity, basedOnDt: Double) {
        //Position and velocity of otherBall
        val vel1minus2 = velocity.minus(otherBall.velocity)
        val pos1minus2 = position.minus(otherBall.position)

        //Update BallEntity velocity
        val vDot = vel1minus2.dot(pos1minus2.unitScaled())
        if (vDot < 0) {
            val collisionCoR: Double =
                min(cOr, otherBall.cOr)    //Arbitrary, sticky overrides bouncy
            val vMultiplier = (collisionCoR) * vDot
            velocity = velocity.plus(pos1minus2.unitScaled().times(-vMultiplier))
            otherBall.velocity = otherBall.velocity.plus(pos1minus2.unitScaled().times(vMultiplier))
        }
    }

    override fun collided(otherEntity: CollidableEntity): Boolean {
        return when (otherEntity) {
            is BallEntity -> collidedWithBall(otherEntity)
            is BarrierEntity -> otherEntity.collided(this)
            else -> false
        }
    }

    private fun collidedWithBall(ballEntity: BallEntity): Boolean {
        return (position.minus(ballEntity.position).mag()) <= (this.radius + ballEntity.radius)
    }

    override fun markCollisionGrid(collisionGrid: CollisionGrid) {
        val cell = collisionGrid.getKeyForPosition(position)
        collisionGrid.markGridCell(cell, this)
        collisionGridCell = cell
    }

    override fun unMarkCollisionGrid(collisionGrid: CollisionGrid) {
        collisionGrid.unMarkGridCell(collisionGridCell, this)
        collisionGridCell = null
    }

    override fun getPaintableShape(): PaintableShape {
        return PaintableCircle(position, radius, colorIndex)
    }

    fun refreshCollisionGridMark(collisionGrid: CollisionGrid) {
        this.unMarkCollisionGrid(collisionGrid)
        this.markCollisionGrid(collisionGrid)
    }

    override fun getPotentialColliders(collisionGrid: CollisionGrid): List<CollidableEntity> {
        val potentialColliders = mutableListOf<CollidableEntity>()
        if (collisionGridCell == null) {
            return potentialColliders
        }
        collisionGrid.getCollisionKeys(collisionGridCell!!)
            .forEach { key ->
                collisionGrid.getCellEntities(key)?.let { collidableList ->
                    collidableList.forEach {
                        if ((!potentialColliders.contains(it)) && (it !== this)) {
                            potentialColliders.add(it)
                        }
                    }
                }
            }
        return potentialColliders
    }

    override fun getCollisionTime(other: CollidableEntity): Double {
        return when (other) {
            is BallEntity -> getCollisionTimeWithBall(other)
            is BarrierEntity -> getCollisionTimeWithBarrier(other)
            else -> -1.0
        }
    }

    private fun getCollisionTimeWithBarrier(barrierEntity: BarrierEntity): Double {
        val vRel = barrierEntity.barrierUnitNormal()
            .times(this.velocity.dot(barrierEntity.barrierUnitNormal()))

        //If velocity parallel to barrier or away, no collision possible
        if (vRel.dot(barrierEntity.lineEndToPoint(position)) >= 0.0) {
            return -1.0
        }

        val barrierDist: Double =
            (barrierEntity.pointBarrierDistance(position)
                .pow(2) - (radius + barrierEntity.width / (2.0)).pow(2)) / (barrierEntity.pointBarrierDistance(
                position
            ) + radius + barrierEntity.width / (2.0))

        return (if (barrierDist < 0.0) 0.0 else barrierDist) / vRel.mag()
    }

    private fun getCollisionTimeWithBall(otherBall: BallEntity): Double {
        val dR = this.position.minus(otherBall.position)
        val dV = this.velocity

        //Do not register collision if balls moving away from each other
        if (ballsSeparating(dR, dV, 0.0)) {
            return -1.0
        }

        val coeffC = saferMinus(
            dR.x.pow(2) + dR.y.pow(2),
            (this.radius + otherBall.radius).pow(2)
        )
        val coeffB = (2f) * (dR.x * dV.x + dR.y * dV.y)
        val coeffA = dV.x.pow(2) + dV.y.pow(2)

        val roots = quadraticSolution(coeffA, coeffB, coeffC)
        if (roots.first == null) {
            return -1.0
        }
        var potentialSolns = arrayOf(POSITIVE_INFINITY, POSITIVE_INFINITY)
        if ((roots.first!! >= NEGATIVE_TIME_EPS) && !ballsSeparating(dR, dV, roots.first!!)) {
            potentialSolns[0] = roots.first!!
        }
        if ((roots.second!! >= NEGATIVE_TIME_EPS) && !ballsSeparating(dR, dV, roots.second!!)) {
            potentialSolns[1] = roots.second!!
        }
        val bestSoln = min(potentialSolns[0], potentialSolns[1])
        //Log.d("BallPositions","")
        return if (bestSoln == POSITIVE_INFINITY) -1.0 else if (bestSoln < 0.0) 0.0 else bestSoln
    }

    override fun applyGravity(gravityVect: Vector) {
        gravity = gravityVect
    }

    private fun ballsSeparating(dR: Vector, dV: Vector, dt: Double): Boolean {
        return ((dR.x * dV.x + dR.y * dV.y) + dt * dV.dot(dV)) >= 0
    }

}
