package hoo.etahk.common.helper

import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.NetworkType
import hoo.etahk.common.Constants.Url
import hoo.etahk.common.tools.ConnectionFactory
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.remote.api.GovApi
import hoo.etahk.remote.api.KmbApi
import hoo.etahk.remote.api.NlbApi
import hoo.etahk.remote.api.NwfbApi
import hoo.etahk.remote.connection.*
import okhttp3.OkHttpClient

object ConnectionHelper: BaseConnection {
    private lateinit var okHttp: OkHttpClient
    private lateinit var okHttpStop: OkHttpClient
    private lateinit var okHttpEtaKmb: OkHttpClient
    private lateinit var okHttpEtaNwfb: OkHttpClient
    private lateinit var okHttpEtaNlb: OkHttpClient

    // KMB
    lateinit var kmb: KmbApi private set
    lateinit var kmbStop: KmbApi private set
    lateinit var kmbEta: KmbApi private set
    lateinit var kmbEtaFeed: KmbApi private set
    // NWFB
    lateinit var nwfb: NwfbApi private set
    lateinit var nwfbStop: NwfbApi private set
    lateinit var nwfbEta: NwfbApi private set
    // NLB
    lateinit var nlb: NlbApi private set
    lateinit var nlbEta: NlbApi private set
    // GOV
    lateinit var gov: GovApi private set
    lateinit var govStop: GovApi private set

    fun init() {
        okHttp = ConnectionFactory.createClient(NetworkType.DEFAULT, "")
        okHttpStop = ConnectionFactory.createClient(NetworkType.STOP, "")
        okHttpEtaKmb = ConnectionFactory.createClient(NetworkType.ETA, Company.KMB)
        okHttpEtaNwfb = ConnectionFactory.createClient(NetworkType.ETA, Company.NWFB)
        okHttpEtaNlb = ConnectionFactory.createClient(NetworkType.ETA, Company.KMB)

        kmb = ConnectionFactory.createRetrofit(okHttp, Url.KMB_URL)
                .create(KmbApi::class.java)

        kmbStop = ConnectionFactory.createRetrofit(okHttpStop, Url.KMB_URL)
                .create(KmbApi::class.java)

        kmbEta = ConnectionFactory.createRetrofit(okHttpEtaKmb, Url.KMB_ETA_URL)
                .create(KmbApi::class.java)

        kmbEtaFeed = ConnectionFactory.createRetrofit(okHttp, Url.KMB_ETA_FEED_URL)
                .create(KmbApi::class.java)

        nwfb = ConnectionFactory.createRetrofit(okHttp, Url.NWFB_URL)
                .create(NwfbApi::class.java)

        nwfbStop = ConnectionFactory.createRetrofit(okHttpStop, Url.NWFB_URL)
                .create(NwfbApi::class.java)

        nwfbEta = ConnectionFactory.createRetrofit(okHttpEtaNwfb, Url.NWFB_URL)
                .create(NwfbApi::class.java)

        nlb = ConnectionFactory.createRetrofit(okHttp, Url.NLB_URL)
                .create(NlbApi::class.java)

        nlbEta = ConnectionFactory.createRetrofit(okHttpEtaNlb, Url.NLB_URL)
                .create(NlbApi::class.java)

        gov = ConnectionFactory.createRetrofit(okHttp, Url.GOV_URL)
                .create(GovApi::class.java)

        govStop = ConnectionFactory.createRetrofit(okHttpStop, Url.GOV_URL)
                .create(GovApi::class.java)
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

    override fun getParentRoutes(company: String): HashMap<String, Route>? {
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

    override fun updateEta(stop: Stop) {
        getConnection(stop.routeKey.company)?.updateEta(stop)
    }

    override fun updateEta(stops: List<Stop>) {
        if (stops.isNotEmpty())
            getConnection(stops[0].routeKey.company)?.updateEta(stops)
    }

    fun updateItemsEta(items: List<ItemAndStop>) {

    }
}