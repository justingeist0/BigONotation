package com.fantasmaplasma.bigonotation

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        model = Model.Factory.getInstance()
        backgroundGradientAnimation()
        headerTextAnimation()
        setUpButtons()
    }

    private fun setUpButtons() {
        btn_main_time
            .setOnClickListener {
                navigateToComplexityActivity(true)
            }
        btn_main_space
            .setOnClickListener {
                navigateToComplexityActivity(false)
            }
    }

    private fun navigateToComplexityActivity(showTimeComplexity: Boolean) {
        model.showTimeComplexity = showTimeComplexity
        startActivity(
            Intent(this, ComplexityActivity::class.java)
        )
    }

    private fun headerTextAnimation() {
        with(ValueAnimator.ofFloat(1.0f,  1.2f)) {
            duration = 6000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                val value = it.animatedValue as Float
                val reverseValue = 2.2f-value
                with(tv_main_big_o) {
                    scaleX = value
                    scaleY = value
                }
                with(tv_main_notation) {
                    scaleX = reverseValue
                    scaleY = reverseValue
                }
            }
            start()
        }
    }

    private fun backgroundGradientAnimation() {
        val darkGreen = ContextCompat.getColor(this, R.color.darkGreen)
        val green = ContextCompat.getColor(this, R.color.green)
        val gradient = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(green,darkGreen,green))
        val evaluator = ArgbEvaluator()
        with(ValueAnimator.ofFloat(0.0f, 1.5f)) {
            duration = 12000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                val value = it.animatedValue as Float
                gradient.colors =
                    when {
                        value >= 1 ->
                            intArrayOf(
                                darkGreen,
                                evaluator.evaluate(value-.5f, green, darkGreen) as Int,
                                darkGreen
                            )
                        else ->
                            intArrayOf(
                                evaluator.evaluate(value, green, darkGreen) as Int,
                                evaluator.evaluate(value, green, evaluator.evaluate(.5f, green, darkGreen)) as Int,
                                evaluator.evaluate(value, green, darkGreen) as Int
                            )
                    }
            }
            start()
        }
        findViewById<View>(R.id.layout_main).background = gradient
    }
}