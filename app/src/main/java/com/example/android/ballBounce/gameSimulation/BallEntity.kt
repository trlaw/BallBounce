package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableCircle
import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.utility.quadraticSolution
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.min
import kotlin.math.pow


class BallEntity() : GameEntity(), CollidableEntity, PaintableEntity, MobileEntity,
    GravitySensitiveEntity {

    lateinit var position: Vector
    lateinit var velocity: Vector
    var radius: Float = 0f
    var colorIndex: Int = 0
    var cOr: Float = 1f //Bounciness factor
    var collisionGridCell: Pair<Int, Int>? = null
    val maxSpeed = 10f
    var gravity = Vector(0f,0f)

    override fun travel(dt: Float): Unit {
        if (velocity.mag() > maxSpeed) {
            velocity = velocity.times(maxSpeed / velocity.mag())
        }
        position = position.plus(velocity.times(dt))
        velocity = velocity.plus(gravity.times(dt))

    }

    fun copyBall(): BallEntity {
        return BallEntity().apply {
            position = this.position
            velocity = this.velocity
            radius = this.radius
            colorIndex = this.colorIndex
            cOr = this.cOr
            collisionGridCell = this.collisionGridCell
        }
    }

    override fun handleCollision(otherEntity: CollidableEntity, dt: Float) {
        when (otherEntity) {
            is BallEntity -> handleBallCollision(otherEntity, dt)
            is BarrierEntity -> otherEntity.handleCollision(this, dt)
            else -> return
        }
    }

    private fun handleBallCollision(otherBall: BallEntity, dt: Float) {
        //Position and velocity of otherBall
        val vel1minus2 = velocity.minus(otherBall.velocity)
        val pos1minus2 = position.minus(otherBall.position)

        //Update BallEntity velocity
        val vDot = vel1minus2.dot(pos1minus2.unitScaled())

        val collisionCoR: Float =
            min(cOr, otherBall.cOr)    //Arbitrary, sticky overrides bouncy
        val vMultiplier = (collisionCoR) * vDot
        velocity = velocity.plus(pos1minus2.unitScaled().times(-vMultiplier))
        otherBall.velocity = otherBall.velocity.plus(pos1minus2.unitScaled().times(vMultiplier))

        //Undo effect of gravity if there was a collision in last time step
        velocity = velocity.minus(gravity.times(dt*100f))
        otherBall.velocity = otherBall.velocity.minus(otherBall.gravity.times(dt*1f))

        position = position.minus(gravity.times(dt.pow(2)))
        otherBall.position = otherBall.position.minus(otherBall.gravity.times(dt.pow(2)))


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

    override fun getCollisionTime(other: CollidableEntity): Float {
        return when (other) {
            is BallEntity -> getCollisionTimeWithBall(other)
            is BarrierEntity -> getCollisionTimeWithBarrier(other)
            else -> -1.0f
        }
    }

    private fun getCollisionTimeWithBarrier(barrierEntity: BarrierEntity): Float {
        val vRel = barrierEntity.barrierUnitNormal()
            .times(this.velocity.dot(barrierEntity.barrierUnitNormal()))

        //If velocity parallel to barrier or away, no collision possible
        if (vRel.dot(barrierEntity.lineEndToPoint(position)) >= 0f) {
            return -1.0f
        }

        val barrierDist: Float =
            barrierEntity.pointBarrierDistance(position) - (radius + barrierEntity.width / (2f))

        return (if (barrierDist < 0) 0f else barrierDist) / vRel.mag()
    }

    private fun getCollisionTimeWithBall(otherBall: BallEntity): Float {
        val dR = this.position.minus(otherBall.position)
        val dV = this.velocity.minus(otherBall.velocity)

        //Immediate bounce away if balls found on top of each other
        if (this.overlaps(otherBall) && ballsClosingDistance(dR, dV, 0f)) {
            return 0f
        }

        val coeffC = dR.x.pow(2) + dR.y.pow(2) - (this.radius + otherBall.radius).pow(2)
        val coeffB = (2f) * (dR.x * dV.x + dR.y * dV.y)
        val coeffA = dV.x.pow(2) + dV.y.pow(2)

        val roots = quadraticSolution(coeffA, coeffB, coeffC)
        if (roots.first == null) {
            return -1.0f
        }
        var potentialSolns = arrayOf(POSITIVE_INFINITY, POSITIVE_INFINITY)
        if ((roots.first!! >= 0f) && ballsClosingDistance(dR, dV, roots.first!!)) {
            potentialSolns[0] = roots.first!!
        }
        if ((roots.second!! >= 0f) && ballsClosingDistance(dR, dV, roots.second!!)) {
            potentialSolns[1] = roots.second!!
        }
        val bestSoln = min(potentialSolns[0], potentialSolns[1])
        return if (bestSoln == POSITIVE_INFINITY) -1.0f else bestSoln
    }

    override fun applyGravityDeltaV(gravityVect: Vector) {
        gravity = gravityVect
    }

    fun ballsClosingDistance(dR: Vector, dV: Vector, dt: Float): Boolean {
        return (0 > ((dR.x + dR.y) + dt * (dV.dot(dV))))
    }

    fun overlaps(otherBall: BallEntity): Boolean {
        val posDifference = this.position.minus(otherBall.position)
        return posDifference.mag() < (this.radius + otherBall.radius)
    }
}
