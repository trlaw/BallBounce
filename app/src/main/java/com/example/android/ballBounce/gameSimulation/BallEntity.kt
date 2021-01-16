package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableCircle
import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.utility.Vector
import kotlin.math.abs
import kotlin.math.min

class BallEntity() : GameEntity(), CollidableEntity, PaintableEntity, MobileEntity {
    lateinit var position: Vector
    lateinit var velocity: Vector
    var radius: Float = 0f
    var checkedEntities = mutableSetOf<CollidableEntity>()
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
        val overlap = (radius + otherBall.radius) - pos1minus2.mag()
        var collisionRelDt = 0f
        if (overlap > 0) {
            //Get magnitude of velocity component of BallEntity 1 directly towards BallEntity 2
            val vRel = abs(vel1minus2.dot(pos1minus2.unitScaled()))
            collisionRelDt = (-1f) * (overlap / vRel)
            travel(collisionRelDt)
            otherBall.travel(collisionRelDt)
        }

        //Update BallEntity velocity
        val vDot = vel1minus2.dot(pos1minus2.unitScaled())
        val collisionCoR: Float = min(cOr, otherBall.cOr)    //Arbitrary, sticky overrides bouncy
        val vMultiplier = collisionCoR * vDot
        velocity = velocity.plus(pos1minus2.unitScaled().times(-vMultiplier))
        otherBall.velocity = otherBall.velocity.plus(pos1minus2.unitScaled().times(vMultiplier))

        //Move balls forward to initial time, if moved backward
        if (overlap > 0) {
            travel(-collisionRelDt)
            otherBall.travel(-collisionRelDt)
        }
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
                var rslt: Boolean
                //val ballBallCheckTime = measureNanoTime {
                    rslt = collidedWithBall(otherEntity)
                //}
                //Log.d("BallBallCheckTime", "$ballBallCheckTime ns")
                return rslt
            }
            is BarrierEntity -> {
                var rslt: Boolean
                //val ballBarrierCheckTime = measureNanoTime {
                    rslt = otherEntity.collided(this)
                //}
                //Log.d("BallBarrierCheckTime", "$ballBarrierCheckTime ns")
                return rslt
            }
            else -> false
        }
    }

    override fun reactToCollisions(collisionGrid: CollisionGrid) {
        checkedEntities.clear()
        if (collisionGridCell == null) {
            return
        }
        collisionGrid.getCollisionKeys(collisionGridCell!!).forEach { it ->
            collisionGrid.getCellEntities(it)
                ?.let { entities ->
                    entities.forEach { entityBeingChecked ->
                        if (!checkedEntities.contains(entityBeingChecked)) {
                            if ((entityBeingChecked !== this) && (this.collided(entityBeingChecked))) {
                                handleCollision(entityBeingChecked)
                            }
                            checkedEntities.add(entityBeingChecked)
                        }
                    }
                }
        }
        checkedEntities.clear()
    }

    private fun collidedWithBall(otherBall: BallEntity): Boolean {
        return position.minus(otherBall.position).mag() <= (radius + otherBall.radius)
    }

}