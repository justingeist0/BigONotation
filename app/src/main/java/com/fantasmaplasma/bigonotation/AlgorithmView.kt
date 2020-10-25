package com.fantasmaplasma.bigonotation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.max
import kotlin.math.min

class AlgorithmView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private var bar : Array<BarModel>? = null
    private var barMargin = context.resources.getDimension(R.dimen.margin_complexity_bar)

    private var width = 0f
    private var height = 0f
    private var barWidth = 0f

    private var noActionPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
    }

    private var activePaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.GREEN
    }

    private var eliminatedPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.RED
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        Log.d("Mama", "draw")
        var currentX = barMargin
        bar?.forEach {
            drawBar(canvas, currentX, it)
            currentX += barWidth+barMargin
        }
    }

    private fun drawBar(canvas: Canvas, position: Float, bar: BarModel) {
        val paint =
            when (bar.state) {
                BarState.RULED_OUT -> eliminatedPaint
                BarState.ACCESSED -> activePaint
                else -> noActionPaint
            }
        val drawBarsStartToEnd = height > width
        if(drawBarsStartToEnd)
            canvas.drawRect (
                barMargin,
                position,
                scaleValueToScreen(bar.value),
                position+barWidth,
                paint
            )
        else
            canvas.drawRect (
                position,
                barMargin,
                position+barWidth,
                scaleValueToScreen(bar.value),
                paint
            )
    }

    /**
     * Call to get height of bar to fill up screen scaled by the value.
     * Called for each item in bar when items exist.
     *
     * @param value Value of bar
     */
    private fun scaleValueToScreen(value: Int) =
        value/bar!!.size.toFloat() * (min(width, height) - barMargin*2)

    fun setBar(bar: Array<BarModel>) {
        this.bar = bar
        barWidth = (max(width, height) - barMargin*2) / bar.size.toFloat() - barMargin
        invalidate()
    }

}