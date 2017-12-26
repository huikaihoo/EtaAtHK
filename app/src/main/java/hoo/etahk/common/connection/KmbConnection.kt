package hoo.etahk.common.connection

import android.util.Log
import com.google.gson.annotations.SerializedName
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.EtaResult
import hoo.etahk.model.data.Stop
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object KmbConnection: BaseConnection {

    private val TAG = "KmbConnection"

    override fun updateEta(stop: Stop) {
        val request = Request.Builder()
                .url(stop.etaUrl)
                .build()

        Log.d(TAG, request.url().toString())

        AppHelper.okHttp.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response){
                val responseStr = response.body()?.string()
                Log.d(TAG, responseStr)

                val kmbResponse = AppHelper.gson.fromJson(responseStr, KmbResponse::class.java)

                if (kmbResponse.response != null && (kmbResponse.response)!!.isNotEmpty()) {
                    val etaResults = mutableListOf<EtaResult>()
                    (kmbResponse.response)!!.forEach {
                        etaResults.add(toEtaResult(stop, it))
                    }
                    stop.etaResults = etaResults
                    stop.etaUpdateTime = Utils.getCurrentTimestamp()
                    //stop.etaResultsStr = gson.toJson(etaResultsStr)
                    Log.d(TAG, AppHelper.gson.toJson(etaResults))
                    AppHelper.db.stopsDao().insert(stop)
                }
            }
        })
    }

    private fun toEtaResult(stop: Stop, response: KmbResponse.Response): EtaResult {
        return EtaResult(
                company = stop.routeKey.company,
                etaTime = Utils.timeStrToTimestamp(response.t?:""),
                msg = Utils.timeStrToMsg(response.t?:""),
                scheduledOnly = Utils.isScheduledOnly(response.t?:""),
                variant = response.busServiceType,
                wifi = (response.wifi != null && response.wifi.equals("Y"))
        )
    }

    internal class KmbResponse {
        var updated: Long = 0L
        var generated: Long = 0L
        @SerializedName("responsecode")
        var responseCode: Long = 0L
        var response: List<Response>? = emptyList()

        internal class Response {
            @SerializedName("bus_service_type")
            var busServiceType: Long = 0L
            var t: String? = null        // time (hh:mm xxxx)
            var ei: String? = null       // cannot trust: Is Scheduled Only (Y/N)
            var w: String? = null        // wheelchair (Y/N/"")
            var eot: String? = null      // E: time only; T: with text
            var ol: String? = null       // (N)
            var ex: String? = null       // expire time (YYYY-MM-DD hh:mm:ss)
            var wifi: String? = null     // wifi (null/Y)
        }
    }

}