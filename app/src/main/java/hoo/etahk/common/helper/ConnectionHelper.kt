package hoo.etahk.common.helper

import hoo.etahk.common.Constants.Company
import hoo.etahk.model.data.Stop
import hoo.etahk.remote.api.KmbApi
import hoo.etahk.remote.api.NwfbApi
import hoo.etahk.remote.connection.BaseConnection
import hoo.etahk.remote.connection.KmbConnection
import hoo.etahk.remote.connection.NwfbConnection
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ConnectionHelper: BaseConnection {

    lateinit var kmbEta: KmbApi private set
    lateinit var nwfb: NwfbApi private set

    fun init() {
        kmbEta = Retrofit.Builder()
                .client(AppHelper.okHttp)
                .baseUrl("http://etav3.kmb.hk/")
                .addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
                .create(KmbApi::class.java)

        nwfb = Retrofit.Builder()
                .client(AppHelper.okHttp)
                .baseUrl("http://mobile.nwstbus.com.hk/")
                //.addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
                .create(NwfbApi::class.java)
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

    override fun updateEta(stop: Stop) {
        getConnection(stop.routeKey.company)?.updateEta(stop)
    }
}