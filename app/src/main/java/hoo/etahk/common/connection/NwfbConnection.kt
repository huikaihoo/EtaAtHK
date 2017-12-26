package hoo.etahk.common.connection

import android.util.Log
import com.mcxiaoke.koi.HASH
import hoo.etahk.R
import hoo.etahk.common.Constants.Eta
import hoo.etahk.common.Utils
import hoo.etahk.common.Utils.timeStrToMsg
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.EtaResult
import hoo.etahk.model.data.Stop
import hoo.etahk.view.App
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*


object NwfbConnection: BaseConnection {

    private val TAG = "BaseConnection"

    override fun updateEta(stop: Stop) {
        val request = Request.Builder()
                .url(appendSystemCode(stop.etaUrl))
                .build()

        Log.d(TAG, request.url().toString())

        AppHelper.okHttp.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response){
                var responseStr = response.body()?.string()
                Log.d(TAG, responseStr)

                val etaResults = mutableListOf<EtaResult>()
                val msg = getInvalidMsg(responseStr?: "")

                if (responseStr.isNullOrBlank() || !msg.isEmpty()) {
                    etaResults.add(toEtaResult(stop, msg))

                } else {
                    responseStr = responseStr!!.replace(".*\\|##\\|".toRegex(), "")
                    val nwfbResponse = responseStr.split("<br>")

                    nwfbResponse.forEach({
                        val records = it.split("(\\|\\|)|(\\|\\^\\|)".toRegex())
                        if(records.size >= Eta.NWFB_ETA_RECORD_SIZE) {
                            etaResults.add(toEtaResult(records))
                        }
                        Log.d(TAG, it)
                    })
                }

                if (!etaResults.isEmpty()) {
                    //stop.etaResultsStr = gson.toJson(etaResultsStr)
                    stop.etaResults = etaResults
                    stop.etaUpdateTime = Utils.getCurrentTimestamp()
                    Log.d(TAG, AppHelper.gson.toJson(stop.etaResults))
                    AppHelper.db.stopsDao().insert(stop)
                }
            }
        })
    }

    fun appendSystemCode(url: String): String {
        var randomInt = Integer.toString(Random().nextInt(1000))
        while (randomInt.length < 4) {
            randomInt += "0"
        }

        var timestamp = Utils.getCurrentTimestamp().toString()
        timestamp = timestamp.substring(timestamp.length - 6)

        val syscode = timestamp + randomInt + HASH.md5(timestamp + randomInt + "firstbusmwymwy")

        return url +
                when ((url.indexOf("?") > -1)) {
                    true -> "&syscode="
                    false -> "?syscode="
                } + syscode + "&p=android&appversion=3.3"
    }

    // ETA Related
    private fun getInvalidMsg(responseStr: String): String {
        return when (responseStr.contains("\\|DISABLED\\|".toRegex())) {
            true -> App.Companion.instance.getString(R.string.eta_msg_no_eta_service)
            false -> {
                when (responseStr.contains("\\|HTML\\|".toRegex())) {
                    true -> when (responseStr.contains("服務時間已過")) {
                        true -> App.Companion.instance.getString(R.string.eta_msg_not_in_service_hours)
                        // TODO("Extract ETA message")
                        false -> App.Companion.instance.getString(R.string.eta_msg_no_eta_service)
                    }
                    false -> ""
                }
            }
        }
    }

    private fun toEtaResult(stop: Stop, msg: String): EtaResult {
        return EtaResult(
                company = stop.routeKey.company,
                etaTime = -1L,
                msg = msg,
                scheduledOnly = false,
                distance = -1L)
    }

    private fun toEtaResult(records: List<String>): EtaResult {
        assert(records.size >= Eta.NWFB_ETA_RECORD_SIZE)

        var msg = (records[Eta.NWFB_EAT_RECORD_ETA_TIME] + " " + timeStrToMsg(records[Eta.NWFB_EAT_RECORD_MSG])).trim()

        val distance = records[Eta.NWFB_EAT_RECORD_DISTANCE].toLong()

        return EtaResult(
                company = records[Eta.NWFB_EAT_RECORD_COMPANY].trim(),
                etaTime = Utils.timeStrToTimestamp(records[Eta.NWFB_EAT_RECORD_ETA_TIME]),
                msg = msg,
                scheduledOnly = (distance <= 0),
                distance = distance)
    }
}
