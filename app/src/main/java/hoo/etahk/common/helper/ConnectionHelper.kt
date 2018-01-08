package hoo.etahk.common.helper

import hoo.etahk.common.Constants
import hoo.etahk.common.Constants.Company
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.remote.api.GovApi
import hoo.etahk.remote.api.KmbApi
import hoo.etahk.remote.api.NwfbApi
import hoo.etahk.remote.connection.BaseConnection
import hoo.etahk.remote.connection.GovConnection
import hoo.etahk.remote.connection.KmbConnection
import hoo.etahk.remote.connection.NwfbConnection
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ConnectionHelper: BaseConnection {
    lateinit var kmb: KmbApi private set
    lateinit var kmbEta: KmbApi private set
    lateinit var kmbEtaFeed: KmbApi private set
    lateinit var nwfb: NwfbApi private set
    lateinit var gov: GovApi private set

    fun init() {
        kmb = Retrofit.Builder()
                .client(AppHelper.okHttp)
                .baseUrl(Constants.Url.KMB_URL)
                .addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
                .create(KmbApi::class.java)

        kmbEta = Retrofit.Builder()
                .client(AppHelper.okHttp)
                .baseUrl(Constants.Url.KMB_ETA_URL)
                .addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
                .create(KmbApi::class.java)

        kmbEtaFeed = Retrofit.Builder()
                .client(AppHelper.okHttp)
                .baseUrl(Constants.Url.KMB_ETA_FEED_URL)
                .addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
                .create(KmbApi::class.java)

        nwfb = Retrofit.Builder()
                .client(AppHelper.okHttp)
                .baseUrl(Constants.Url.NWFB_URL)
                //.addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
                .create(NwfbApi::class.java)

        gov = Retrofit.Builder()
                .client(AppHelper.okHttp)
                .baseUrl(Constants.Url.GOV_URL)
                //.addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
                .create(GovApi::class.java)
    }

    private fun getConnection(company: String): BaseConnection? {
        return when (company) {
            Company.KMB -> KmbConnection
            Company.LWB -> KmbConnection
            Company.NWFB -> NwfbConnection
            Company.CTB -> NwfbConnection
            else -> null
        }
    }

    override fun getParentRoutes() {
        GovConnection.getParentRoutes()
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
}