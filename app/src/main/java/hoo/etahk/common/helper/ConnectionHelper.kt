package hoo.etahk.common.helper

import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.NetworkType
import hoo.etahk.common.Constants.Url
import hoo.etahk.common.tools.ConnectionFactory
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.remote.api.GistApi
import hoo.etahk.remote.api.GovApi
import hoo.etahk.remote.api.KmbApi
import hoo.etahk.remote.api.MtrbApi
import hoo.etahk.remote.api.NlbApi
import hoo.etahk.remote.api.NwfbApi
import hoo.etahk.remote.api.NwfbV2Api
import hoo.etahk.remote.api.TramApi
import hoo.etahk.remote.connection.BaseConnection
import hoo.etahk.remote.connection.BusConnection
import hoo.etahk.remote.connection.GovConnection
import hoo.etahk.remote.connection.KmbConnection
import hoo.etahk.remote.connection.KmbV2Connection
import hoo.etahk.remote.connection.MtrbConnection
import hoo.etahk.remote.connection.NlbConnection
import hoo.etahk.remote.connection.NlbV2Connection
import hoo.etahk.remote.connection.NwfbConnection
import hoo.etahk.remote.connection.TramConnection
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.create

object ConnectionHelper: BaseConnection, KoinComponent {

    val clientModule = module {
        single(named("okHttpLong")){ ConnectionFactory.createClient(NetworkType.LONG, "") }
        single(named("okHttpEtaKmb")){ ConnectionFactory.createClient(NetworkType.ETA, Company.KMB) }
        single(named("okHttpEtaNwfb")){ ConnectionFactory.createClient(NetworkType.ETA, Company.NWFB) }
        single(named("okHttpEtaNlb")){ ConnectionFactory.createClient(NetworkType.ETA, Company.KMB) }
        single(named("okHttpEtaMtrb")){ ConnectionFactory.createClient(NetworkType.ETA, Company.KMB) }
        single(named("okHttpEtaTram")){ ConnectionFactory.createClient(NetworkType.ETA, Company.KMB) }
    }

    val gistModule = module {
        // API
        single<GistApi> {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.GIST_URL).create()
        }
    }

    val busModule = module {
        // Connection
        single { BusConnection() }
    }

    val govModule = module {
        // API
        single<GovApi> {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.GOV_URL).create()
        }

        // Connection
        single { GovConnection(get()) }
    }

    val kmbModule = module {
        // API
        single<KmbApi>(named("kmbApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.KMB_URL).create()
        }
        single<KmbApi>(named("kmbEtaApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpEtaKmb")), Url.KMB_URL).create()
        }

        // Connection
        single { KmbConnection(get(named("kmbApi")), get(named("kmbEtaApi")), get()) }
    }

    val kmbV2Module = module {
        // API
        single<KmbApi>(named("kmbApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.KMB_URL).create()
        }
        single<KmbApi>(named("kmbEtaApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpEtaKmb")), Url.KMB_URL).create()
        }

        // Connection
        single<KmbConnection> { KmbV2Connection(get(named("kmbApi")), get(named("kmbEtaApi")), get()) }
    }

    val nwfbModule = module {
        // API
        single<NwfbApi>(named("nwfbApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.NWFB_URL).create()
        }
        single<NwfbApi>(named("nwfbEtaApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpEtaNwfb")), Url.NWFB_URL).create()
        }

        // Connection
        single { NwfbConnection(get(named("nwfbApi")), get(named("nwfbEtaApi")), get()) }
    }

    val nwfbV2Module = module {
        // API
        single<NwfbV2Api>(named("nwfbV2Api")) {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.NWFB_URL).create()
        }
        single<NwfbV2Api>(named("nwfbV2EtaApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpEtaNwfb")), Url.NWFB_URL).create()
        }

        // Connection
        single { NwfbConnection(get(named("nwfbV2Api")), get(named("nwfbV2EtaApi")), get()) }
    }

    val nlbModule = module {
        // API
        single<NlbApi>(named("nlbApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.NLB_URL).create()
        }
        single<NlbApi>(named("nlbEtaApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpEtaNlb")), Url.NLB_URL).create()
        }

        // Connection
        single { NlbConnection(get(named("nlbApi")), get(named("nlbEtaApi"))) }
    }

    val nlbV2Module = module {
        // API
        single<NlbApi>(named("nlbApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.NLB_URL).create()
        }
        single<NlbApi>(named("nlbEtaApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpEtaNlb")), Url.NLB_URL).create()
        }

        // Connection
        single<NlbConnection> { NlbV2Connection(get(named("nlbApi")), get(named("nlbEtaApi"))) }
    }


    val mtrbModule = module {
        // API
        single<MtrbApi> {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.MTRB_URL).create()
        }

        // Connection
        single { MtrbConnection(get(), get(), get()) }
    }

    val tramModule = module {
        // API
        single<TramApi>(named("tramApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpLong")), Url.TRAM_URL).create()
        }
        single<TramApi>(named("tramEtaApi")) {
            ConnectionFactory.createRetrofit(get(named("okHttpEtaTram")), Url.TRAM_URL).create()
        }

        // Connection
        single { TramConnection(get(named("tramApi")), get(named("tramEtaApi")), get()) }
    }

    val modules = listOf(
        clientModule, gistModule, busModule,
        govModule, kmbV2Module  , nwfbModule,
        nlbV2Module, mtrbModule, tramModule
    )

    private val busConnection: BusConnection by inject()
    private val govConnection: GovConnection by inject()
    private val kmbConnection: KmbConnection by inject()
    private val nwfbConnection: NwfbConnection by inject()
    private val nlbConnection: NlbConnection by inject()
    private val mtrbConnection: MtrbConnection by inject()
    private val tramConnection: TramConnection by inject()

    private val connectionMap: HashMap<String, BaseConnection> by lazy {
        hashMapOf(
            Company.BUS to busConnection,
            Company.GOV to govConnection,
            Company.KMB to kmbConnection,
            Company.LWB to kmbConnection,
            Company.NWFB to nwfbConnection,
            Company.CTB to nwfbConnection,
            Company.NLB to nlbConnection,
            Company.MTRB to mtrbConnection,
            Company.TRAM to tramConnection
        )
    }

    private fun getConnection(company: String): BaseConnection? {
        return connectionMap[company]
    }

    override fun getEtaRoutes(company: String): List<String>? {
        return getConnection(company)?.getEtaRoutes()
    }

    override fun getParentRoutes(company: String): ParentRoutesMap? {
        return getConnection(company)?.getParentRoutes(company)
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