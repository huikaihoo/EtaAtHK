package hoo.etahk.remote.connection

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.perf.metrics.AddTrace
import com.google.gson.JsonSyntaxException
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.common.tools.Rule
import hoo.etahk.model.data.Path
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.api.GistApi
import hoo.etahk.remote.api.TramApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.koin.core.KoinComponent

open class TramConnection(
    private val tram: TramApi,
    private val tramEta: TramApi,
    private val gist: GistApi): BaseConnection, KoinComponent {

    /***************
     * Shared
     ***************/
    private val rules: List<Rule> by lazy {
        listOf(
            Rule(basedOn = "WB", target="11", excludeFrom = "105", excludeTo = "112"),
            Rule(basedOn = "WB", target="12", includeTo = "HVT_K"),
            Rule(basedOn = "WB", target="13", includeFrom = "HVT_K", excludeFrom = "50W", excludeTo = "50W"),
            Rule(basedOn = "EB", target="21", excludeFrom = "49E", excludeTo = "112"),
            Rule(basedOn = "EB", target="22", includeTo = "HVT_B"),
            Rule(basedOn = "EB", target="23", includeFrom = "HVT_B")
        )
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

        try {
            val response = tram.getDatabase().execute()

            if (response.isSuccessful) {
                // Get stop records from file
                val rawLines = (response.body()?.string() ?: "").split("\n".toRegex()).map {
                    it.trim().trimEnd(',').trim()
                }

                var boundStr = ""
                val recordsMap: HashMap<String, MutableList<Array<String>>> = hashMapOf()

                for (line in rawLines) {
                    // Start of lines that contain records
                    val matchResult = "var stopsArray([a-zA-z]*)[ ]*=".toRegex().find(line)
                    if (matchResult != null && matchResult.groupValues.size >= 2) {
                        boundStr = matchResult.groupValues[1].trim()
                        if (!recordsMap.containsKey(boundStr)) {
                            recordsMap[boundStr] = mutableListOf()
                        }
                    }

                    // End of lines that contain records
                    if (line.matches("][ ]*;".toRegex())) {
                        boundStr = ""
                    }

                    // Get records
                    if ( boundStr.isNotBlank() &&
                        line.isNotBlank() &&
                        line.startsWith('[') &&
                        line.endsWith(']') &&
                        !line.matches("//".toRegex()) ) {
                        try {
                            val records = AppHelper.gson.fromJson(line, Array<String>::class.java)
                            if (records.size >= Constants.Stop.TRAM_STOP_RECORD_SIZE) {
                                recordsMap[boundStr]?.add(records)
                            }
                        } catch (e: JsonSyntaxException) {
                            loge("Not a valid tram stop record [$line]")
                        }
                    }
                }

                // Sort stops records in orders
                recordsMap.forEach {
                    if (it.key == "WB") {
                        val index = it.value.indexOfFirst{ records -> records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID] == "WMT" }
                        if (index >= 0) {
                            it.value[index][Constants.Stop.TRAM_STOP_RECORD_STOP_ID] = "WM"
                        }
                    } else if (it.key == "EB") {
                        val hvFrom = it.value.indexOfFirst{ records -> records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID] == "105" }
                        val hvTo = it.value.indexOfFirst{ records -> records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID] == "49E" }

                        val after = mutableListOf<Array<String>>()

                        for (i in (it.value.size - 1) downTo hvTo) {
                            after.add(it.value[i])
                        }
                        for (i in hvFrom..hvTo) {
                            after.add(it.value[i])
                        }
                        for (i in (hvFrom - 1) downTo 0) {
                            after.add(it.value[i])
                        }
                        recordsMap["EB"] = after
                    }
                }

                // Check
//                recordsMap.forEach {
//                    logd(it.key)
//                    it.value.forEach {
//                        logd("${it[0]} ${it[1]} ${it[2]} ${it[3]} ${it[4]} ${it[5]}")
//                    }
//                }

                // Get parent routes / child routes / stops from records based on rules
                rules.forEach { rule ->
                    val recordsList = recordsMap[rule.basedOn]

                    if (recordsList != null) {
                        // Get from / to
                        var fromIndex = 0
                        var toIndex = recordsList.size - 1

                        if (rule.includeFrom.isNotBlank()) {
                            val index = recordsList.indexOfFirst{ it[Constants.Stop.TRAM_STOP_RECORD_STOP_ID] == rule.includeFrom }
                            fromIndex = if (index < 0) fromIndex else index
                        }
                        if (rule.includeTo.isNotBlank()) {
                            val index = recordsList.indexOfFirst{ it[Constants.Stop.TRAM_STOP_RECORD_STOP_ID] == rule.includeTo }
                            toIndex = if (index < 0) toIndex else index
                        }

                        // Create parent / child route
                        val parentRoute = toParentRoute(
                            routeNo = rule.target,
                            from = toName(recordsList[fromIndex]),
                            to = toName(recordsList[toIndex]),
                            displaySeq = result.size + 1L,
                            t = t
                        )
                        val childRoute = toChildRoute(parentRoute, 1L, t)

                        // Get stops based on rules
                        val stops = mutableListOf<Stop>()
                        var isExclude = false
                        var isExcludeEnded = false
                        for (i in fromIndex..toIndex) {
                            val records = recordsList[i]
                            isExclude = isExclude || (rule.excludeFrom == records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID])
                            if (!isExclude || isExcludeEnded) {
                                stops.add(toStop(childRoute, stops.size + 1L, records, t))
                            }
                            isExclude = isExclude && rule.excludeTo != records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID]
                            isExcludeEnded = isExcludeEnded || (rule.excludeTo == records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID])
                        }

                        result.add(parentRoute)
                        AppHelper.db.childRouteDao().insertOrUpdate(listOf(childRoute), t)
                        AppHelper.db.stopDao().insertOrUpdate(childRoute.routeKey, stops, t)
                    }
                }
                AppHelper.db.parentRouteDao().insertOrUpdate(result.getAll(), t)
                logd("Finish Update")
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        return result
    }

    private fun toName(records: Array<String>): StringLang {
        return StringLang(records[Constants.Stop.TRAM_STOP_RECORD_NAME_TC], records[Constants.Stop.TRAM_STOP_RECORD_NAME_EN], records[Constants.Stop.TRAM_STOP_RECORD_NAME_SC])
    }

    private fun toParentRoute(routeNo: String,
                              from: StringLang,
                              to: StringLang,
                              displaySeq: Long,
                              t: Long): Route {
        return Route(
            routeKey = RouteKey(company = Company.TRAM,
                routeNo = routeNo,
                bound = 0L,
                variant = 0L),
            direction = 1L,
            specialCode = 0L,
            companyDetails = listOf(Company.TRAM),
            from = from,
            to = to,
            info = Info(startSeq = routeNo[0].toString().toLong(), endSeq = routeNo[1].toString().toLong()),
            displaySeq = displaySeq,
            updateTime = t
        )
    }

    private fun toChildRoute(parentRoute: Route,
                             bound: Long,
                             t: Long): Route
    {
        return Route(
            routeKey = parentRoute.routeKey.copy(bound = bound, variant = 1L),
            direction = parentRoute.childDirection,
            specialCode = parentRoute.specialCode,
            companyDetails = parentRoute.companyDetails,
            from = parentRoute.from,
            to = parentRoute.to,
            info = parentRoute.info,
            displaySeq = parentRoute.displaySeq,
            typeSeq = parentRoute.typeSeq,
            updateTime = t
        )
    }

    private fun toStop(route: Route, seq: Long, records: Array<String>, t: Long): Stop {
        return Stop(
            routeKey = route.routeKey.copy(),
            seq = seq,
            name = toName(records),
            to = route.to,
            details = StringLang.newInstance(records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID]),
            latitude = records[Constants.Stop.TRAM_STOP_RECORD_LATITUDE].toDouble(),
            longitude = records[Constants.Stop.TRAM_STOP_RECORD_LONGITUDE].toDouble(),
            fare = 2.6,
            info = Info(stopId = records[Constants.Stop.TRAM_STOP_RECORD_STOP_ID]),
            updateTime = t
        )
    }

    override fun getParentRoute(routeKey: RouteKey): Route? {
        return null
    }

    override fun getChildRoutes(parentRoute: Route) {
        return
    }

    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        val prefix = "[${route.routeKey}]"

        if (needEtaUpdate) {
            val stops = AppHelper.db.stopDao().selectOnce(
                route.routeKey.company,
                route.routeKey.routeNo,
                route.routeKey.bound,
                route.routeKey.variant)
            updateEta(stops)
        }

        try {
            val response = tram.getStops(
                route = route.routeKey.company,
                bound = route.info.startSeq.toString(),
                serviceType = route.info.endSeq.toString()).execute()

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

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    override fun getTimetableUrl(route: Route): String? {
        return tram.getTimetable().request().url().toString()
    }

    override fun getTimetable(route: Route): String? {
        return null
    }

    override fun updateEta(stop: Stop) {
        return
    }

    @AddTrace(name = "TramConnection_updateEta")
    override fun updateEta(stops: List<Stop>) {
        val t = Utils.getCurrentTimestamp()

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                stops.forEach { stop ->
                    jobs += launch(Dispatchers.Default) {
                        stop.etaStatus = Constants.EtaStatus.FAILED
                        stop.etaUpdateTime = t
                        try {
                            val response =
                                tramEta.getEta(stop.info.stopId).execute()

                            if (response.isSuccessful) {
                                val responseStr = response.body()?.string()
                                val etaResults = mutableListOf<EtaResult>()
                                val tramResponse = Jsoup.parse(responseStr).body().getElementsByTag("metadata")

                                tramResponse.forEach {
                                    etaResults.add(toEtaResult(stop, it))
                                    //logd(it)
                                }

                                if (responseStr.isNullOrBlank() || tramResponse.isEmpty()) {
                                    etaResults.add(toEtaResult(stop, AppHelper.getString(R.string.eta_msg_no_eta_info)))
                                }

                                if (etaResults.isNotEmpty()) {
                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                    stop.etaResults = etaResults
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

    private fun toEtaResult(stop: Stop, element: Element): EtaResult {
        val etaTime = Utils.dateStrToTimestamp(element.attr(Constants.Eta.TRAM_ETA_RECORD_ETA_TIME), "MMM d yyyy h:mma")
        val msg = Utils.timestampToTimeStr(etaTime) + " " + AppHelper.getString(R.string.to) + Utils.phaseFromTo(element.attr(Constants.Eta.TRAM_ETA_RECORD_DEST_TC))

        return EtaResult(
            company = stop.routeKey.company,
            etaTime = etaTime,
            msg = Utils.timeStrToMsg(msg),
            scheduleOnly = false,
            gps = true,
            wifi = element.attr(Constants.Eta.TRAM_ETA_RECORD_DEST_CODE).startsWith("HVT")   // changed to store if dest is HVT
        )
    }
}