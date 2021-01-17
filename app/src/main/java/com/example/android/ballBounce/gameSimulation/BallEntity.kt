package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableCircle
import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.utility.Vector
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

const val INCHING_FACTOR = 0.01f

class BallEntity() : GameEntity(), CollidableEntity, PaintableEntity, MobileEntity,
    GravitySensitiveEntity {
    lateinit var position: Vector

    lateinit var velocity: Vector
    var radius: Float = 0f
    var colorIndex: Int = 0
    var cOr: Float = 1f //Bounciness factor
    var collisionGridCell: Pair<Int, Int>? = null
    var hasMaxSpeed: Boolean = false
    var maxSpeed: Float = 0f

    override fun travel(dt: Float): Unit {
        position = position.plus(velocity.times(dt))
    }

    fun copyBall(): BallEntity {
        return BallEntity().apply {
            position = this.position
            velocity = this.velocity
            radius = this.radius
            colorIndex = this.colorIndex
        }
    }

    override fun handleCollision(otherEntity: CollidableEntity) {
        when (otherEntity) {
            is BallEntity -> handleBallCollision(otherEntity)
            is BarrierEntity -> otherEntity.handleCollision(this)
            else -> return
        }
    }

    private fun handleBallCollision(otherBall: BallEntity) {
        //Position and velocity of otherBall
        val vel1minus2 = velocity.minus(otherBall.velocity)
        val pos1minus2 = position.minus(otherBall.position)

        //BallEntities collided at some earlier time if already overlapping.
        //Move balls backwards in time to the actual collision time
        /*
        val overlap = (radius + otherBall.radius) - pos1minus2.mag()
        var collisionRelDt = 0f
        if (overlap > 0) {
            //Get magnitude of velocity component of BallEntity 1 directly towards BallEntity 2
            val vRel = abs(vel1minus2.dot(pos1minus2.unitScaled()))
            collisionRelDt = (-1f) * (overlap / vRel)
            travel(collisionRelDt)
            otherBall.travel(collisionRelDt)
        }
        */

        //Update BallEntity velocity
        val vDot = vel1minus2.dot(pos1minus2.unitScaled())
        val collisionCoR: Float = min(cOr, otherBall.cOr)    //Arbitrary, sticky overrides bouncy
        val vMultiplier = collisionCoR * vDot
        velocity = velocity.plus(pos1minus2.unitScaled().times(-vMultiplier))
        otherBall.velocity = otherBall.velocity.plus(pos1minus2.unitScaled().times(vMultiplier))

        //Move balls forward to initial time, if moved backward
        /*
        if (overlap > 0) {
            travel(-collisionRelDt)
            otherBall.travel(-collisionRelDt)
        }
        */
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

    override fun collided(otherEntity: CollidableEntity): Boolean {
        return when (otherEntity) {
            is BallEntity -> {
                return collidedWithBall(otherEntity)
            }
            is BarrierEntity -> {
                return otherEntity.collided(this)
            }
            else -> false
        }
    }

    override fun reactToCollisions(collisionGrid: CollisionGrid) {
        getCollidedItems(collisionGrid)?.forEach { collidableEntity ->
            handleCollision(
                collidableEntity
            )
        }
    }

    override fun enforceCompatibility(collisionGrid: CollisionGrid): Boolean {
        val collidedItems = getCollidedItems(collisionGrid)
        if ((collidedItems == null) || (collidedItems.isEmpty())) {
            return true
        } else {
            collidedItems.forEach { it ->
                when (it) {
                    is BallEntity -> forceBallCompatibility(it, collisionGrid)
                    is BarrierEntity -> forceBarrierCompatibility(it, collisionGrid)
                }
            }
        }
        return false
    }

    fun refreshCollisionGridMark(collisionGrid: CollisionGrid) {
        this.unMarkCollisionGrid(collisionGrid)
        this.markCollisionGrid(collisionGrid)
    }

    /*
    fun forceBallCompatibility(otherBall: BallEntity, collisionGrid: CollisionGrid) {
        val ballDisp = otherBall.position.minus(this.position)
        val overlap =
            ((this.radius + otherBall.radius) - ballDisp.mag()) + (INCHING_FACTOR * this.radius)
        this.position = this.position.plus(ballDisp.unitScaled().times(-overlap))
        //Update collision grid
        this.refreshCollisionGridMark(collisionGrid)
        otherBall.refreshCollisionGridMark(collisionGrid)
    }
    */

    fun forceBarrierCompatibility(barrierEntity: BarrierEntity, collisionGrid: CollisionGrid) {
        val unitNorm = barrierEntity.barrierUnitNormal()
        val overlap =
            (this.radius + (barrierEntity.width / (2f))) - barrierEntity.pointBarrierDistance(
                position
            ) + (INCHING_FACTOR * this.radius)
        val jumpOneWay = position.plus(unitNorm.times(overlap))
        val jumpOtherWay = position.minus(unitNorm.times(overlap))
        position = if (jumpOneWay.mag() > jumpOtherWay.mag()) {
            jumpOneWay
        } else {
            jumpOtherWay
        }
        this.refreshCollisionGridMark(collisionGrid)
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
        return (barrierEntity.pointBarrierDistance(position)/(vRel.mag()))
    }

    private fun getCollisionTimeWithBall(otherBall: BallEntity): Float {
        val dR = this.position.minus(otherBall.position)
        val dV = this.velocity.minus(otherBall.velocity)
        val coeffA = dR.x.pow(2)+dR.y.pow(2)-(this.radius+otherBall.radius).pow(2)
        val coeffB = (2f)*(dR.x*dV.x+dR.y*dV.y)
        val coeffC = dV.x.pow(2)+dV.y.pow(2)
        val discriminant = coeffB.pow(2)-4*coeffA*coeffC
        if (discriminant <= 0) {
            return -1.0f
        }
        return ((-coeffB)+sqrt(discriminant))/((2f)*coeffA)
    }

    private fun collidedWithBall(otherBall: BallEntity): Boolean {
        return position.minus(otherBall.position).mag() <= (radius + otherBall.radius)
    }

    override fun applyGravityDeltaV(gravityDeltaV: Vector) {
        velocity = velocity.plus(gravityDeltaV)
    }
}
