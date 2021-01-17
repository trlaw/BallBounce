package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableShapeList
import com.example.android.ballBounce.utility.Vector


const val BALL_ADD_TIME = 10f
const val BALL_LIMIT = 50
const val GRAVITY_STRENGTH = 0.02f
const val MAX_COMPATIBILITY_ITERATIONS = 100

class EntitySimulator() {

    private val gameBallFactory: BallEntityFactory = BallEntityFactory()
    private lateinit var collisionGrid: CollisionGrid
    private val entityList = mutableListOf<GameEntity>()
    var entitySimulationState = EntitySimulationState.NONE
    private var gameBoundary: GameBoundary? = null
    private var entitiesToRemove = mutableSetOf<GameEntity>()
    private var restartFlag: Boolean = false
    var gravityVector = Vector.zero()
    var simTime = 0f
    var ballCount: Int = 0

    //Game Simulation Lifecycle Methods

    fun initialize(screenDims: Vector? = null) {
        entitySimulationState = EntitySimulationState.NONE

        if (!trySetupGameBoundary(screenDims)) {
            return
        }

        resetEntityLists()
        initCollisionGrid()
        addGameBarriers()

        entitySimulationState = EntitySimulationState.INITIALIZED
    }

    private fun restartIfRequested() {
        if (restartFlag) {
            restartFlag = false
            initialize()
        }
    }

    private fun scheduleRestart() {
        restartFlag = true
    }

    fun updateState(dt: Float) {

        if (entitySimulationState == EntitySimulationState.RUNNING) {

            //Add ball to game if spawn conditions satisfied
            tryAddBall()

            //Apply gravity to sensitive objects
            applyGravity(gravityVector.times(dt))

            //Advance mobile object positions
            updateMobileEntityPositions(dt)

            //Populate collision grid with mobile objects
            markCollisionGridWithMobileEntities()

            //Adjust mobile objects according to collisions
            processMobileEntityCollisions()

            //Enforce compatibility
            var iterNumber = 0
            while (iterNumber < MAX_COMPATIBILITY_ITERATIONS) {
                val rslt: Boolean  = entityList.all {
                    when (it) {
                        is MobileEntity -> it.enforceCompatibility(collisionGrid)
                        else -> true
                    }
                }
                if (rslt) {
                    break
                }
                iterNumber++
            }

            //Remove mobile entities from collision grid since position may change on next step
            unMarkMobileEntitiesFromGrid()

            removeMarkedEntities() //Remove entity list items marked for removal

            simTime += dt
        }
        restartIfRequested()
    }

    private fun applyGravity(deltaV: Vector) {
        for (gameEntity in entityList) {
            if (gameEntity is GravitySensitiveEntity) {
                gameEntity.applyGravityDeltaV(deltaV)
            }
        }
    }

    //State Update Helper Methods
    private fun markCollisionGridWithMobileEntities() {
        var i = 0
        while (i < entityList.size) {
            if ((entityList[i] is CollidableEntity) && (entityList[i] is MobileEntity)) {
                (entityList[i] as CollidableEntity).markCollisionGrid(collisionGrid)
            }
            i++
        }
    }

    private fun processMobileEntityCollisions() {
        entityList.forEach { it ->
            if (it is MobileEntity) {
                it.reactToCollisions(collisionGrid)
            }
        }
    }

    private fun unMarkMobileEntitiesFromGrid() {
        for (gameEntity in entityList) {
            if ((gameEntity is CollidableEntity) && (gameEntity is MobileEntity)) {
                gameEntity.unMarkCollisionGrid(collisionGrid)
            }
        }
    }

    private fun updateMobileEntityPositions(dt: Float) {
        entityList.forEach { it ->
            if (it is MobileEntity) {
                it.travel(dt)
            }
        }
    }

    private fun tryAddBall() {

        //Add ball only if enough time elapsed
        if (simTime < (ballCount * BALL_ADD_TIME)) {
            return
        }

        //Don't add if already at maximum allowed ball count
        if (ballCount >= BALL_LIMIT) {
            return
        }

        val newBall = gameBallFactory.create()
        gameBoundary!!.setSpawnState(newBall)

        //Add ball only if it will not collide with an existing entity
        if (entityList.none { gameEntity ->
                ((gameEntity is CollidableEntity) && (gameEntity.collided(newBall)))
            }) {
            addEntity(newBall)
            ballCount++
        }
    }

    //General GameEntity Lifecycle Methods
    private fun addEntity(gameEntity: GameEntity) {
        //Mark collision grid with static element if necessary
        if ((gameEntity is CollidableEntity) && !(gameEntity is MobileEntity)) {
            gameEntity.markCollisionGrid(collisionGrid)
        }
        entityList.add(gameEntity)
    }

    private fun markForRemoval(gameEntity: GameEntity) {
        if ((gameEntity is CollidableEntity) && (gameEntity !is MobileEntity)) {
            gameEntity.unMarkCollisionGrid(collisionGrid)
        }
        entitiesToRemove.add(gameEntity)
    }

    private fun removeMarkedEntities() {
        entityList.removeAll(entitiesToRemove)
        entitiesToRemove.clear()
    }

    //Accessors

    fun getPaintableObjects(): PaintableShapeList {
        val outputList = PaintableShapeList()
        if ((gameBoundary != null) && (gameBoundary!!.isValidGameBoundary())) {
            outputList.shapesUpperLeft = gameBoundary!!.lowerBound
            outputList.shapesLowerRight = gameBoundary!!.upperBound
            entityList.forEach { gameEntity ->
                if (gameEntity is PaintableEntity) {
                    outputList.items.add(gameEntity.getPaintableShape())
                }
            }
        }
        return outputList
    }

    //Initialization Methods

    private fun addGameBarriers() {
        //Just the outsides
        val cornersArray = arrayOf<Vector>(
            gameBoundary!!.lowerBound,
            Vector(gameBoundary!!.upperBound.x, gameBoundary!!.lowerBound.y),
            gameBoundary!!.upperBound,
            Vector(gameBoundary!!.lowerBound.x, gameBoundary!!.upperBound.y)
        )
        for (i in 0 until 4) {
            addEntity(BarrierEntity().apply {
                start = cornersArray[i]
                end = cornersArray[(i + 1) % 4]
            })
        }
    }

    private fun initCollisionGrid() {
        //maxEntitySize is that for mobile+collidable objects and should be the maximum distance
        //between any two points on an entity.  Argument source must be revised if add any more
        //mobile+collidable objects, or BallEntities no longer have common radius
        //collisionGrid = CollisionGrid(gameBoundary!!, (2f) * gameBallFactory.create().radius)
        collisionGrid =
            CollisionGrid(gameBoundary!!, (2f) * BallEntityFactory().create().radius)
    }

    private fun resetEntityLists() {
        entityList.clear()
    }

    private fun trySetupGameBoundary(boundaryDims: Vector? = null): Boolean {
        if ((gameBoundary == null) || (!(gameBoundary!!.isValidGameBoundary()))) {
            if (boundaryDims != null) {
                gameBoundary = GameBoundary(boundaryDims)
            } else {
                return false
            }
            if (gameBoundary!!.isValidGameBoundary()) {
                return true
            }
            return false
        }
        return true
    }

    fun takeGravity(gravVector: Vector) {
        gravityVector = gravVector.times(GRAVITY_STRENGTH)
    }
}

enum class EntitySimulationState {
    NONE, INITIALIZED, RUNNING, PAUSED, ENDED
}