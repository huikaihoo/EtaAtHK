package hoo.etahk.remote.connection

import com.google.android.gms.maps.model.LatLng
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Path
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import hoo.etahk.remote.response.KmbBoundVariantRes
import hoo.etahk.remote.response.KmbEtaRes
import hoo.etahk.remote.response.KmbStopsRes
import hoo.etahk.view.App
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object KmbConnection: BaseConnection {

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    override fun getParentRoutes(company: String): HashMap<String, Route>? {
        return null
    }

    /*****************************
     * Get Parent Route (Single) *
     *****************************/
    override fun getParentRoute(routeKey: RouteKey): Route? {
        return null
    }

    /********************
     * Get Child Routes *
     ********************/
    override fun getChildRoutes(parentRoute: Route) {
        for (bound in 1..parentRoute.boundCount) {
            ConnectionHelper.kmb.getBoundVariant(
                    route = parentRoute.routeKey.routeNo,
                    bound = bound.toString())
                    .enqueue(object : Callback<KmbBoundVariantRes> {
                        override fun onFailure(call: Call<KmbBoundVariantRes>, t: Throwable) {}
                        override fun onResponse(call: Call<KmbBoundVariantRes>, response: Response<KmbBoundVariantRes>) {
                            launch(CommonPool) {
                                val t = Utils.getCurrentTimestamp()
                                val kmbBoundVariantRes = response.body()
                                //logd(kmbBoundVariantRes.toString())

                                if (kmbBoundVariantRes?.data?.routes != null && (kmbBoundVariantRes.data.routes).isNotEmpty()) {
                                    val routes = mutableListOf<Route>()
                                    (kmbBoundVariantRes.data.routes).forEach {
                                        //logd("${it?.bound} == $bound")
                                        assert(it!!.bound!! == bound)
                                        routes.add(toChildRoute(parentRoute, it, t))
                                    }

                                    //logd(AppHelper.gson.toJson(routes))
                                    AppHelper.db.childRouteDao().insertOrUpdate(routes, t)
                                }
                            }
                        }
                    })
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

    /***********************
     * Get Paths and Stops *
     ***********************/
    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        ConnectionHelper.kmbStop.getStops(
                route = route.routeKey.routeNo,
                bound = route.routeKey.bound.toString(),
                serviceType = route.routeKey.variant.toString())
                .enqueue(object : Callback<KmbStopsRes> {
                    override fun onFailure(call: Call<KmbStopsRes>, t: Throwable) {}
                    override fun onResponse(call: Call<KmbStopsRes>, response: Response<KmbStopsRes>) {
                        launch(CommonPool) {
                            val t = Utils.getCurrentTimestamp()
                            val kmbStopsRes = response.body()
                            //logd(kmbStopsRes.toString())

                            // Add Paths to database
                            if (kmbStopsRes?.data?.route?.lineGeometry != null && (kmbStopsRes.data.route.lineGeometry).isNotEmpty()) {
                                val paths = mutableListOf<Path>()

                                val strPaths = kmbStopsRes.data.route.lineGeometry.replace("{paths:", "").replace("}", "")
                                val arrPaths = AppHelper.gson.fromJson(strPaths, Array<Array<DoubleArray>>::class.java)

                                var seq = 0L
                                arrPaths.forEachIndexed { section, arrPaths2 ->
                                    arrPaths2.forEach { point ->
                                        if (point.size == 2) {
                                            val latLng = Utils.hk1980GridToLatLng(point[1], point[0])
                                            paths.add(toPath(route, latLng, seq++, section.toLong(), t))
                                        }
                                    }
                                }

                                AppHelper.db.pathDao().insertOrUpdate(route, paths, t)
                            }

                            // Add Stops to database
                            if (kmbStopsRes?.data?.routeStops != null && (kmbStopsRes.data.routeStops).isNotEmpty()) {
                                val stops = mutableListOf<Stop>()
                                (kmbStopsRes.data.routeStops).forEach {
                                    stops.add(toStop(route, it!!, t))
                                }

                                //logd(AppHelper.gson.toJson(stops))
                                AppHelper.db.stopDao().insertOrUpdate(route, stops, t)
                                if (needEtaUpdate)
                                    updateEta(stops)
                            }
                        }
                    }
                })
    }

    private fun toPath(route: Route, latLng: LatLng, seq: Long, section: Long, t: Long): Path {
        val path = Path(
            routeKey = route.routeKey.copy(),
            seq = seq,
            section = section,
            updateTime = t
        )
        path.location = latLng
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
            stop.location = Utils.hk1980GridToLatLng(routeStop.y, routeStop.x)
        return stop
    }

    /***************
     * Update ETA
     ***************/

    override fun updateEta(stops: List<Stop>) {
        val t = Utils.getCurrentTimestamp()

        try {
            runBlocking {
                val jobs = arrayListOf<Job>()

                stops.forEach { stop ->
                    jobs += launch(CommonPool) {
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

                                if (kmbEtaRes?.response != null && (kmbEtaRes.response).isNotEmpty()) {
                                    (kmbEtaRes.response).forEach {
                                        etaResults.add(toEtaResult(stop, it))
                                    }
                                    //logd(AppHelper.gson.toJson(etaResults))
                                } else {
                                    etaResults.add(toEtaResult(stop, App.instance.getString(R.string.eta_msg_no_eta_info)))
                                }

                                if (!etaResults.isEmpty()) {
                                    stop.etaStatus = Constants.EtaStatus.SUCCESS
                                    stop.etaResults = etaResults
                                    //logd(AppHelper.gson.toJson(stop.etaResults))
                                }

                                stop.etaStatus = Constants.EtaStatus.SUCCESS
                                stop.etaResults = etaResults
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

                        if (kmbEtaRes?.response != null && (kmbEtaRes.response).isNotEmpty()) {
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
                wifi = (response.wifi != null && response.wifi == "Y"),
                capacity = Utils.phaseCapacity(response.ol ?: "")
        )
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
}