package com.example.android.ballBounce.paintableShapes

import android.graphics.Canvas
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.view.TextPaintFactory

class PaintableText(val text:String,val position: Vector): PaintableShape() {
    lateinit var textPaintFactory: TextPaintFactory

    override fun paintShape(paintCanvas: Canvas) {
        paintCanvas.drawText(text,position.x,position.y,textPaintFactory.getPaint())
    }
}