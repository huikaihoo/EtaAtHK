package hoo.etahk.remote.connection

import com.google.firebase.perf.metrics.AddTrace
import com.mcxiaoke.koi.HASH
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.remote.request.MtrbEtaReq
import hoo.etahk.remote.response.GistDatabaseRes
import hoo.etahk.remote.response.MtrbEtaRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


object MtrbConnection: BaseConnection {

    /***************
     * Shared
     ***************/
    fun getKey(): String {
        return HASH.md5("mtrMobile_" + Utils.getDateTimeString(Utils.getCurrentTimestamp(), "yyyyMMddHHmm"))
    }

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    /**
     * Get List of Parent Routes
     *
     * @param company company code
     * @return map of route no to list of parent routes
     */
    override fun getParentRoutes(company: String): ParentRoutesMap? {
        val t = Utils.getCurrentTimestamp()
        val result = ParentRoutesMap()
        val gistId = SharedPrefsHelper.get<String>(R.string.param_gist_id_mtrb)

        try {
            val response = ConnectionHelper.gist.getGist(gistId).execute()

            logd("gistId = $gistId; isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val gistFile = response.body()?.files?.get(company.toLowerCase())
                val gistDatabaseRes =
                    if (gistFile != null) toGistDatabaseRes(company.toLowerCase(), gistFile, t) else GistDatabaseRes()

                logd("gistDatabaseRes.isValid = ${gistDatabaseRes.isValid}")

                if (gistDatabaseRes.isValid) {
                    result.addAll(gistDatabaseRes.parentRoutes)

                    val childRoutesMap = gistDatabaseRes.childRoutes.groupBy {
                        RouteKey(
                            company = it.routeKey.company,
                            routeNo = it.routeKey.routeNo,
                            bound = it.routeKey.bound,
                            variant = 0L
                        )
                    }
                    childRoutesMap.forEach { (routeKey, childRoutes) ->
                        AppHelper.db.childRouteDao().insertOrUpdate(childRoutes, t)
                    }
                    logd("After insert child routes")

                    if (gistDatabaseRes.stops.isNotEmpty()) {
                        AppHelper.db.stopDao().insertOnDeleteOld(listOf(Constants.Company.MTRB), gistDatabaseRes.stops)
                    }
                }

                logd("response ${result.size}")
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        return result
    }

    override fun getParentRoute(routeKey: RouteKey): Route? {
        return null
    }

    override fun getChildRoutes(parentRoute: Route) {
        return
    }

    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        if (needEtaUpdate) {
            val stops = AppHelper.db.stopDao().selectOnce(
                route.routeKey.company,
                route.routeKey.routeNo,
                route.routeKey.bound,
                route.routeKey.variant)
            updateEta(stops)
        }
        return
    }

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    override fun getTimetableUrl(route: Route): String? {
        return null
    }

    override fun getTimetable(route: Route): String? {
        return null
    }

    override fun updateEta(stop: Stop) {
        return
    }

    @AddTrace(name = "MtrbConnection_updateEta")
    override fun updateEta(stops: List<Stop>) {
        val t = Utils.getCurrentTimestamp()
        val stopsMapByRouteNo = stops.groupBy { stop -> stop.routeKey.routeNo }
        val stopsMapById = stops.groupBy { stop -> stop.info.stopId }

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                stopsMapByRouteNo.forEach { (routeNo, stopList) ->
                    jobs += GlobalScope.launch(Dispatchers.Default) {
                        stopList.forEach { stop ->
                            stop.etaStatus = Constants.EtaStatus.FAILED
                            stop.etaUpdateTime = t
                        }

                        try {
                            val response =
                                ConnectionHelper.mtrb.getEta(
                                    MtrbEtaReq(key = getKey(),
                                        routeName = routeNo)
                                ).execute()

                            if (response.isSuccessful) {
                                val mtrbEtaRes = response.body()
                                val responseTime = Utils.getCurrentTimestamp()

                                if (!mtrbEtaRes?.busStop.isNullOrEmpty()) {
                                    mtrbEtaRes?.busStop?.forEach {
                                        it.busStopId?.let { stopId ->
                                            val stopsById = stopsMapById[stopId]
                                            if (!stopsById.isNullOrEmpty()) {
                                                val stop = stopsById[0]
                                                val etaResults = mutableListOf<EtaResult>()

                                                if (it.isSuspended == "1") {
                                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_service_suspended)))
                                                } else if (stop.seq == stop.info.endSeq)  {
                                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info)))
                                                } else if (!it.bus.isNullOrEmpty()) {
                                                    (it.bus).forEach {
                                                        etaResults.add(toEtaResult(stop, it, responseTime))
                                                    }
                                                } else {
                                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info)))
                                                }

                                                if (etaResults.isNotEmpty()) {
                                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                                    stop.etaResults = etaResults
                                                    //logd(AppHelper.gson.toJson(stop.etaResults))
                                                }
                                            }
                                        }
                                    }
                                }

                                val msg = when (mtrbEtaRes?.routeStatusRemarkTitle.isNullOrBlank()) {
                                    true -> AppHelper.getString(R.string.eta_msg_no_eta_info)
                                    false -> mtrbEtaRes!!.routeStatusRemarkTitle!!
                                }

                                stopList.filter { it.etaStatus != Constants.EtaStatus.SUCCESS }.forEach { stop ->
                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                    stop.etaResults = listOf(toEtaResult(stop, msg))
                                }
                            }
                        } catch (e: Exception) {
                            loge("updateEta::stopsMapByRouteNo.forEach failed!", e)
                            stopList.forEach { stop ->
                                stop.etaStatus = Constants.EtaStatus.NETWORK_ERROR
                            }
                        }
                    }
                }
                jobs.forEach { it.join() }
            }

            AppHelper.db.stopDao().updateOnReplace(stops)
        } catch (e: Exception) {
            loge("updateEta failed!", e)
        }
    }

    private fun toEtaResult(stop: Stop, bus: MtrbEtaRes.BusStop.Bus, responseTime: Long): EtaResult {
        var etaTime = responseTime
        var msg: String

        val arrivalTime = bus.arrivalTimeInSecond?.toLong() ?: etaTime
        val departureTime = bus.departureTimeInSecond?.toLong() ?: etaTime
        if (departureTime < arrivalTime) {
            etaTime += departureTime
            msg = bus.departureTimeText ?: ""
        } else {
            etaTime += arrivalTime
            msg = bus.arrivalTimeText ?: ""
        }

        msg = msg.replace("([0-9]+)[ ]*分鐘".toRegex(), "")
            .replace("即將.*離開".toRegex(), "")
            .trim()

        if (!bus.busRemark.isNullOrBlank()) {
            if (msg.contains("交通擠塞")) { // or bus.isDelayed == null
                msg += (" 交通擠塞")
            }
        }

        msg = Utils.timestampToTimeStr(etaTime) + " " + msg

        return EtaResult(
            company = stop.routeKey.company,
            etaTime = etaTime,
            msg = Utils.timeStrToMsg(msg),
            scheduleOnly = bus.isScheduled != "0",
            gps = (bus.busLocation?.latitude ?: 0.0) > 0.0 && (bus.busLocation?.longitude ?: 0.0) > 0.0
        )
    }
}