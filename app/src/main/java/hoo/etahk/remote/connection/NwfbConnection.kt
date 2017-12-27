package hoo.etahk.remote.connection

import android.util.Log
import com.mcxiaoke.koi.HASH
import hoo.etahk.R
import hoo.etahk.common.Constants.Eta
import hoo.etahk.common.Utils
import hoo.etahk.common.Utils.timeStrToMsg
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.view.App
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


object NwfbConnection: BaseConnection {

    private val TAG = "BaseConnection"

    override fun updateEta(stop: Stop) {
//        val request = Request.Builder()
//                .url(appendSystemCode(stop.etaUrl))
//                .build()
//
//        Log.d(TAG, request.url().toString())
//
//        AppHelper.okHttp.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {}
//            override fun onResponse(call: Call, response: Response){
//                var responseStr = response.body()?.string()
//                Log.d(TAG, responseStr)
//
//                val etaResults = mutableListOf<EtaResult>()
//                val msg = getInvalidMsg(responseStr ?: "")
//
//                if (responseStr.isNullOrBlank() || !msg.isEmpty()) {
//                    etaResults.add(toEtaResult(stop, msg))
//
//                } else {
//                    responseStr = responseStr!!.replace(".*\\|##\\|".toRegex(), "")
//                    val nwfbResponse = responseStr.split("<br>")
//
//                    nwfbResponse.forEach({
//                        val records = it.split("(\\|\\|)|(\\|\\^\\|)".toRegex())
//                        if(records.size >= Eta.NWFB_ETA_RECORD_SIZE) {
//                            etaResults.add(toEtaResult(records))
//                        }
//                        Log.d(TAG, it)
//                    })
//                }
//
//                if (!etaResults.isEmpty()) {
//                    //stop.etaResultsStr = gson.toJson(etaResultsStr)
//                    stop.etaResults = etaResults
//                    stop.etaUpdateTime = Utils.getCurrentTimestamp()
//                    Log.d(TAG, AppHelper.gson.toJson(stop.etaResults))
//                    AppHelper.db.stopsDao().insert(stop)
//                }
//            }
//        })


        ConnectionHelper.nwfb.getEta(
                stopid = "1596",
                service_no = "E23",
                removeRepeatedSuspend = "Y",
                interval = "60",
                l = "0",
                bound = "I",
                stopseq = "16",
                rdv = "E23-TWS-2",
                showtime = "Y",
                syscode = getSystemCode())
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                        var responseStr = response.body()?.string()
                        Log.d(TAG, responseStr)

                        val etaResults = mutableListOf<EtaResult>()
                        val msg = getInvalidMsg(responseStr ?: "")

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

    fun getSystemCode(): String {
        var randomInt = Integer.toString(Random().nextInt(1000))
        while (randomInt.length < 4) {
            randomInt += "0"
        }

        var timestamp = Utils.getCurrentTimestamp().toString()
        timestamp = timestamp.substring(timestamp.length - 6)

        return timestamp + randomInt + HASH.md5(timestamp + randomInt + "firstbusmwymwy")
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
                scheduleOnly = false,
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
                scheduleOnly = (distance <= 0),
                distance = distance)
    }
}
