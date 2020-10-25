package com.fantasmaplasma.bigonotation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.util.*

class Model {
    private lateinit var mBars: Array<BarModel>
    private var mBarsLiveData = MutableLiveData<Array<BarModel>>()
    val barsLiveData: LiveData<Array<BarModel>>
        get() = mBarsLiveData

    private var mTotalLiveData = MutableLiveData<Int>()
    val totalLiveData: LiveData<Int>
        get() = mTotalLiveData

    private var mAccessedLiveData = MutableLiveData<Boolean>()
    val accessedLiveData: LiveData<Boolean>
        get() = mAccessedLiveData

    var complexity = 0
        set(value) {
            if(field != value) {
                field = value
                initBars()
            }
        }

    var showTimeComplexity = true
    var dataSetSize = 0
        set (value) {
            field = value
            initBars()
        }

    var speedMS = 0L

    private var random = Random()
    private var barJob: Job? = null

    fun playClicked() {
        barJob =
            if(barJob != null) {
                initBars()
                barJob?.cancel()
                null
            } else
                GlobalScope.launch(Dispatchers.Default) {
                    while(isActive && updateExample()) {
                        delay(1000)
                    }
                }
    }

    private fun updateExample() : Boolean {
        val isAnotherStepNeeded: Boolean =
            if(showTimeComplexity)
                when (complexity) {
                    O_1 -> constantTimeStep()
                    O_LOGN -> logNTimeStep()
                    O_N -> nTimeStep()
                    O_LOGN_N -> false
                    O_N_SQUARED -> false
                    else -> false
                }
            else
                false
        mBarsLiveData.postValue(mBars)
        return isAnotherStepNeeded
    }

    private fun constantTimeStep() : Boolean {
        val randomIdx = random.nextInt(mBars.size)
        mBars.forEach { it.state = BarState.RULED_OUT }
        mBars[randomIdx].state = BarState.ACCESSED
        Log.d("Mama", "$randomIdx")
        return false
    }

    var desiredValue = -1
    var start = -1
    var end = - -1
    private fun logNTimeStep() : Boolean {
        if(desiredValue == -1) {
            desiredValue = random.nextInt(mBars.size)
            start = 0
            end = mBars.size - 1
        }
        val middle = (start + end) / 2
        val value = mBars[middle].value
        mBars[middle].state = BarState.ACCESSED
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

    fun nTimeStep() : Boolean {
        if(desiredValue == -1) {
            desiredValue = random.nextInt(mBars.size)
            start = 0
        }
        val currentBar = mBars[start]
        currentBar.state = BarState.ACCESSED
        start++
        val isValueFound = currentBar.value == desiredValue
        if(isValueFound) {
            currentBar.value = 0
            for(i in start until mBars.size)
                ruleOut(i)
        }
        return !isValueFound
    }

    private fun reset() {
        desiredValue = -1
        start = -1
        end = -1
    }

    private fun ruleOut(idx: Int) {
        if(mBars[idx].state != BarState.ACCESSED)
            mBars[idx].state = BarState.RULED_OUT
    }

    private fun initBars() {
        reset()
        mBars =
            Array(dataSetSize) {
                BarModel(it+1)
            }.apply { if(!showTimeComplexity || complexity != O_LOGN) shuffle() }
        mBarsLiveData.postValue(mBars)
    }

    companion object {
        const val O_1 = 0
        const val O_LOGN = 1
        const val O_N = 2
        const val O_LOGN_N = 3
        const val O_N_SQUARED = 4
    }

    object Factory {
        private lateinit var controller: Model

        fun getInstance(dataSetSize: Int = 0, speedMS: Long = 0L) : Model {
            if(!this::controller.isInitialized)
                controller = Model()
            return controller.apply {
                this.dataSetSize = dataSetSize
                this.speedMS = speedMS
            }
        }
    }

}
