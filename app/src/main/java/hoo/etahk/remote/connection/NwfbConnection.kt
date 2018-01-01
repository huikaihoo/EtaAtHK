package hoo.etahk.remote.connection

import com.mcxiaoke.koi.HASH
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Eta
import hoo.etahk.common.Utils
import hoo.etahk.common.Utils.timeStrToMsg
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.view.App
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


object NwfbConnection: BaseConnection {

    private val TAG = "BaseConnection"

    /***************
     * Shared
     ***************/
    fun getSystemCode(): String {
        var randomInt = Integer.toString(Random().nextInt(1000))
        while (randomInt.length < 4) {
            randomInt += "0"
        }

        var timestamp = Utils.getCurrentTimestamp().toString()
        timestamp = timestamp.substring(timestamp.length - 6)

        return timestamp + randomInt + HASH.md5(timestamp + randomInt + "firstbusmwymwy")
    }

    /*******************
     * Get Child Routes
     *******************/
    override fun getChildRoutes(parentRoute: Route) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /***************
     * Get Stops
     ***************/
    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        val info = "1|*|${route.routeKey.company}||${route.info.rdv}||${route.info.startSeq}||${route.info.endSeq}"
        //Log.d(TAG, "info=[$info]")

        ConnectionHelper.nwfb.getStops(
                info = info,
                l = "0",
                syscode = getSystemCode())
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val t = Utils.getCurrentTimestamp()
                        val responseStr = response.body()?.string()
                        //Log.d(TAG, responseStr)

                        if (!responseStr.isNullOrBlank()) {
                            val stops = mutableListOf<Stop>()
                            val nwfbResponse = responseStr!!.split("<br>")

                            nwfbResponse.forEach({
                                val records = it.split("\\|\\|".toRegex())
                                if(records.size >= Constants.Stop.NWFB_STOP_RECORD_SIZE) {
                                    stops.add(toStop(route, records, t))
                                }
                                //Log.d(TAG, it)
                            })

                            //Log.d(TAG, AppHelper.gson.toJson(stops))
                            AppHelper.db.stopsDao().insertOrUpdate(route, stops, t)
                            if (needEtaUpdate)
                                stops.forEach { updateEta(it) }
                        }
                    }
                })
    }

    private fun toStop(route: Route, records: List<String>, t: Long): Stop {
        assert(records.size >= Constants.Stop.NWFB_STOP_RECORD_SIZE)

        return Stop(
                routeKey = route.routeKey,
                seq = records[Constants.Stop.NWFB_STOP_RECORD_SEQ].toLong(),
                name = StringLang.newInstance(records[Constants.Stop.NWFB_STOP_RECORD_DETAILS].substringBefore(",").trim()),
                to = StringLang.newInstance(records[Constants.Stop.NWFB_STOP_RECORD_TO].trim()),
                details = StringLang.newInstance(records[Constants.Stop.NWFB_STOP_RECORD_DETAILS].trim()),
                latitude = records[Constants.Stop.NWFB_STOP_RECORD_LATITUDE].toDouble(),
                longitude =records[Constants.Stop.NWFB_STOP_RECORD_LONGITUDE].toDouble(),
                fare = records[Constants.Stop.NWFB_STOP_RECORD_FARE].toDouble(),
                info = Info(rdv = records[Constants.Stop.NWFB_STOP_RECORD_RDV].trim(),
                        bound = route.info.bound,
                        stopId = records[Constants.Stop.NWFB_STOP_RECORD_STOPID].toInt().toString()),
                updateTime = t
        )
    }

    /***************
     * Update ETA
     ***************/
    override fun updateEta(stop: Stop) {
        ConnectionHelper.nwfb.getEta(
                stopid = stop.info.stopId,
                service_no = stop.routeKey.routeNo,
                removeRepeatedSuspend = "Y",
                interval = "60",
                l = "0",
                bound = stop.info.bound,
                stopseq = stop.seq.toString(),
                rdv = stop.info.rdv,
                showtime = "Y",
                syscode = getSystemCode())
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        val t = Utils.getCurrentTimestamp()

                        stop.etaStatus = Constants.EtaStatus.FAILED
                        stop.etaUpdateTime = t

                        AppHelper.db.stopsDao().update(stop)
                    }
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>){
                        val t = Utils.getCurrentTimestamp()
                        var responseStr = response.body()?.string()
                        //Log.d(TAG, responseStr)

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
                                //Log.d(TAG, it)
                            })
                        }

                        if (!etaResults.isEmpty()) {
                            stop.etaStatus = Constants.EtaStatus.SUCCESS
                            stop.etaResults = etaResults
                            stop.etaUpdateTime = t
                            //Log.d(TAG, AppHelper.gson.toJson(stop.etaResults))
                        } else {
                            stop.etaStatus = Constants.EtaStatus.FAILED
                            stop.etaUpdateTime = t
                        }

                        AppHelper.db.stopsDao().update(stop)
                    }
                })
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

    // ETA with message only (no time)
    private fun toEtaResult(stop: Stop, msg: String): EtaResult {
        return EtaResult(
                company = stop.routeKey.company,
                etaTime = -1L,
                msg = msg,
                scheduleOnly = false,
                distance = -1L)
    }

    // Normal ETA
    private fun toEtaResult(records: List<String>): EtaResult {
        assert(records.size >= Eta.NWFB_ETA_RECORD_SIZE)

        val msg = (records[Eta.NWFB_ETA_RECORD_ETA_TIME] + " " + timeStrToMsg(records[Eta.NWFB_ETA_RECORD_MSG])).trim()

        val distance = records[Eta.NWFB_ETA_RECORD_DISTANCE].toLong()

        return EtaResult(
                company = records[Eta.NWFB_ETA_RECORD_COMPANY].trim(),
                etaTime = Utils.timeStrToTimestamp(records[Eta.NWFB_ETA_RECORD_ETA_TIME]),
                msg = msg,
                scheduleOnly = (distance <= 0),
                distance = distance)
    }
}
