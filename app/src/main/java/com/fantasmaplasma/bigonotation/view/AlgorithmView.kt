package com.fantasmaplasma.bigonotation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.fantasmaplasma.bigonotation.R
import kotlin.math.max
import kotlin.math.min

class AlgorithmView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private var bar : List<Array<Bar>>? = null
    private val defaultMargin = context.resources.getDimension(R.dimen.margin_complexity_bar)
    private var barMargin = 0f

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

    private var revisitedPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.green)
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
        var currentX = barMargin
        barWidth = getBarWidth(bar?.get(0)?.size ?: return)
        bar?.get(0)?.forEach {
            drawBar(canvas, currentX, it)
            currentX += barWidth+barMargin
        }
        if(bar?.size != 2) return
        barWidth =
            if(bar!![0].size < bar!![1].size)
                getBarWidth(bar!![1].size)
            else
                barWidth
        currentX = barMargin
        bar?.get(1)?.forEach {
            drawBar(canvas, currentX, it, min(width, height)/2f + barMargin)
            currentX += barWidth+barMargin
        }
    }

    private fun drawBar(canvas: Canvas, position: Float, bar: Bar,
                        addToBeginning: Float = 0f, flipY: Boolean = false) {
        val paint =
            when (bar.state) {
                BarState.ACCESSED_AGAIN -> revisitedPaint
                BarState.RULED_OUT -> eliminatedPaint
                BarState.ACCESSED -> activePaint
                else -> noActionPaint
            }
        val drawBarsStartToEnd = height > width
        if(drawBarsStartToEnd)
            canvas.drawRect (
                barMargin + addToBeginning,
                position,
                scaleValueToScreen(bar.value) + addToBeginning,
                position+barWidth,
                paint
            )
        else
            canvas.drawRect (
                position,
                barMargin + addToBeginning,
                position+barWidth,
                scaleValueToScreen(bar.value) + addToBeginning,
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
            value/bar!![0].size.toFloat() * (min(width, height) - barMargin*2f) /
                    (if(bar?.size == 2) 2f else 1f) - (if(bar?.size == 2) barMargin else 0f)


    fun setBar(bar: List<Array<Bar>>) {
        this.bar = bar
        invalidate()
    }

    private fun getBarWidth(size: Int): Float {
        barMargin = defaultMargin
        var barWidth = (max(width, height) - barMargin*2) / size - barMargin
        if(barWidth < 1) {
            barWidth = max(width, height) / size
            barMargin = 0f
        }
        return barWidth
    }
}