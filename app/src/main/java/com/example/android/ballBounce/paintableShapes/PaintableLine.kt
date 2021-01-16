package com.example.android.ballBounce.paintableShapes

import android.graphics.Canvas
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.view.LinePaintFactory

class PaintableLine(val start: Vector, val end: Vector, val width: Float) : PaintableShape() {
    lateinit var linePaintFactory: LinePaintFactory

    override fun paintShape(paintCanvas: Canvas) {
        paintCanvas.drawLine(start.x,start.y,end.x,end.y,linePaintFactory.getPaint(width))
    }

}