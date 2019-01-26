package hoo.etahk.common.helper

import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.NetworkType
import hoo.etahk.common.Constants.Url
import hoo.etahk.common.tools.ConnectionFactory
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.remote.api.*
import hoo.etahk.remote.connection.*
import okhttp3.OkHttpClient
import retrofit2.create

object ConnectionHelper: BaseConnection {
    private lateinit var okHttp: OkHttpClient
    private lateinit var okHttpLong: OkHttpClient
    private lateinit var okHttpEtaKmb: OkHttpClient
    private lateinit var okHttpEtaNwfb: OkHttpClient
    private lateinit var okHttpEtaNlb: OkHttpClient

    // KMB
    lateinit var kmb: KmbApi private set
    lateinit var kmbEta: KmbApi private set
    // NWFB
    lateinit var nwfb: NwfbApi private set
    lateinit var nwfbEta: NwfbApi private set
    // NLB
    lateinit var nlb: NlbApi private set
    lateinit var nlbEta: NlbApi private set
    // GOV
    lateinit var gov: GovApi private set
    // GIST
    lateinit var gist: GistApi private set

    fun init() {
        okHttp = ConnectionFactory.createClient(NetworkType.DEFAULT, "")
        okHttpLong = ConnectionFactory.createClient(NetworkType.LONG, "")
        okHttpEtaKmb = ConnectionFactory.createClient(NetworkType.ETA, Company.KMB)
        okHttpEtaNwfb = ConnectionFactory.createClient(NetworkType.ETA, Company.NWFB)
        okHttpEtaNlb = ConnectionFactory.createClient(NetworkType.ETA, Company.KMB)

        kmb = ConnectionFactory.createRetrofit(okHttpLong, Url.KMB_URL).create()
        kmbEta = ConnectionFactory.createRetrofit(okHttpEtaKmb, Url.KMB_URL).create()

        nwfb = ConnectionFactory.createRetrofit(okHttpLong, Url.NWFB_URL).create()
        nwfbEta = ConnectionFactory.createRetrofit(okHttpEtaNwfb, Url.NWFB_URL).create()

        nlb = ConnectionFactory.createRetrofit(okHttpLong, Url.NLB_URL).create()
        nlbEta = ConnectionFactory.createRetrofit(okHttpEtaNlb, Url.NLB_URL).create()

        gov = ConnectionFactory.createRetrofit(okHttpLong, Url.GOV_URL).create()

        gist = ConnectionFactory.createRetrofit(okHttpLong, Url.GIST_URL).create()
    }

    private fun getConnection(company: String): BaseConnection? {
        return when (company) {
            Company.BUS -> BusConnection
            Company.GOV -> GovConnection
            Company.KMB -> KmbConnection
            Company.LWB -> KmbConnection
            Company.NWFB -> NwfbConnection
            Company.CTB -> NwfbConnection
            Company.NLB -> NlbConnection
            else -> null
        }
    }

    override fun getEtaRoutes(company: String): List<String>? {
        return getConnection(company)?.getEtaRoutes()
    }

    override fun getParentRoutes(company: String): ParentRoutesMap? {
        return getConnection(company)?.getParentRoutes()
    }

    override fun getParentRoute(routeKey: RouteKey): Route? {
        return getConnection(routeKey.company)?.getParentRoute(routeKey)
    }

    override fun getChildRoutes(parentRoute: Route) {
        getConnection(parentRoute.routeKey.company)?.getChildRoutes(parentRoute)
    }

    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        getConnection(route.routeKey.company)?.getStops(route, needEtaUpdate)
    }

    override fun getTimetableUrl(route: Route): String? {
        return getConnection(route.routeKey.company)?.getTimetableUrl(route)
    }

    override fun getTimetable(route: Route): String? {
        return getConnection(route.routeKey.company)?.getTimetable(route)
    }
    
    override fun updateEta(stop: Stop) {
        getConnection(stop.routeKey.company)?.updateEta(stop)
    }

    override fun updateEta(stops: List<Stop>) {
        if (stops.isNotEmpty())
            getConnection(stops[0].routeKey.company)?.updateEta(stops)
    }
}