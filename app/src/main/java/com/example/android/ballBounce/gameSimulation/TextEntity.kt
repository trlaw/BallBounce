package com.example.android.ballBounce.gameSimulation

import com.example.android.ballBounce.paintableShapes.PaintableShape
import com.example.android.ballBounce.paintableShapes.PaintableText
import com.example.android.ballBounce.utility.Vector

open class TextEntity(private val position: Vector) : GameEntity(),PaintableEntity {
    var text = ""
    override fun getPaintableShape(): PaintableShape {
        return PaintableText(text, position)
    }
}