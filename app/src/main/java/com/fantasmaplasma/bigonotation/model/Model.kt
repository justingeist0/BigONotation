package com.fantasmaplasma.bigonotation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fantasmaplasma.bigonotation.constant.BigO
import com.fantasmaplasma.bigonotation.view.Bar
import com.fantasmaplasma.bigonotation.view.BarState
import kotlinx.coroutines.*
import java.util.*

class Model {
    private var mBars = mutableListOf<Array<Bar>>()
    private var mBarsLiveData = MutableLiveData<List<Array<Bar>>>()
    val barsLiveData: LiveData<List<Array<Bar>>>
        get() = mBarsLiveData

    private var mConstantOperationsLiveData = MutableLiveData<Int>()
    val constantOperationsLiveData: LiveData<Int>
        get() = mConstantOperationsLiveData
    private var mConstantOperations = -1

    var complexity = BigO.CONSTANT_TIME
        set(value) {
            if(field != value) {
                field = value
                reset()
            }
        }

    var showTimeComplexity = true
    var dataSetSize = 0
        set (value) {
            if(value == field)
                return
            field = value
            reset()
        }

    var speedMS = 0L

    private var random = Random()
    private var barJob: Job? = null
    val isJobActive: Boolean
        get() = barJob != null

    private var desiredValue = -1
    private var start = -1
    private var end = - -1

    fun playClicked() {
        barJob =
            if(barJob != null) {
                reset()
                return
            } else
                GlobalScope.launch {
                    while(isActive && updateExample()) {
                        delay(speedMS)
                    }
                }
    }

    private fun updateExample() : Boolean {
        val isAnotherStepNeeded: Boolean =
            if(showTimeComplexity)
                when (complexity) {
                    BigO.CONSTANT_TIME -> constantTimeStep()
                    BigO.LOGARITHMIC_TIME -> logNTimeStep()
                    BigO.LINEAR_TIME -> nTimeStep()
                    BigO.EXPONENTIAL_TIME -> exponentialExample()
                    else -> false
                }
            else
                when (complexity) {
                    BigO.CONSTANT_TIME -> constantSpaceStep()
                    BigO.LOGARITHMIC_TIME -> logNSpace()
                    BigO.LINEAR_TIME -> nSpace()
                    BigO.EXPONENTIAL_TIME -> exponentialSpace()
                    else -> false
                }
        updateLiveData()
        return isAnotherStepNeeded
    }

    private fun constantSpaceStep(): Boolean {
        val randomIdx = random.nextInt(mBars[0].size)
        mBars[0].forEach { it.state = BarState.RULED_OUT }
        mBars[0][randomIdx].state = BarState.ACCESSED
        mBars[1] = arrayOf(Bar(mBars[0][randomIdx].value))
        return false
    }

    private fun logNSpace(): Boolean {
        mConstantOperations=0
        val copy = mutableListOf<Bar>()
        var idx = mBars[0].size-1
        do {
            copy.add(Bar(mBars[0][idx].value))
            mConstantOperations++
            mBars[0][idx].apply{ state = BarState.ACCESSED }
            for(i in idx until mBars[0].size)
                mBars[0][i].apply {
                    if(state !== BarState.ACCESSED)
                        state = BarState.RULED_OUT
                }
            mBars[1] = copy.toTypedArray()
            if(idx == 0) return false
            idx /= 2
            updateLiveData()
            Thread.sleep(speedMS)
            if(barJob == null) return false
        } while(true)
    }

    private fun nSpace(): Boolean {
        mConstantOperations = 0
        val copy = mutableListOf<Bar>()
        mBars[0].forEach {
            copy.add(Bar(it.value))
            mConstantOperations++
            mBars[1] = copy.toTypedArray()
            it.apply{ state = BarState.ACCESSED }
            updateLiveData()
            Thread.sleep(speedMS)
            if(barJob == null) return false
        }
        return false
    }

    private fun exponentialSpace(): Boolean {
        mConstantOperations = 0
        val copy = mutableListOf<Bar>()
        mBars[0].forEach { value1 ->
            mBars[0].forEach { value2 ->
                copy.add(Bar((value1.value+value2.value)/2))
                mBars[1] = copy.toTypedArray()
                mConstantOperations++
                if (value1.state != BarState.NO_ACTION)
                    value1.state = BarState.ACCESSED_AGAIN
                else
                    value1.state = BarState.ACCESSED
                if(value2.state == BarState.ACCESSED) {
                    value2.state = BarState.ACCESSED_AGAIN
                    updateLiveData()
                    Thread.sleep(speedMS/2)
                    value2.state = BarState.ACCESSED
                    updateLiveData()
                    Thread.sleep(speedMS/2)
                } else {
                    value2.state = BarState.ACCESSED
                    updateLiveData()
                    Thread.sleep(speedMS)
                }
                if(barJob == null) return false
            }
            value1.state = BarState.ACCESSED
        }
        return false
    }

