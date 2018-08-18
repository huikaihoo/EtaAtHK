package hoo.etahk.view.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.CountDownTimer
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils

open class TimerViewModel : ViewModel() {

    private val millisLeft = MutableLiveData<Long>()
    private val lastUpdateTime = MutableLiveData<Long>()
    private var timer : CountDownTimer? = null

    var durationInMillis = 0L
        set(value) {
            if (field != value)
                lastUpdateTime.value = 0L
            field = value
        }

    fun getLastUpdateTime(): LiveData<Long> {
        return lastUpdateTime
    }

    fun getMillisLeft(): LiveData<Long> {
        return millisLeft
    }

    fun startTimer() {
        timer?.cancel()
        millisLeft.value = 0L

        timer = object : CountDownTimer(durationInMillis, Constants.Time.PROGRESS_BAR_UPDATE_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                millisLeft.value = millisUntilFinished
            }

            override fun onFinish() {
                millisLeft.value = 0L
                lastUpdateTime.value = Utils.getCurrentTimestamp() - 1
            }
        }

        timer?.start()
    }

    fun stopTimer() {
        if (timer != null) {
            timer?.cancel()
            millisLeft.value = 0L
            lastUpdateTime.value = Utils.getCurrentTimestamp() - 1
        }
    }

}
