package com.example.android.ballBounce.viewModel

import androidx.lifecycle.ViewModel
import com.example.android.ballBounce.gameSimulation.EntitySimulationState
import com.example.android.ballBounce.gameSimulation.EntitySimulator
import com.example.android.ballBounce.paintableShapes.PaintableShapeList
import com.example.android.ballBounce.utility.Vector

class MainActivityViewModel() : ViewModel() {

    private var entitySim: EntitySimulator? = null
    private val playerBarrierBuffer = mutableListOf<Pair<Vector, Vector>>()
    private var gravityVector = Vector.zero()

    fun resizeSpace(w: Int, h: Int) {

    }

    fun flushPlayerLineBuffer() {
        playerBarrierBuffer.forEach { barrier -> entitySim?.addPlayerBarrier(barrier) }
        playerBarrierBuffer.clear()
    }

    fun initialize(screenDims: Vector) {
        entitySim = entitySim ?: EntitySimulator()
        entitySim!!.initialize(screenDims)
    }

    fun stepModel(dt: Float): Unit {
        entitySim?.updateState(dt)
    }

    fun takeGravity(gravVector: Vector?) {
        if (gravVector != null) {
            gravityVector = Vector(-1f*gravVector.x, gravVector.y)
        }
    }

    fun reportGravity(gravVector: Vector) {
        if (gravityVector != null) {
            entitySim?.takeGravity(gravVector)
        }
    }

    fun getDrawObjects(): PaintableShapeList? {
        return entitySim?.getPaintableObjects()
    }


    fun tryRunGame() {
        if (entitySim?.entitySimulationState == EntitySimulationState.INITIALIZED) {
            entitySim?.entitySimulationState = EntitySimulationState.RUNNING
        }
    }
}