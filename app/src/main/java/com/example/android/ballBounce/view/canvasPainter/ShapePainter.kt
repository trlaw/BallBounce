package com.example.android.ballBounce.view.canvasPainter

import android.graphics.Canvas
import android.graphics.Color
import com.example.android.ballBounce.paintableShapes.*
import com.example.android.ballBounce.utility.Rectangle
import com.example.android.ballBounce.view.CanvasDrawView
import com.example.android.ballBounce.view.CirclePaintFactory
import com.example.android.ballBounce.view.LinePaintFactory
import com.example.android.ballBounce.view.TextPaintFactory

const val BACKGROUND_COLOR = Color.WHITE

class ShapePainter() : CanvasPainter() {

    var linePaintFactory: LinePaintFactory? = null
    var circlePaintFactory: CirclePaintFactory? = null
    var textPaintFactory: TextPaintFactory? = null

    public var paintableShapeList: PaintableShapeList? = null

    fun assignPaintFactories() {
        if (paintableShapeList != null) {
            for (paintableShape: PaintableShape in paintableShapeList!!.items) {
                when (paintableShape) {
                    is PaintableCircle -> assignCirclePaintFactory(paintableShape)
                    is PaintableLine -> assignLinePaintFactory(paintableShape)
                    is PaintableText -> assignTextPaintFactory(paintableShape)
                }
            }
        }
    }

    override fun getContentBounds(): Rectangle? {
        if (paintableShapeList != null) {
            return Rectangle(paintableShapeList!!.shapesUpperLeft, paintableShapeList!!.shapesLowerRight)
        }
        return null
    }

    private fun assignTextPaintFactory(paintableText: PaintableText) {
        if (textPaintFactory != null) {
            paintableText.textPaintFactory = this.textPaintFactory!!
        }
    }

    private fun assignLinePaintFactory(paintableLine: PaintableLine) {
        if (linePaintFactory != null) {
            paintableLine.linePaintFactory = this.linePaintFactory!!
        }
    }

    private fun assignCirclePaintFactory(paintableCircle: PaintableCircle) {
        if (circlePaintFactory != null) {
            paintableCircle.circlePaintFactory = this.circlePaintFactory!!
        }
    }

    override fun paintViewCanvas(canvas: Canvas, view: CanvasDrawView) {
        canvas.drawColor(BACKGROUND_COLOR)
        if (paintableShapeList != null) {
            for (paintableShape: PaintableShape in paintableShapeList!!.items) {
                paintableShape.paintShape(canvas)
            }
        }
    }

}