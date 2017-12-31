package hoo.etahk.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.TypedValue
import com.google.android.gms.maps.model.LatLng
import hoo.etahk.R
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

    fun getThemeColorAccent(context: Context): Int {
        return getThemeColor(context, R.attr.colorAccent)
    }

    fun getThemeColor(context: Context, resId: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(resId, value, true)
        return value.data
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
            k = k + 1
            if (k > 1000) {
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