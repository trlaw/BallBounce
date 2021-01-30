package com.example.android.ballBounce.paintableShapes

import android.graphics.Canvas
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.view.LinePaintFactory

class PaintableLine(val start: Vector, val end: Vector, val width: Double) : PaintableShape() {
    lateinit var linePaintFactory: LinePaintFactory

    override fun paintShape(paintCanvas: Canvas) {
        paintCanvas.drawLine(start.x.toFloat(),start.y.toFloat(),end.x.toFloat(),end.y.toFloat(),linePaintFactory.getPaint(width))
    }

}