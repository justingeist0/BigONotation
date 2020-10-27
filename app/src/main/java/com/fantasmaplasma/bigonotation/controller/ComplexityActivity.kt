package com.fantasmaplasma.bigonotation.controller

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fantasmaplasma.bigonotation.view.ComplexitySpinnerAdapter
import com.fantasmaplasma.bigonotation.model.Model
import com.fantasmaplasma.bigonotation.R
import kotlinx.android.synthetic.main.activity_complexity.*
import kotlinx.android.synthetic.main.dialog_complexity.*
import kotlinx.android.synthetic.main.dialog_complexity_settings.*
import kotlin.math.log

class ComplexityActivity : AppCompatActivity() {
    private lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complexity)
        model = Model.Factory.getInstance(
            getPreferredSize(),
            getPreferredSpeed()
        )
        setUpActionBar()
        setUpSpinner()
        setUpButtons()
        setUpObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.header_complexity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.settings -> {
                showSettings()
                true
            }
            R.id.basics -> {
                showInfoDialog(
                    getString(R.string.big_o_notation),
                    getString(R.string.big_o_description)
                )
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else ->
                super.onContextItemSelected(item)
        }
    }

    private fun setUpActionBar() {
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = getString(
                if (model.showTimeComplexity)
                    R.string.time_complexity
                else
                    R.string.space_complexity
            )
        }
    }

    private fun setUpSpinner() {
        ComplexitySpinnerAdapter(this,
            resources.getStringArray(R.array.big_o_measurement))
            .also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_complexity_measurements.adapter = it
            }
        spinner_complexity_measurements.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tv_complexity_algorithm.text =
                        resources.getStringArray(
                            if (model.showTimeComplexity)
                                R.array.time_complexity_example
                            else
                                R.array.space_complexity_example
                        )[position]
                    model.complexity = position
                    setPlayImage()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setUpButtons() {
        btn_complexity_info
            .setOnClickListener {
                showInfoDialog(
                    "${resources
                        .getStringArray(R.array.big_o_measurement)
                            [model.complexity]}: ${resources
                        .getStringArray(R.array.big_o_measurement_name)
                            [model.complexity]}",
                    resources
                    .getStringArray(R.array.big_o_measurement_description)
                        [model.complexity].replace("%s",
                        if(model.showTimeComplexity)
                            getString(R.string.time)
                        else
                            getString(R.string.space)
                    )
                )
            }
        btn_complexity_play
            .setOnClickListener {
                model.playClicked()
                setPlayImage()
            }
    }

    private fun setPlayImage() {
        btn_complexity_play.setImageDrawable(
            ContextCompat.getDrawable(this,
                if(model.isJobActive)
                    R.drawable.ic_complexity_reset
                else
                    R.drawable.ic_complexity_play
            )
        )
    }

    private fun setUpObservers() {
        model.barsLiveData.observe(this, {
            algorithm_view_complexity.setBar(it)
        })
        model.constantOperationsLiveData.observe(this, { constantOperations ->
            tv_complexity_stats.text =
                if(constantOperations <= 0)
                    getString(R.string.size, model.dataSetSize.toString())
                else {
                    val operationsStr = constantOperations.toString()
                    if (model.showTimeComplexity)
                        getString(R.string.time_operations, operationsStr)
                    else
                        getString(R.string.space_operations, operationsStr)
                }
        })
    }

    private fun getLogarithmicTime() =
        with(log(model.dataSetSize.toDouble(), 2.0)) {
            if(this > this.toInt()) (this.toInt()+1) else this.toInt()
        }

    private fun showInfoDialog(header:String, body: String) {
        Dialog(this).apply {
            setContentView(R.layout.dialog_complexity)
            tv_dialog_complexity_header.text = header
            tv_dialog_complexity_body.text = body
            show()
        }
    }

    private fun showSettings() {
        Dialog(this).apply {
            setContentView(R.layout.dialog_complexity_settings)
            var currentSizeValue = model.dataSetSize
            tv_settings_size.text = getSizeText(currentSizeValue)
            with(slider_settings_size) {
                value = currentSizeValue.toFloat()
                addOnChangeListener { _, value, _ ->
                    currentSizeValue = value.toInt()
                    this@apply.tv_settings_size.text = getSizeText(currentSizeValue)
                }
            }
            var currentSpeedValue = model.speedMS
            tv_settings_speed.text = getSpeedText(currentSpeedValue)
            with(slider_settings_speed) {
                value = currentSpeedValue.toFloat()
                addOnChangeListener { _, value, _ ->
                    currentSpeedValue = value.toLong()
                    this@apply.tv_settings_speed.text = getSpeedText(currentSpeedValue)
                }
            }
            setOnCancelListener {
                model.dataSetSize = currentSizeValue
                model.speedMS = currentSpeedValue
                setPlayImage()
            }
            show()
        }
    }

    private fun getSizeText(currentSizeValue: Int) =
        this@ComplexityActivity.getString(R.string.size, currentSizeValue.toString())

    private fun getSpeedText(currentSpeedValue: Long) =
        this@ComplexityActivity.getString(R.string.speed_ms, currentSpeedValue.toString())


    override fun onResume() {
        super.onResume()
        model.reset()
    }

    override fun onPause() {
        updatePreferences(
            model.dataSetSize,
            model.speedMS
        )
        super.onPause()
    }

    private fun updatePreferences(size: Int, speed: Long) {
        getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE)
            .edit().apply {
                putInt(KEY_SIZE, size)
                putLong(KEY_SPEED_MS, speed)
                apply()
            }
    }

    private fun getPreferredSize() =
        getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE)
            .getInt(KEY_SIZE, DEFAULT_SIZE)


    private fun getPreferredSpeed() =
        getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE)
            .getLong(KEY_SPEED_MS, DEFAULT_SPEED)

    companion object {
        const val KEY_PREFERENCES = "KEY_PREFERENCES"
        const val KEY_SIZE = "KEY_SIZE"
        const val KEY_SPEED_MS = "KEY_SPEED_MS"
        const val DEFAULT_SIZE = 100
        const val DEFAULT_SPEED = 100L
    }
}