package hoo.etahk.common

import android.annotation.SuppressLint
import hoo.etahk.common.Constants.Time
import java.text.SimpleDateFormat
import kotlin.math.abs

object Utils {
    /**
     * Function to return current timestamp in second.
     * @return timestamp in second
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis() / Time.ONE_SECOND_IN_MILLIS
    }

    /**
     * Function to return timestamp in EAT Time String
     * @return timestamp in second
     */
    @SuppressLint("SimpleDateFormat")
    fun timeStrToTimestamp(timeStr: String): Long {
        val etaTimeFormat = SimpleDateFormat("HH:mm")
        val now = getCurrentTimestamp()
        var result = now - now % Time.ONE_DAY_IN_SECONDS

        try {
            result += (etaTimeFormat.parse(timeStr).time / Time.ONE_SECOND_IN_MILLIS)
            if ((now - result) > Time.ONE_DAY_IN_SECONDS/2) {
                result += Time.ONE_DAY_IN_SECONDS
            }
            if ((result - now) > Time.ONE_DAY_IN_SECONDS/2) {
                result -= Time.ONE_DAY_IN_SECONDS
            }
            if (abs(result - now) > Time.ONE_DAY_IN_SECONDS/2) {
                return -1L
            }
        } catch (e: Exception) {
            return -1L
        }
        return result
    }

    /**
     * Function to return valid ETA Result's message from EAT Time String
     * @return message in ETAResult
     */
    fun timeStrToMsg(timeStr: String): String {
        // TODO("Need to Support English")
        return timeStr.replace("　".toRegex(), " ")
                .replace("班次".toRegex(), "")
                .replace("時段".toRegex(), "")
                .replace("九巴預定".toRegex(), "九巴 預定")
                .replace("距離.*公里".toRegex(), "")
                .trim()
    }

    /**
     * Function to check if ETA is schedule only from EAT Time String
     * @return message in ETAResult
     */
    fun isScheduledOnly(timeStr: String): Boolean {
        // TODO("Need to Support English")
        return timeStr.contains("預定".toRegex())
    }
}