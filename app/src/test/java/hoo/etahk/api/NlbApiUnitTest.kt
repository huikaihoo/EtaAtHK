package hoo.etahk.api

import hoo.etahk.BaseUnitTest
import hoo.etahk.remote.api.NlbApi
import hoo.etahk.remote.request.NlbEtaReq
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NlbApiUnitTest: BaseUnitTest() {

    override val printLog = false

    private val nlb: NlbApi by inject(named("nlbApi"))

    private val routeId = "88" // 1 88 3
    private val stopId = "78" // 28 105 127

    @Test
    fun getParentRoutes() {
        val call = nlb.getDatabase()
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")

                println("routeStops.size = ${result?.routeStops?.size}")
                println("routes.size = ${result?.routes?.size}")
                println("specialRoutes.size = ${result?.specialRoutes?.size}")
                println("stopDistricts.size = ${result?.stopDistricts?.size}")
                println("stops.size = ${result?.stops?.size}")

                assert(result != null && !result.version.isNullOrEmpty())
            } else {
                println("error = ${response.errorBody()?.string()}")
                assert(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }
    }

    @Test
    fun getEta() {
        val call = nlb.getEta(NlbEtaReq(routeId = routeId, stopId = stopId))
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")

                val x = Jsoup.parse(result!!.estimatedArrivalTime!!.html!!).body().getElementsByTag("div")
                for (xe in x) {
                    println("xe = ${xe.text()}")
                }

                assert(!result.estimatedArrivalTime?.html.isNullOrEmpty())
            } else {
                println("error = ${response.errorBody()?.string()}")
                assert(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }
    }

    @Test
    fun getEtaV2() {
        val call = nlb.getEtaV2(NlbEtaReq(routeId = routeId, stopId = stopId))
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(result != null && !(result.estimatedArrivals.isNullOrEmpty() && result.message.isNullOrBlank()))
            } else {
                println("error = ${response.errorBody()?.string()}")
                assert(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }
    }
}