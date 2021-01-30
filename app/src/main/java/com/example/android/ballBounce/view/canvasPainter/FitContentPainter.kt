package com.example.android.ballBounce.view.canvasPainter

import android.graphics.Canvas
import com.example.android.ballBounce.utility.Rectangle
import com.example.android.ballBounce.view.CanvasDrawView

class FitContentPainter(contentPainter: CanvasPainter) : CanvasPainter() {
    init {
        delegatePainter = contentPainter
    }

    override fun getContentBounds(): Rectangle? {
        return delegatePainter?.getContentBounds()
    }

    override fun paintViewCanvas(canvas: Canvas, view: CanvasDrawView) {
        if (delegatePainter == null) {
            return
        }
        applyContentFitTransformation(canvas,view.drawingSpace,getContentBounds())
        delegatePainter!!.paintViewCanvas(canvas,view)
        unApplyContentFitTransformation(canvas)
    }

    private fun applyContentFitTransformation(
        canvas: Canvas,
        viewBounds: Rectangle?,
        contentBounds: Rectangle?
    ) {
        if ((viewBounds == null) || (contentBounds == null)) {
            return
        }
        if (!(viewBounds.isValid() && contentBounds.isValid())) {
            return
        }

        val widthRatio = contentBounds.width() / viewBounds.width()
        val heightRatio = contentBounds.height() / viewBounds.height()
        val heightLimited = (heightRatio / widthRatio) > 1f
        val scaleFactor = (1f) / (if (heightLimited) heightRatio else widthRatio)
        canvas.save()

        //Translate objects so origin of content is coincident with view origin
        canvas.translate(
            ((-1f) * contentBounds.lowerBounds.x - viewBounds.lowerBounds.x).toFloat(),
            ((-1f) * contentBounds.lowerBounds.y - viewBounds.lowerBounds.y).toFloat()
        )

        //Scale to fit objects inside view
        canvas.scale(scaleFactor.toFloat(), scaleFactor.toFloat())

        //Center in letter-boxed dimension
        canvas.translate(
            (if (heightLimited) (0.5) * (viewBounds.width() - scaleFactor * contentBounds.width()) else 0.0).toFloat(),
            (if (heightLimited) 0.0 else (0.5) * (viewBounds.height() - scaleFactor * contentBounds.height())).toFloat()
        )
    }

    private fun unApplyContentFitTransformation(canvas: Canvas) {
        canvas.restore()
    }
}