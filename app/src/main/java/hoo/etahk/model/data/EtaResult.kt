package hoo.etahk.model.data

import hoo.etahk.common.Constants.Time.ONE_MINUTE_IN_SECONDS
import hoo.etahk.common.Utils.getCurrentTimestamp

data class EtaResult (
        var company: String = "",
        var etaTime: Long = 0L,
        var msg: String = "",
        var scheduledOnly: Boolean = false,
        var variant: Long = 0L,
        var wifi: Boolean? = false,
        var distance: Long? = 0L) {

    fun isValid() : Boolean {
        return etaTime > 0
    }

    fun getDiffInMinutes(): Long {
        val sec = etaTime - getCurrentTimestamp()
        return sec / ONE_MINUTE_IN_SECONDS + if ((sec % 60) > 0) 1 else 0
    }

    fun getDisplayMsg() : String {
        return msg + when (isValid()) {
            true -> {
                val min = getDiffInMinutes()
                if (min > 0)
                    " (" + getDiffInMinutes() + "分鐘)"
                else
                    ""
            }
            false -> ""
        }
    }
}