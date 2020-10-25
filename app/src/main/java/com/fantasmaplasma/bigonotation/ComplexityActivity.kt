package com.fantasmaplasma.bigonotation

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_complexity.*
import kotlinx.android.synthetic.main.dialog_complexity.*

class ComplexityActivity : AppCompatActivity() {
    private lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complexity)
        model = Model.Factory.getInstance(
            getPreferredSize(),
            getPreferredSpeed()
        )
        model.barsLiveData.observe(this, {
            algorithm_view_complexity.setBar(it)
        })
        setUpActionBar()
        setUpSpinner()
        setUpButtons()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.header_complexity, menu)
        return true
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
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setUpButtons() {
        btn_complexity_info
            .setOnClickListener {
                showInfo(resources
                    .getStringArray(R.array.big_o_measurement)
                        [model.complexity],
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
            }
    }

    private fun showInfo(header:String, body: String) {
        Dialog(this).apply {
            setContentView(R.layout.dialog_complexity)
            tv_dialog_complexity_header.text = header
            tv_dialog_complexity_body.text = body
            show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.settings -> {
                true
            }
            R.id.basics -> {
                showInfo(
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

    override fun onPause() {
        updatePreferences(
            model.dataSetSize,
            model.speedMS
        )
        super.onPause()
    }

    private fun updatePreferences(size: Int, speed: Long) {
        getSharedPreferences(KEY_PREFERENCES, MODE_PRIVATE).edit().apply {
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