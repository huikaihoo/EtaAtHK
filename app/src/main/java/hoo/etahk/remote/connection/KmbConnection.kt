package hoo.etahk.remote.connection

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.perf.metrics.AddTrace
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.extensions.yn
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Path
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.response.GistDatabaseRes
import hoo.etahk.remote.response.KmbBoundVariantRes
import hoo.etahk.remote.response.KmbEtaRes
import hoo.etahk.remote.response.KmbStopsRes
import hoo.etahk.remote.response.KmbTimetableRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object KmbConnection: BaseConnection {

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
        val gistId = SharedPrefsHelper.get<String>(R.string.param_gist_id_kmb)

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
                        AppHelper.db.stopDao().insertOnDeleteOld(listOf(Constants.Company.KMB, Constants.Company.LWB), gistDatabaseRes.stops)
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

    /**
     * Get Child Route by Parent Route and update into DB
     *
     *  @param parentRoute parent route
     */
    override fun getChildRoutes(parentRoute: Route) {
        for (bound in 1..parentRoute.boundCount) {
            try {
                val prefix = "[${parentRoute.routeKey.routeNo}][$bound]"

                val response = ConnectionHelper.kmb.getBoundVariant(
                            route = parentRoute.routeKey.routeNo,
                            bound = bound.toString()).execute()

                logd("$prefix isSuccessful = ${response.isSuccessful}")

                if (response.isSuccessful) {
                    GlobalScope.launch(Dispatchers.Default) {
                        val t = Utils.getCurrentTimestamp()
                        val kmbBoundVariantRes = response.body()
                        //logd(kmbBoundVariantRes.toString())

                        if (kmbBoundVariantRes?.data?.routes != null && kmbBoundVariantRes.data.routes.isNotEmpty()) {
                            val routes = mutableListOf<Route>()
                            (kmbBoundVariantRes.data.routes).forEach {
                                //logd("${it?.bound} == $bound")
                                assert(it!!.bound!! == bound)
                                routes.add(toChildRoute(parentRoute, it, t))
                            }

                            logd("$prefix response childroutes ${routes.size}")
                            AppHelper.db.childRouteDao().insertOrUpdate(routes, t)
                        }
                    }
                }
            } catch (e: Exception) {
                loge("getChildRoutes failed!", e)
            }
        }
    }

    private fun toChildRoute(parentRoute: Route, route: KmbBoundVariantRes.Route, t: Long): Route {
        return Route(
                routeKey = parentRoute.routeKey.copy(bound = route.bound!!, variant = route.serviceType?.trim()?.toLong()?: 1),
                direction = parentRoute.childDirection,
                specialCode = parentRoute.specialCode,
                companyDetails = parentRoute.companyDetails,
                from = StringLang(route.originChi?: "", route.originEng?: ""),
                to = StringLang(route.destinationChi?: "", route.destinationEng?: ""),
                details = StringLang(route.descChi?: "", route.descEng?: ""),
                displaySeq = parentRoute.displaySeq,
                typeSeq = parentRoute.typeSeq,
                updateTime = t
        )
    }

    /**
     * Get list of stops and path by Child Route and update into DB
     *
     * @param route Child Route
     * @param needEtaUpdate update eta of stops as well if true
     */
    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        val prefix = "[${route.routeKey}]"

        try {
            val response = ConnectionHelper.kmb.getStops(
                    route = route.routeKey.routeNo,
                    bound = route.routeKey.bound.toString(),
                    serviceType = route.routeKey.variant.toString()).execute()

            logd("$prefix isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Default) {
                    val t = Utils.getCurrentTimestamp()
                    val kmbStopsRes = response.body()
                    //logd(kmbStopsRes.toString())

                    // Add Paths to database
                    if (kmbStopsRes?.data?.route?.lineGeometry != null && kmbStopsRes.data.route.lineGeometry.isNotEmpty()) {
                        val paths = mutableListOf<Path>()

                        val strPaths = kmbStopsRes.data.route.lineGeometry.replace("{paths:", "")
                            .replace("}", "")
                        val arrPaths =
                            AppHelper.gson.fromJson(strPaths, Array<Array<DoubleArray>>::class.java)

                        var seq = 0L
                        arrPaths.forEachIndexed { section, arrPaths2 ->
                            arrPaths2.forEach { point ->
                                if (point.size == 2) {
                                    val latLng = Utils.hk1980GridToLatLng(point[1], point[0])
                                    paths.add(toPath(route, latLng, seq++, section.toLong(), t))
                                }
                            }
                        }

                        logd("$prefix paths response ${paths.size}")
                        AppHelper.db.pathDao().insertOrUpdate(route, paths, t)
                    }

                    // Add Stops to database
                    if (kmbStopsRes?.data?.routeStops != null && kmbStopsRes.data.routeStops.isNotEmpty()) {
                        val stops = mutableListOf<Stop>()
                        (kmbStopsRes.data.routeStops).forEach {
                            stops.add(toStop(route, it!!, t))
                        }

                        logd("$prefix stops response ${stops.size}")
                        AppHelper.db.stopDao().insertOrUpdate(route.routeKey, stops, t)

                        if (needEtaUpdate)
                            updateEta(stops)
                    }
                }
            }
        } catch (e: Exception) {
            loge("getStops failed!", e)
        }
    }

    private fun toPath(route: Route, latLng: LatLng, seq: Long, section: Long, t: Long): Path {
        val path = Path(
            routeKey = route.routeKey.copy(),
            seq = seq,
            section = section,
            updateTime = t
        )
        path.latLng = latLng
        return path
    }

    private fun toStop(route: Route, routeStop: KmbStopsRes.RouteStop, t: Long): Stop {
        val stop = Stop(
                routeKey = route.routeKey.copy(),
                seq = routeStop.seq?: 0,
                name = StringLang(routeStop.cName?: "", routeStop.eName?: "", routeStop.sCName?: ""),
                to = route.to,
                details = StringLang(routeStop.cLocation?: "", routeStop.eLocation?: "", routeStop.sCLocation?: ""),
                fare = routeStop.airFare?: 0.0,
                info = Info(stopId = routeStop.bsiCode ?: ""),
                updateTime = t
        )
        if (routeStop.x != null && routeStop.y != null)
            stop.latLng = Utils.hk1980GridToLatLng(routeStop.y, routeStop.x)
        return stop
    }

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    override fun getTimetableUrl(route: Route): String? {
        return null
    }

    /**
     * Get timetable of route
     *
     * @param route Child Route
     * @return timetable of child route
     */
    override fun getTimetable(route: Route): String? {
        val prefix = "[${route.routeKey}]"
        var result = ""

        try {
            val response = ConnectionHelper.kmb.getTimetable(
                    route = route.routeKey.routeNo,
                    bound = route.routeKey.bound.toString()).execute()

            logd("$prefix isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                var lastDayType = ""
                val kmbTimetableRes = response.body()

                kmbTimetableRes?.data?.toList()?.sortedBy { it.first }?.forEach { pair ->
                    var variantHeader = true
                    pair.second?.forEach {
                        it?.let { rec ->
                            result += toTimetableMarkdown(
                                route = route,
                                rec = rec,
                                variantHeader = variantHeader,
                                tableHeader = rec.dayType != lastDayType,
                                thematicBreak = result.isNotEmpty())
                            variantHeader = false
                            lastDayType = rec.dayType.orEmpty()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            loge("getTimetable failed!", e)
        }

        //logd(result)
        return result
    }

    private fun toTimetableMarkdown(route: Route, rec: KmbTimetableRes.Timetable, variantHeader: Boolean, tableHeader: Boolean, thematicBreak: Boolean): String {
        var result = ""

        val boundText = (route.routeKey.bound == 1L).yn(rec.boundText1, rec.boundText2)
        val boundTime = (route.routeKey.bound == 1L).yn(rec.boundTime1, rec.boundTime2)

        if (!boundText.isNullOrBlank()) {
            // From To Header
            if (variantHeader) {
                if (thematicBreak) {
                    result += "\n***\n"
                }
                val variant = rec.serviceTypeChi.isNullOrBlank().yn(AppHelper.getString(R.string.normal_route), rec.serviceTypeChi)
                result += "## $variant\n"
            } else if (tableHeader) {
                result += "***"
            }

            // Table Header
            if (tableHeader || variantHeader) {
                result += "\n\n${toHeader(rec.dayType.orEmpty())}|"
                if (!boundTime.isNullOrBlank()) {
                    result += AppHelper.getString(R.string.headway_mins)
                }
                result += "\n---|"
                if (!boundTime.isNullOrBlank()) {
                    result += "---"
                }
                result += "\n"
            }

            // Table Content
            result += "$boundText|"
            if (!boundTime.isNullOrBlank()) {
                result += boundTime
            }
            result += "\n"
        }

        return result
    }

    private fun toHeader(dayType: String): String {
        val resId = when (dayType.trim()) {
            "MF" -> R.string.mon_to_fri
            "MS" -> R.string.mon_to_sat
            "S" -> R.string.sat
            "H" -> R.string.sun_holidays
            "D" -> R.string.daily
            else -> null
        }

        return if (resId != null) AppHelper.getString(resId) else ""
    }

    /**
     * Get Eta of list of stops and update into DB
     *
     * @param stops list of stops
     */
    @AddTrace(name = "KmbConnection_updateEta")
    override fun updateEta(stops: List<Stop>) {
        val t = Utils.getCurrentTimestamp()

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                stops.forEach { stop ->
                    jobs += GlobalScope.launch(Dispatchers.Default) {
                        stop.etaStatus = Constants.EtaStatus.FAILED
                        stop.etaUpdateTime = t
                        try {
                            val response =
                                ConnectionHelper.kmbEta.getEta(
                                    route = stop.routeKey.routeNo,
                                    bound = stop.routeKey.bound.toString(),
                                    stop = stop.info.stopId,
                                    stop_seq = stop.seq.toString(),
                                    serviceType = stop.routeKey.variant.toString(),
                                    lang = "tc").execute()

                            if (response.isSuccessful) {
                                val kmbEtaRes = response.body()
                                //logd(kmbEtaRes.toString())

                                val etaResults = mutableListOf<EtaResult>()

                                if (kmbEtaRes?.response != null && kmbEtaRes.response.isNotEmpty()) {
                                    (kmbEtaRes.response).forEach {
                                        etaResults.add(toEtaResult(stop, it))
                                    }
                                    //logd(AppHelper.gson.toJson(etaResults))
                                } else {
                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info)))
                                }

                                if (etaResults.isNotEmpty()) {
                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                    stop.etaResults = etaResults
                                    //logd(AppHelper.gson.toJson(stop.etaResults))
                                }
                            }
                        } catch (e: Exception) {
                            loge("updateEta::stops.forEach failed!", e)
                            stop.etaStatus = Constants.EtaStatus.NETWORK_ERROR
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

    override fun updateEta(stop: Stop) {
        ConnectionHelper.kmbEta.getEta(
                route = stop.routeKey.routeNo,
                bound = stop.routeKey.bound.toString(),
                stop = stop.info.stopId,
                stop_seq = stop.seq.toString(),
                serviceType = stop.routeKey.variant.toString(),
                lang = "tc")
                .enqueue(object : Callback<KmbEtaRes> {
                    override fun onFailure(call: Call<KmbEtaRes>, t: Throwable) {
                        val t = Utils.getCurrentTimestamp()

                        stop.etaStatus = Constants.EtaStatus.FAILED
                        stop.etaUpdateTime = t

                        AppHelper.db.stopDao().update(stop)
                    }

                    override fun onResponse(call: Call<KmbEtaRes>, response: Response<KmbEtaRes>){
                        val t = Utils.getCurrentTimestamp()
                        val kmbEtaRes = response.body()
                        //logd(kmbEtaRes.toString())

                        if (kmbEtaRes?.response != null && kmbEtaRes.response.isNotEmpty()) {
                            val etaResults = mutableListOf<EtaResult>()
                            (kmbEtaRes.response).forEach {
                                etaResults.add(toEtaResult(stop, it))
                            }
                            stop.etaStatus = Constants.EtaStatus.SUCCESS
                            stop.etaResults = etaResults
                            stop.etaUpdateTime = t
                            //logd(AppHelper.gson.toJson(etaResults))
                        } else {
                            stop.etaStatus = Constants.EtaStatus.FAILED
                            stop.etaUpdateTime = t
                        }

                        AppHelper.db.stopDao().update(stop)
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
                wifi = (response.wifi != null && response.wifi == "Y"),     // changed to store wheelchair
                capacity = Utils.phaseCapacity(response.ol ?: "")
        )
    }
}