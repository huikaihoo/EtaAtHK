package hoo.etahk.remote.connection

import android.util.Log
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.response.KmbEtaRes
import hoo.etahk.remote.response.KmbStopsRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object KmbConnection: BaseConnection {

    private val TAG = "KmbConnection"

    /***************
     * Get Stops
     ***************/
    override fun getStops(route: Route) {
        ConnectionHelper.kmb.getStops(
                route = route.routeKey.routeNo,
                bound = route.routeKey.bound.toString(),
                serviceType = route.routeKey.variant.toString())
                .enqueue(object : Callback<KmbStopsRes> {
                    override fun onFailure(call: Call<KmbStopsRes>, t: Throwable) {}
                    override fun onResponse(call: Call<KmbStopsRes>, response: Response<KmbStopsRes>) {
                        val t = Utils.getCurrentTimestamp()
                        val kmbStopsRes = response.body()
                        Log.d(TAG, kmbStopsRes.toString())

                        if (kmbStopsRes?.data?.routeStops != null && (kmbStopsRes.data.routeStops).isNotEmpty()) {
                            val stops = mutableListOf<Stop>()
                            (kmbStopsRes.data.routeStops).forEach {
                                stops.add(toStop(route, it!!, t))
                            }

                            Log.d(TAG, AppHelper.gson.toJson(stops))
                            AppHelper.db.stopsDao().insertOrUpdate(route, stops, t)
                            stops.forEach {
                                updateEta(it)
                            }
                        }
                    }
                })
    }

    private fun toStop(route: Route, routeStop: KmbStopsRes.RouteStop, t: Long): Stop {
        val info = Info()

        val stop = Stop(
                routeKey = route.routeKey,
                seq = routeStop.seq?: 0,
                name = StringLang(routeStop.cName?: "", routeStop.eName?: "", routeStop.sCName?: ""),
                to = route.getToByBound(),
                details = StringLang(routeStop.cLocation?: "", routeStop.eLocation?: "", routeStop.sCLocation?: ""),
                fare = routeStop.airFare?: 0.0,
                info = Info(stopId = routeStop.bsiCode ?: ""),
                updateTime = t
        )
        if (routeStop.x != null && routeStop.y != null)
            stop.location = Utils.hk1980GridToLatLng(routeStop.x, routeStop.y)
        return stop
    }

    /***************
     * Update ETA
     ***************/
    override fun updateEta(stop: Stop) {
        ConnectionHelper.kmbEta.getEta(
                route = stop.routeKey.routeNo,
                bound = stop.routeKey.bound.toString(),
                stop = stop.info.stopId,
                stop_seq = stop.seq.toString(),
                serviceType = stop.routeKey.variant.toString(),
                lang = "tc")
                .enqueue(object : Callback<KmbEtaRes> {
                    override fun onFailure(call: Call<KmbEtaRes>, t: Throwable) {}
                    override fun onResponse(call: Call<KmbEtaRes>, response: Response<KmbEtaRes>){
                        val t = Utils.getCurrentTimestamp()
                        val kmbEtaRes = response.body()
                        Log.d(TAG, kmbEtaRes.toString())

                        if (kmbEtaRes?.response != null && (kmbEtaRes.response).isNotEmpty()) {
                            val etaResults = mutableListOf<EtaResult>()
                            (kmbEtaRes.response).forEach {
                                etaResults.add(toEtaResult(stop, it))
                            }
                            stop.etaResults = etaResults
                            stop.etaUpdateTime = t

                            Log.d(TAG, AppHelper.gson.toJson(etaResults))
                            AppHelper.db.stopsDao().update(stop)
                        }
                    }
                })
    }

    private fun toEtaResult(stop: Stop, response: KmbEtaRes.Response): EtaResult {
        return EtaResult(
                company = stop.routeKey.company,
                etaTime = Utils.timeStrToTimestamp(response.t ?: ""),
                msg = Utils.timeStrToMsg(response.t ?: ""),
                scheduleOnly = Utils.isScheduledOnly(response.t ?: ""),
                gps = (response.ei != null && response.ei == "N"),
                variant = response.busServiceType,
                wifi = (response.wifi != null && response.wifi == "Y")
        )
    }
}