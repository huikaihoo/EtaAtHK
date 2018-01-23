package hoo.etahk.model.json

import hoo.etahk.R
import hoo.etahk.common.Constants.Time.ONE_MINUTE_IN_SECONDS
import hoo.etahk.common.Utils.getCurrentTimestamp
import hoo.etahk.view.App

data class EtaResult (
        var company: String = "",
        var etaTime: Long = 0L,
        var msg: String = "",
        var scheduleOnly: Boolean = false,
        var gps: Boolean = false,
        var variant: Long = 0L,
        var wifi: Boolean = false,
        var capacity: Long = -1L,
        var distance: Long = 0L) {

    var valid = etaTime > 0L
        get() = etaTime > 0L
        private set

    fun getDiffInMinutes(): Long {
        val sec = etaTime - getCurrentTimestamp()
        return sec / ONE_MINUTE_IN_SECONDS + if ((sec % 60) > 0) 1 else 0
    }

    fun getDisplayMsg() : String {
        return msg + when (valid) {
            true -> {
                val min = getDiffInMinutes()
                if (min > 0)
                    " " + getDiffInMinutes() + App.instance.getString(R.string.min)
                else
                    ""
            }
            false -> ""
        }
    }
}