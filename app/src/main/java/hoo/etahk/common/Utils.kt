package hoo.etahk.common

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.os.Build
import android.util.Log
import android.util.TypedValue
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.R
import hoo.etahk.common.Constants.Time
import hoo.etahk.common.constants.SharePrefs
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.view.App
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs


object Utils {
    val isUnitTest = (Build.DEVICE ?: "") == "robolectric" && (Build.PRODUCT ?: "") == "robolectric"

    /**
     * Function to return current timestamp in second.
     * @return timestamp in second
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis() / Time.ONE_SECOND_IN_MILLIS
    }

    fun getValidUpdateTimestamp(): Long {
        return getCurrentTimestamp() - SharePrefs.DEFAULT_DATA_VALIDITY_PERIOD * Time.ONE_DAY_IN_SECONDS
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTimeString(t: Long?, pattern: String = Constants.Time.PATTERN_DISPLAY): String {
        if (t != null && t > 0) {
            try {
                val sdf = SimpleDateFormat(pattern)
                val strDate = Date(t * Time.ONE_SECOND_IN_MILLIS)
                return sdf.format(strDate)
            } catch (e: Exception) {
                loge("getDateTimeString failed!", e)
            }
        }
        return ""
    }

    fun getThemeColorPrimary(context: Context): Int {
        return getThemeColor(context, R.attr.colorPrimary)
    }

    fun getThemeColorPrimaryDark(context: Context): Int {
        return getThemeColor(context, R.attr.colorPrimaryDark)
    }

    fun getThemeColorAccent(context: Context): Int {
        return getThemeColor(context, R.attr.colorAccent)
    }

    private fun getThemeColor(context: Context, resId: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(resId, value, true)
        return value.data
    }

    fun getStringResourceByName(name: String): String {
        val resId = App.instance.applicationContext.resources.getIdentifier(name, "string", App.instance.packageName)
        return when (resId) {
            0 -> ""
            else -> AppHelper.getString(resId)
        }
    }

    /**
     * Method to convert HK1980GRID coordinates to WGS84GEO coordinates
     * Source: https://github.com/helixcn/HK80/blob/master/R/HK1980GRID_TO_WGS84GEO.R
     * Verification: https://data.gov.hk/tc/datagovhk-api/coordinate-conversion
     * @param n HK1980 Grid Northing
     * @param e HK1980 Grid Easting
     * @return WGS84 Geometric coordinates
     */
    fun hk1980GridToLatLng(n: Double, e: Double): LatLng {
        //////// HK1980GRID_TO_HK80GEO ////////
        // 1. HK80
        val N0 = 819069.80
        val E0 = 836694.05
        //double phi0 = 22.0 + (18.0/60.0) + 43.68/(3600.0);
        val lambda0 = 114.0 + 10.0 / 60.0 + 42.80 / 3600.0
        val m0 = 1.0
        val M0 = 2468395.723
        //double niu_s = 6381480.500;
        //double rou_s = 6359840.760;
        //double psi_s = 1.003402560;
        val a = 6378388.0
        val e2 = 6.722670022e-3

        val A0 = 1.0 - e2 / 4.0 - 3.0 * Math.pow(e2, 2.0) / 64.0
        val A2 = 3.0 / 8.0 * (e2 + Math.pow(e2, 2.0) / 4.0)
        val A4 = 15.0 / 256.0 * Math.pow(e2, 2.0)
        val delta_N = n - N0

        // 2. iterations
        val phi_rou: Double

        var fa = 0.5
        val fd = 1e-30
        var fb = ((delta_N + M0) / m0 / a + A2 * Math.sin(2 * fa) - A4 * Math.sin(4 * fa)) / A0
        var k = 0
        while (fa - fb > fd || fa - fb < -1 * fd) {
            fa = fb
            fb = ((delta_N + M0) / m0 / a + A2 * Math.sin(2 * fa) - A4 * Math.sin(4 * fa)) / A0
            if (++k > 1000) {
                Log.d("hk1980GridToLatLng", "The equation does not converge. Computation Stopped.")
                fb = 0.0
                break
            }
        }
        phi_rou = fb

        // 3. Verification of the value
        //  a*(A0 * phi_rou - A2 * sin(2 * phi_rou) + A4 * sin(4*phi_rou))
        // (delta_N + M0) / m0

        val t_rou = Math.tan(phi_rou)
        val niu_rou = a / Math.sqrt(1.0 - e2 * Math.pow(Math.sin(phi_rou), 2.0))
        val rou_rou = a * (1.0 - e2) / Math.pow(1 - e2 * Math.pow(Math.sin(phi_rou), 2.0), 1.5)
        val psi_rou = niu_rou / rou_rou

        val delta_E = e - E0

        // Equation 4
        val lambda = lambda0 / (180.0 / Math.PI) + 1.0 / Math.cos(phi_rou) * (delta_E / (m0 * niu_rou)) - 1 / Math.cos(phi_rou) * (Math.pow(delta_E, 3.0) / (6.0 * Math.pow(m0, 3.0) * Math.pow(niu_rou, 3.0))) * (psi_rou + 2 * Math.pow(t_rou, 2.0))

        // Equation 5
        val phi = phi_rou - t_rou / (m0 * rou_rou) * (Math.pow(delta_E, 2.0) / (2.0 * m0 * niu_rou))

        var latitude = phi * (180.0 / Math.PI)
        var longitude = lambda * (180.0 / Math.PI)

        //////// HK80GEO_TO_WGS84GEO ////////
        latitude -= 5.5 / 3600.0
        longitude += 8.8 / 3600.0

        return LatLng(latitude, longitude)
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
     * Function to return timestamp in EAT Time String
     * @return timestamp in second
     */
    @SuppressLint("SimpleDateFormat")
    fun dateStrToTimestamp(timeStr: String, pattern: String): Long {
        return try {
            SimpleDateFormat(pattern, Locale.US).parse(timeStr).time / Time.ONE_SECOND_IN_MILLIS
        } catch (e: Exception) {
            loge("XXX", e)
            -1L
        }
    }

    /**
     * Function to return EAT Time String from timestamp
     *
     * @param timestamp timestamp in second
     * @return time string (HH:mm)
     */
    @SuppressLint("SimpleDateFormat")
    fun timestampToTimeStr(timestamp: Long): String {
        val etaTimeFormat = SimpleDateFormat("HH:mm")
        return etaTimeFormat.format(Date(timestamp * 1000))
    }

    fun getNameFromAddress(address: Address): String {
        return if (address.featureName.isBlank() ||
            address.featureName.contains("Unnamed Road".toRegex()) ||
            address.featureName.matches("香港".toRegex()) ||
            address.featureName.matches("^[0-9]+[a-zA-Z]?(-[0-9]*[a-zA-Z]?)?$".toRegex()))
        {
            val fullAddress = address.getAddressLine(0)
            if (fullAddress.contains(',')) {
                if (fullAddress.startsWith("Unnamed Road")) {
                    fullAddress.split(',')[1].trim()
                } else {
                    fullAddress.split(',')[0].trim()
                }
            } else if (!address.thoroughfare.isNullOrBlank() && fullAddress.contains(address.thoroughfare)){
                fullAddress.substring(fullAddress.indexOf(address.thoroughfare))
            } else {
                fullAddress
            }
        } else {
            address.featureName
        }
    }

    fun replaceSpecialCharacters(str: String): String {
        return str.replace("\uE473".toRegex(), "邨")
                .replace("\uE2B4".toRegex(), "璧")
                .replace("\uE88C".toRegex(), "埗")
                .replace("\uE1D0".toRegex(), "栢")
                .replace("\uE05E".toRegex(), "匯")
    }

    fun phaseFromTo(str: String): String {
        // TODO("Need to Support English")
        return str.replace("公共運輸".toRegex(), "")
            .replace("公共交通".toRegex(), "")
            .replace("運輸交匯處".toRegex(), "")
            .replace("交匯處".toRegex(), "")
            .replace("臨時巴士總站".toRegex(), "")
            .replace("巴士總站".toRegex(), "")
            .replace("總站".toRegex(), "")
            .replace("鐵路站".toRegex(), "站")
            .replace("渡輪碼頭".toRegex(), "碼頭")
            .trim()
    }

    /**
     * Function to return valid ETA Result's message from EAT Time String
     * @return message in ETAResult
     */
    fun timeStrToMsg(timeStr: String): String {
        // TODO("Need to Support English")
        return replaceSpecialCharacters(timeStr)
                .replace("　".toRegex(), " ")
                .replace("班次".toRegex(), "")
                .replace("時段".toRegex(), "")
                .replace("預定".toRegex(), "")
                .replace("距離.*公里".toRegex(), "")
                .replace("未開出".toRegex(), "")
                .replace("預計時間".toRegex(), "")
                .trim()
    }

    /**
     * Function to check if ETA is schedule only from EAT Time String
     * @return message in ETAResult
     */
    fun isScheduledOnly(timeStr: String): Boolean {
        // TODO("Need to Support English")
        return timeStr.contains("預定".toRegex()) || timeStr.contains("預計時間".toRegex())
    }

    fun isLocationMatch(locationStr1: String, locationStr2: String): Boolean {
        return locationStr1.trim() == locationStr2.trim() ||
                locationStr1.trim().startsWith(locationStr2.trim().substring(0, 2)) ||
                locationStr1.trim().contains(locationStr2.trim()) ||
                locationStr2.trim().contains(locationStr1.trim())
    }

    fun phaseCapacity(capacityStr: String): Long {
        return when (capacityStr.toUpperCase()) {
            "N" -> -1
            "E" -> 0
            "F" -> 10
            else -> capacityStr.toLong()
        }
    }

    fun getCapacityResId(capacity: Long): Int{
        return when (capacity) {
            0L -> R.drawable.ic_text_capacity_0
            1L -> R.drawable.ic_text_capacity_1
            2L -> R.drawable.ic_text_capacity_2
            3L -> R.drawable.ic_text_capacity_3
            4L -> R.drawable.ic_text_capacity_4
            5L -> R.drawable.ic_text_capacity_5
            6L -> R.drawable.ic_text_capacity_6
            7L -> R.drawable.ic_text_capacity_7
            8L -> R.drawable.ic_text_capacity_8
            9L -> R.drawable.ic_text_capacity_9
            10L -> R.drawable.ic_text_capacity_10
            else -> R.drawable.ic_text_capacity_0
        }
    }
}
