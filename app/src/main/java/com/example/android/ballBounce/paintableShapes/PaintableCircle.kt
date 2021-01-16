package com.example.android.ballBounce.paintableShapes

import android.graphics.Canvas
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.view.CirclePaintFactory

class PaintableCircle(val center: Vector, val radius: Float,val colorIndex: Int = 0) : PaintableShape() {

    lateinit var circlePaintFactory: CirclePaintFactory

    override fun paintShape(paintCanvas: Canvas) {
        paintCanvas.drawCircle(
            center.x,
            center.y,
            radius,
            circlePaintFactory.getFillPaint(colorIndex)
        )

        paintCanvas.drawCircle(
            center.x,
            center.y,
            radius-circlePaintFactory.getOutlinePaint().strokeWidth/2,
            circlePaintFactory.getOutlinePaint()
        )
    }
}