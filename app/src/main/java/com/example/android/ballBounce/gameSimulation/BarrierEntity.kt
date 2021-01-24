package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableLine
import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.utility.vectorMidpoint
import kotlin.math.abs

enum class SegmentLocation {
    TOP, LEAF
}

open class BarrierEntity : GameEntity(), PaintableEntity, CollidableEntity {
    var start: Vector? = null
    var end: Vector? = null
        set(value) {
            field = value
            if (start != null) {
                startToEnd = value?.minus(start!!)
            }
        }
    var startToEnd: Vector? = null
    var width: Float = 1f
    var collisionCells: MutableSet<Pair<Int, Int>>? = null //Cache for collision cells touched
    var unitNormal: Vector? = null

    override fun getPaintableShape(): PaintableShape {
        return PaintableLine(start!!, end!!, width)
    }

    override fun collided(otherEntity: CollidableEntity): Boolean {
        return when (otherEntity) {
            is BallEntity -> collidedWithBall(otherEntity)
            else -> false
        }
    }

    private fun collidedWithBall(otherEntity: BallEntity): Boolean {
        val otherPosition = otherEntity.position
        val otherRadius = otherEntity.radius
        val pointLineDist = pointBarrierDistance(otherPosition)
        if (pointLineDist < (width / (2f) + otherRadius)) {
            //Line is finite length, check projection onto line
            val startToPoint = otherPosition.minus(start!!)
            val projectionCoordinate = startToPoint.dot(startToEnd!!.unitScaled())
            if ((projectionCoordinate >= -otherRadius) && (projectionCoordinate <= (startToEnd!!.mag()+otherRadius))) {
                return true
            }
        }
        return false
    }

    override fun handleCollision(otherEntity: CollidableEntity, permittedDt: Float) {
        when (otherEntity) {
            is BallEntity -> handleCollisionWithBall(otherEntity)
            else -> return
        }
    }

    fun pointBarrierDistance(point: Vector): Float {
        val lineToPoint = point.minus(start!!)
        return abs(lineToPoint.dot(barrierUnitNormal()))
    }

    fun lineEndToPoint(point: Vector): Vector {
        return point.minus(start!!)
    }

    fun barrierUnitNormal(): Vector {
        if (unitNormal == null) {
            unitNormal = end!!.minus(start!!).unitNormal()
        }
        return unitNormal!!
    }

    private fun handleCollisionWithBall(ballEntity: BallEntity) {
        //Get velocity normal, and tangent, to wall
        val vNormToBarrier =
            barrierUnitNormal().times(ballEntity.velocity.dot(barrierUnitNormal()))
        val vTangentToBarrier = ballEntity.velocity.minus(vNormToBarrier)

        //Adjust velocity for bounce
        ballEntity.velocity = vTangentToBarrier.plus(vNormToBarrier.times(-ballEntity.cOr))

    }

    override fun markCollisionGrid(collisionGrid: CollisionGrid) {
        if (collisionCells == null) {
            collisionCells = mutableSetOf()
            //Check all lines to find overlap.  A cell which is completely enclosed by a barrier
            //will not contain any other entities against which collision could occur
            getEdges().forEach { it ->
                collisionCells!!.addAll(
                    markLineOnCollisionGrid(
                        collisionGrid,
                        it.first,
                        it.second,
                        SegmentLocation.TOP
                    )
                )
            }
        } else {

            collisionCells!!.forEach { it ->
                collisionGrid.markGridCell(it, this)

            }
        }
    }

    override fun unMarkCollisionGrid(collisionGrid: CollisionGrid) {
        if (collisionCells != null) {
            collisionCells!!.forEach { it -> collisionGrid.unMarkGridCell(it, this) }
        }
        collisionCells = null
    }

    private fun getEdges(): List<Pair<Vector, Vector>> {
        val vWidthOffset = barrierUnitNormal().times(width / (2f))
        val outList = mutableListOf<Pair<Vector, Vector>>()
        val vertices = arrayOf(
            start!!.plus(vWidthOffset),
            start!!.minus(vWidthOffset),
            end!!.minus(vWidthOffset),
            end!!.plus(vWidthOffset)
        )
        for (i in 0 until 4) {
            outList.add(Pair(vertices[i], vertices[(i + 1) % 4]))
        }
        return outList
    }

    private fun markLineOnCollisionGrid(
        collisionGrid: CollisionGrid,
        lineStart: Vector,
        lineEnd: Vector,
        location: SegmentLocation
    ): Set<Pair<Int, Int>> {

        var cellSet = mutableSetOf<Pair<Int, Int>>()

        //Mark endpoints according to position on call tree
        if (location == SegmentLocation.TOP) {
            val topStartKey = collisionGrid.getKeyForPosition(lineStart)
            collisionGrid.markGridCell(topStartKey, this)
            cellSet.add(topStartKey)
            val topEndKey = collisionGrid.getKeyForPosition(lineEnd)
            collisionGrid.markGridCell(topEndKey, this)
            cellSet.add(topEndKey)
        }

        //Termination conditions
        val startKey = collisionGrid.getKeyForPosition(lineStart)
        val endKey = collisionGrid.getKeyForPosition(lineEnd)
        if (collisionGrid.cellsAreAdjacent(startKey, endKey) || collisionGrid.cellsAreDiagonal(
                startKey, endKey
            ) || (startKey == endKey)
        ) {
            return cellSet
        }

        //Mark midpoint and check interior segments
        val midPoint = vectorMidpoint(lineStart, lineEnd)
        val midKey = collisionGrid.getKeyForPosition(midPoint)
        collisionGrid.markGridCell(midKey, this)
        cellSet.add(midKey)
        cellSet.addAll(
            markLineOnCollisionGrid(
                collisionGrid,
                lineStart,
                midPoint,
                SegmentLocation.LEAF
            )
        )
        cellSet.addAll(
            markLineOnCollisionGrid(
                collisionGrid,
                midPoint,
                lineEnd,
                SegmentLocation.LEAF
            )
        )
        return cellSet
    }
}
