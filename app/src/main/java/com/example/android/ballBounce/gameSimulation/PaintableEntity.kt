package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableShape

interface PaintableEntity {
    fun getPaintableShape(): PaintableShape
}