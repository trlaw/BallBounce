
package com.example.android.ballBounce.gameSimulation
/*
import com.example.android.ballCorral.utility.invokeAllPairs
import kotlin.Float.Companion.POSITIVE_INFINITY
import kotlin.math.abs
import kotlin.math.pow

const val MAX_PASSES = 30
const val MAX_VELOCITY = 180f
const val MIN_STEP_RATIO =
    0.005f //Corresponding timeStep = (MIN_STEP_RATIO*minimumBallRadius)/MAX_VELOCITY
const val BALL_COR = 0.98f
//const val INCHING_RATIO =
//    0.005f //After collision ball centers forced to distance of (SUM_OF_RADII*(1+INCHING_RATIO))

enum class StepDirection {
    FORWARD, BACKWARD
}

class CollisionStepSizeSolver : BallSimulationSolver() {
    override fun updateState(ballList: MutableList<Ball>, endTime: Float): MutableList<Ball> {

        //Setup variables
        var workingList: MutableList<Ball> =
            (ballList.map { ball -> ball.copyBall() }).toMutableList()
        val minDt = calcMinDt(workingList)

        //Scale velocities if exceed MAX_VELOCITY
        limitVelocities(workingList)

        //Iteration variables
        var collisionList: MutableList<Pair<Ball, Ball>>
        var lastTime = 0f
        var searchTime = endTime
        for (i in 0 until MAX_PASSES) {
            limitVelocities(workingList)
            workingList.forEach { ball -> ball.travel(searchTime - lastTime) }
            collisionList = getCollisions(workingList)
            if (searchTime == endTime) {
                if (collisionList.size == 0) {
                    //No collisions and end of step.  Work is done.
                    return workingList
                } else if (collisionList.size == 1) {
                    //One collision and end of step.  Process collision then work is done.
                    handleCollisions(collisionList)
                    return workingList
                } else if ((searchTime - lastTime) <= minDt) {
                    //Too many collisions but have to terminate since at iteration limit
                    handleCollisions(collisionList)
                    return workingList
                } else {
                    //Not at iteration limit, go backwards by half of forward jump
                    val delta = (searchTime - lastTime) / (2f)
                    lastTime = searchTime
                    searchTime -= delta
                    continue
                }
            } else {
                if (collisionList.size == 0) {
                    //Found zero collision point, jump halfway to end
                    lastTime = searchTime
                    searchTime = endTime - (endTime - searchTime) / (2f)
                    continue
                } else if (collisionList.size == 1) {
                    //Process single collision and jump halfway to end
                    handleCollisions(collisionList)
                    lastTime = searchTime
                    searchTime = endTime - (endTime - searchTime) / (2f)
                    continue
                } else if (abs(searchTime - lastTime) < minDt) {
                    //Too many collisions but at iteration limit.
                    //Handle collisions and jump halfway to end.
                    handleCollisions(collisionList)
                    lastTime = searchTime
                    searchTime = endTime - (endTime - searchTime) / (2f)
                    continue
                } else {
                    //Too many collisions but not at iteration limit.
                    //Jump backwards by half of previous jump.
                    val delta = (lastTime - searchTime) / (2f)
                    lastTime = searchTime
                    searchTime -= delta
                    continue
                }
            }
        }
        //Global iteration limit reached.
        //Handle collisions at current time step and end time and return.
        limitVelocities(workingList)
        collisionList = getCollisions(workingList)
        handleCollisions(collisionList)
        workingList.forEach { ball -> ball.travel(endTime - lastTime) }
        collisionList = getCollisions(workingList)
        handleCollisions(collisionList)
        return workingList
    }

    private fun limitVelocities(workingList: List<Ball>): Unit {
        //Scale velocities if exceed MAX_VELOCITY
        return workingList.forEach { ball ->
            if (ball.velocity.mag() > MAX_VELOCITY) {
                ball.velocity = ball.velocity.times(MAX_VELOCITY / (ball.velocity.mag()))
            }
        }
    }

    private fun handleCollisions(collisionList: MutableList<Pair<Ball, Ball>>) {
        collisionList.forEach { collision ->
            handleCollision(
                collision.first,
                collision.second
            )
        }
    }

    private fun handleCollision(ball1: Ball, ball2: Ball) {


    }

    private fun getCollisions(workingList: List<Ball>): MutableList<Pair<Ball, Ball>> {
        val collisionList = mutableListOf<Pair<Ball, Ball>>()
        invokeAllPairs(workingList.size) { i, j ->
            if (workingList[i].collided(workingList[j])) {
                collisionList.add(Pair(workingList[i], workingList[j]))
            }
        }
        return collisionList
    }

    private fun calcMinDt(workingList: List<Ball>): Float {
        return (MIN_STEP_RATIO / MAX_VELOCITY) * workingList.fold(
            POSITIVE_INFINITY,
            { minRadius: Float, ball: Ball ->
                if (ball.radius < minRadius) ball.radius else minRadius
            })
    }

    private fun getMinRadius(workingList: List<Ball>): Float {
        return (MIN_STEP_RATIO / MAX_VELOCITY) * workingList.fold(
            POSITIVE_INFINITY,
            { minRadius: Float, ball: Ball ->
                if (ball.radius < minRadius) ball.radius else minRadius
            })
    }

}*/
