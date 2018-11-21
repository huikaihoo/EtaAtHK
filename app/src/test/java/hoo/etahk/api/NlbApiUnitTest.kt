package hoo.etahk.api

import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.remote.request.NlbEtaReq
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NlbApiUnitTest {

    private val routeId = "1" // 1 88
    private val stopId = "37" // 28 105

    @Test
    fun getParentRoutes() {
        val call = ConnectionHelper.nlb.getDatabase()
        System.out.println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            System.out.println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                System.out.println("result = $result")

                System.out.println("routeStops.size = ${result?.routeStops?.size}")
                System.out.println("routes.size = ${result?.routes?.size}")
                System.out.println("specialRoutes.size = ${result?.specialRoutes?.size}")
                System.out.println("stopDistricts.size = ${result?.stopDistricts?.size}")
                System.out.println("stops.size = ${result?.stops?.size}")

                assert(result != null && !result.version.isNullOrEmpty())
            } else {
                System.out.println("error = ${response.errorBody()?.string()}")
                assert(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }
    }

    @Test
    fun getEta() {
        val call = ConnectionHelper.nlb.getEta(NlbEtaReq(routeId = routeId, stopId = stopId))
        System.out.println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            System.out.println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                System.out.println("result = $result")

                val x = Jsoup.parse(result!!.estimatedArrivalTime!!.html!!).body().getElementsByTag("div")
                for (xe in x) {
                    System.out.println("xe = ${xe.text()}")
                }

                assert(!result.estimatedArrivalTime?.html.isNullOrEmpty())
            } else {
                System.out.println("error = ${response.errorBody()?.string()}")
                assert(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }
    }
}