    private fun constantTimeStep() : Boolean {
        val randomIdx = random.nextInt(mBars[0].size)
        mBars[0].forEach { it.state = BarState.RULED_OUT }
        mBars[0][randomIdx].state = BarState.ACCESSED
        mConstantOperations = 1
        return false
    }

    private fun logNTimeStep() : Boolean {
        if(desiredValue == -1) {
            desiredValue = random.nextInt(mBars[0].size)
            start = 0
            end = mBars[0].size - 1
            mConstantOperations = 0
        }
        val middle = (start + end) / 2
        val value = mBars[0][middle].value
        mBars[0][middle].state = BarState.ACCESSED
        mConstantOperations++
        when {
            desiredValue > value -> {
                for(i in (middle - 1) downTo start)
                    ruleOut(i)
                start = middle
            }
            desiredValue < value -> {
                for(i in (middle + 1)..end)
                    ruleOut(i)
                end = middle
            }
            else -> {
                for(i in start..end)
                    ruleOut(i)
                return false
            }
        }
        return true
    }

    private fun nTimeStep() : Boolean {
        if(desiredValue == -1) {
            desiredValue = random.nextInt(mBars[0].size)
            start = 0
            mConstantOperations = 0
        }
        val currentBar = mBars[0][start]
        currentBar.state = BarState.ACCESSED
        mConstantOperations++
        start++
        val isValueFound = currentBar.value == desiredValue
        if(isValueFound) {
            currentBar.value = 0
            for(i in start until mBars[0].size)
                ruleOut(i)
        }
        return !isValueFound
    }

    private fun exponentialExample() : Boolean {
        var isRunning = true
        var iterations = 0
        mConstantOperations = 0
        while(isRunning) {
            isRunning = false
            for (i in 1 until mBars[0].size - iterations) {
                val beforeBar = mBars[0][i - 1]
                val currentBar = mBars[0][i]
                mConstantOperations += 1
                if (beforeBar.value > currentBar.value) {
                    beforeBar.value += currentBar.value
                    currentBar.value = beforeBar.value - currentBar.value
                    beforeBar.value -= currentBar.value
                    isRunning = true
                }
                if(currentBar.state == BarState.ACCESSED) {
                    beforeBar.state = BarState.ACCESSED_AGAIN
                    currentBar.state = BarState.ACCESSED_AGAIN
                    updateLiveData()
                    Thread.sleep(speedMS/2)
                    beforeBar.state = BarState.ACCESSED
                    currentBar.state = BarState.ACCESSED
                    updateLiveData()
                    Thread.sleep(speedMS/2)
                } else {
                    beforeBar.state = BarState.ACCESSED
                    currentBar.state = BarState.ACCESSED
                    updateLiveData()
                    Thread.sleep(speedMS)
                }
                if (barJob == null) return false
            }
            iterations++
        }
        return false
    }

    private fun updateLiveData() {
        mBarsLiveData.postValue(mBars)
        mConstantOperationsLiveData.postValue(mConstantOperations)
    }

    private fun ruleOut(idx: Int, listIdx: Int = 0) {
        if(mBars[listIdx][idx].state != BarState.ACCESSED)
            mBars[listIdx][idx].state = BarState.RULED_OUT
    }

    fun reset() {
        barJob = resetJob()
        initBars()
    }

    private fun initBars() {
        mBars.clear()
        mBars.add(Array(dataSetSize) {
                Bar(it+1)
            }.apply {
                val shouldShuffle =
                    !showTimeComplexity || complexity != BigO.LOGARITHMIC_TIME
                if(shouldShuffle) shuffle()
            }
        )
        if(!showTimeComplexity)
            mBars.add(Array(0) { Bar(0) })
        updateLiveData()
    }

    private fun resetJob() : Job? {
        barJob?.cancel()
        desiredValue = -1
        start = -1
        end = -1
        mConstantOperations = -1
        mConstantOperationsLiveData.postValue(mConstantOperations)
        return null
    }

    object Factory {
        private lateinit var controller: Model

        fun getInstance(dataSetSize: Int = 0, speedMS: Long = 0L) : Model {
            if(!this::controller.isInitialized)
                controller = Model()
            with(controller) {
                this.dataSetSize = dataSetSize
                this.speedMS = speedMS
                return this
            }
        }
    }

}
