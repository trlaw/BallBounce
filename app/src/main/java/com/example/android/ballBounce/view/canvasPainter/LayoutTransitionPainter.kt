package com.example.android.ballBounce.view.canvasPainter

import android.graphics.Canvas
import android.util.Log
import com.example.android.ballBounce.utility.Rectangle
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.view.CanvasDrawView

const val TRANSITION_NS: Long = 100000000L

class LayoutTransitionPainter(contentPainter: CanvasPainter) : CanvasPainter() {

    var transitionStartTime: Long = 0
    var lastRectangleBounds = Rectangle(Vector.zero(), Vector.zero())
    var transitioning = false

    init {
        delegatePainter = contentPainter
    }

    override fun getContentBounds(): Rectangle? {
        return delegatePainter?.getContentBounds()
    }

    override fun paintViewCanvas(canvas: Canvas, view: CanvasDrawView) {
        val viewOrigin = IntArray(2)
        view.getLocationOnScreen(viewOrigin)
        val viewOriginVector = Vector(viewOrigin[0].toFloat(), viewOrigin[1].toFloat())
        val viewRectangle = Rectangle(
            viewOriginVector,
            viewOriginVector.plus(Vector(view.width.toFloat(), view.height.toFloat()))
        )
        updateLastRectangleBounds(viewRectangle)
        if (delegatePainter == null) {
            return
        }
        if (transitioning) {
            applyTransitionTransformation(canvas, viewRectangle)
            delegatePainter!!.paintViewCanvas(canvas, view)
            unApplyTransitionTransformation(canvas)
        } else {
            delegatePainter!!.paintViewCanvas(canvas, view)
        }
    }

    private fun applyTransitionTransformation(canvas: Canvas, viewRectangle: Rectangle) {
        val animationExtent =
            ((System.nanoTime() - transitionStartTime).toDouble()) / (TRANSITION_NS.toDouble())
        Log.d("Animation Extent","$animationExtent")
        canvas.save()
        translateOrigin(canvas, viewRectangle, animationExtent)
        scaleCanvas(canvas, viewRectangle, animationExtent)
    }

    private fun scaleCanvas(canvas: Canvas, viewRectangle: Rectangle, animationExtent: Double) {
        val scaleVector = Vector(
            lastRectangleBounds.width() / viewRectangle.width(),
            lastRectangleBounds.height() / viewRectangle.height()
        )
        canvas.scale(
            ((1.0 - animationExtent).toFloat()) * scaleVector.x + animationExtent.toFloat(),
            ((1.0 - animationExtent).toFloat()) * scaleVector.y + animationExtent.toFloat(), 0f, 0f
        )
    }

    private fun translateOrigin(canvas: Canvas, drawingSpace: Rectangle, animationExtent: Double) {
        val originDelta = lastRectangleBounds.lowerBounds.minus(drawingSpace.lowerBounds)
        canvas.translate(
            ((1.0 - animationExtent).toFloat()) * originDelta.x,
            ((1.0 - animationExtent).toFloat()) * originDelta.y
        )
    }

    private fun unApplyTransitionTransformation(canvas: Canvas) {
        canvas.restore()
    }

    private fun updateLastRectangleBounds(drawingSpace: Rectangle) {
        if (transitioning) {
            if ((System.nanoTime() - transitionStartTime) > TRANSITION_NS) {
                lastRectangleBounds = drawingSpace
                transitioning = false //Terminates animation
            }
            return
        }
        if (lastRectangleBounds.equalTo(Rectangle.zero())) {
            if (drawingSpace.isValid()) {
                lastRectangleBounds = drawingSpace
            }
            return //Don't start transition unless already a valid layout
        }
        if (!(lastRectangleBounds.equalTo(drawingSpace))) {
            transitionStartTime = System.nanoTime()
            transitioning = true
        }
    }
}