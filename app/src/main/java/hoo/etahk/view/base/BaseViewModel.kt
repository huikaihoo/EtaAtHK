package hoo.etahk.view.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Handler
import android.os.Looper
import android.util.Log
import hoo.etahk.common.Constants.Time
import hoo.etahk.common.Utils
import java.util.*

open class BaseViewModel : ViewModel() {

    companion object {
        private val TAG = "BaseViewModel"
    }

    private val lastUpdateTime = MutableLiveData<Long>()
    private var timer :Timer? = null

    var period = 0L
        set(value) {
            field = value
            startTimer()
        }

    fun getLastUpdateTime(): LiveData<Long> {
        return lastUpdateTime
    }

    private fun startTimer() {
        if (period > 0) {
            val periodInMillis = period * Time.ONE_SECOND_IN_MILLIS

            if (timer == null) {
                timer = Timer()
                // Update the elapsed time every n second (n = period)
                timer!!.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post({
                            lastUpdateTime.value = Utils.getCurrentTimestamp()
                            Log.d(TAG, lastUpdateTime.value.toString())})
                    }
                }, Time.ONE_SECOND_IN_MILLIS, periodInMillis)
            }
        } else {
            timer?.cancel()
            timer = null
        }
    }
}
