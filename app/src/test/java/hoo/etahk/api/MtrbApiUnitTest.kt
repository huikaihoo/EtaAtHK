package hoo.etahk.api

import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.remote.connection.MtrbConnection
import hoo.etahk.remote.request.MtrbEtaReq
import hoo.etahk.remote.request.MtrbEtaRoutesReq
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MtrbApiUnitTest {

    private val route = "K52"

    @Test
    fun getEtaRoutes() {
        val call = ConnectionHelper.mtrb.getEtaRoutes(MtrbEtaRoutesReq(key = MtrbConnection.getKey()))
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(result != null && !result.routeStatus.isNullOrEmpty())
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
        val call = ConnectionHelper.mtrb.getEta(MtrbEtaReq(key = MtrbConnection.getKey(), routeName = route))
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(result != null && result.status == "0")
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
    fun getEtaRaw() {
        val call = ConnectionHelper.mtrb.getEtaRaw(MtrbEtaReq(language = "en", key = MtrbConnection.getKey(), routeName = route))
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()?.string()
                println("result = $result")
                assert(!result.isNullOrBlank())
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