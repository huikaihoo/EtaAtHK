package hoo.etahk.model.json

import android.text.SpannableStringBuilder
import android.widget.TextView
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Time.ONE_MINUTE_IN_SECONDS
import hoo.etahk.common.Utils
import hoo.etahk.common.Utils.getCurrentTimestamp
import hoo.etahk.common.extensions.prependImage
import hoo.etahk.common.helper.AppHelper

data class EtaResult(
    var company: String = "",
    var etaTime: Long = 0L,
    var msg: String = "",
    var scheduleOnly: Boolean = false,
    var gps: Boolean = false,
    var variant: Long = 0L,
    var wifi: Boolean = false,      // changed to store wheelchair
    var capacity: Long = -1L,
    var distance: Long = 0L
) {
    var valid = etaTime > 0L
        get() = etaTime > 0L
        private set

    fun getDiffInMinutes(): Long {
        val sec = etaTime - getCurrentTimestamp()
        return sec / ONE_MINUTE_IN_SECONDS
    }

    fun getPlainTextMsg(): String {
        return msg + when (valid) {
            true -> {
                val min = getDiffInMinutes()
                when {
                    min > 0L -> " " + min + AppHelper.getString(R.string.min)
                    min == 0L -> " " + AppHelper.getString(R.string.eta_msg_arriving)
                    else -> " " + AppHelper.getString(R.string.eta_msg_departing)
                }
            }
            false -> ""
        }
    }

    fun getFullTextMsg(tv: TextView, isLoading: Boolean, etaStatus: Constants.EtaStatus): SpannableStringBuilder {
        var text = SpannableStringBuilder()

        if (isLoading) {
            text = tv.prependImage(R.drawable.ic_text_loading, text)
        } else if (etaStatus != Constants.EtaStatus.SUCCESS) {
            text = tv.prependImage(R.drawable.ic_text_failed, text)
        }
        if (valid) {
            if (scheduleOnly) {
                text = tv.prependImage(R.drawable.ic_text_schedule_only, text)
            } else if (!gps) {
                text = tv.prependImage(R.drawable.ic_text_gps_off, text)
            }
        }
        if (wifi) {
            text = tv.prependImage(R.drawable.ic_text_wheelchair, text)
        }
        if (valid && capacity >= 0L) {
            text = tv.prependImage(Utils.getCapacityResId(capacity), text)
        }

        text.append(getPlainTextMsg())
        return text
    }
}