package com.example.android.ballBounce.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.android.ballBounce.utility.Rectangle
import com.example.android.ballBounce.utility.Vector
import com.example.android.ballBounce.view.canvasPainter.CanvasPainter


class CanvasDrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var framePainter: CanvasPainter? = null
    var touchEventHandler: ((MotionEvent) -> Unit)? = null
    var sizeChangeCallback: ((Int, Int) -> Unit)? = null
    var drawingSpace = Rectangle(Vector.zero(), Vector.zero())

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingSpace = Rectangle(Vector.zero(),Vector(w.toDouble(),h.toDouble()))
        sizeChangeCallback?.invoke(w, h)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        super.onTouchEvent(e)
        touchEventHandler?.invoke(e)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (framePainter != null) {
            framePainter!!.paintViewCanvas(canvas,this)
        }
    }
}

