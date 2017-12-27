package hoo.etahk.remote.connection

import android.util.Log
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.remote.api.KmbApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object KmbConnection: BaseConnection {

    private val TAG = "KmbConnection"

    override fun updateEta(stop: Stop) {
        ConnectionHelper.kmbEta.getEta(
                route = "85B",
                bound = "1",
                stop = "HI01N11000",
                stop_seq = "9",
                serviceType = "01",
                lang = "tc")
                .enqueue(object : Callback<KmbApi.KmbEtaResponse> {
                    override fun onFailure(call: Call<KmbApi.KmbEtaResponse>, t: Throwable) {}
                    override fun onResponse(call: Call<KmbApi.KmbEtaResponse>, response: Response<KmbApi.KmbEtaResponse>){
                        val kmbResponse = response.body()
                        Log.d(TAG, kmbResponse.toString())

                        if (kmbResponse?.response != null && (kmbResponse.response)!!.isNotEmpty()) {
                            val etaResults = mutableListOf<EtaResult>()
                            (kmbResponse.response)!!.forEach {
                                etaResults.add(toEtaResult(stop, it))
                            }
                            stop.etaResults = etaResults
                            stop.etaUpdateTime = Utils.getCurrentTimestamp()

                            Log.d(TAG, AppHelper.gson.toJson(etaResults))
                            AppHelper.db.stopsDao().insert(stop)
                        }
                    }
                })

//        val request = Request.Builder()
//                .url(stop.etaUrl)
//                .build()
//
//        Log.d(TAG, request.url().toString())
//        AppHelper.okHttp.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {}
//            override fun onResponse(call: Call, response: Response){
//                val responseStr = response.body()?.string()
//                Log.d(TAG, responseStr)
//
//                val kmbResponse = AppHelper.gson.fromJson(responseStr, KmbService.KmbEtaResponse::class.java)
//
//                if (kmbResponse.response != null && (kmbResponse.response)!!.isNotEmpty()) {
//                    val etaResults = mutableListOf<EtaResult>()
//                    (kmbResponse.response)!!.forEach {
//                        etaResults.add(toEtaResult(stop, it))
//                    }
//                    stop.etaResults = etaResults
//                    stop.etaUpdateTime = Utils.getCurrentTimestamp()
//                    //stop.etaResultsStr = gson.toJson(etaResultsStr)
//                    Log.d(TAG, AppHelper.gson.toJson(etaResults))
//                    AppHelper.db.stopsDao().insert(stop)
//                }
//            }
//        })
    }

    private fun toEtaResult(stop: Stop, response: KmbApi.KmbEtaResponse.Response): EtaResult {
        return EtaResult(
                company = stop.routeKey.company,
                etaTime = Utils.timeStrToTimestamp(response.t ?: ""),
                msg = Utils.timeStrToMsg(response.t ?: ""),
                scheduleOnly = Utils.isScheduledOnly(response.t ?: ""),
                gps = (response.ei != null && response.ei.equals("N")),
                variant = response.busServiceType,
                wifi = (response.wifi != null && response.wifi.equals("Y"))
        )
    }



}