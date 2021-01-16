package com.example.android.ballBounce.view.canvasPainter

import android.graphics.Canvas
import com.example.android.ballBounce.utility.Rectangle
import com.example.android.ballBounce.view.CanvasDrawView

abstract class CanvasPainter {
    var delegatePainter: CanvasPainter? = null
    abstract fun paintViewCanvas(canvas: Canvas,view: CanvasDrawView)
    abstract fun getContentBounds(): Rectangle?
}