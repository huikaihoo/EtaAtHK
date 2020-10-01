package hoo.etahk.api

import com.google.android.gms.common.util.Hex
import hoo.etahk.BaseUnitTest
import hoo.etahk.remote.api.KmbApi
import hoo.etahk.remote.connection.KmbConnection
import hoo.etahk.remote.request.KmbEtaV2Req
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigInteger
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class KmbApiUnitTest: BaseUnitTest() {

    override val printLog = false

    private val kmb: KmbApi by inject(named("kmbApi"))
    private val kmbConnection: KmbConnection by inject()

    private val route = "101"
    private val bound = "1"

    @Test
    fun getRouteBound() {
        val call = kmb.getRouteBound(route = route)
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(result != null && result.result == true && !result.data.isNullOrEmpty())
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
    fun getBoundVariant() {
        val call = kmb.getBoundVariant(route = route, bound = bound)
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(result != null && result.result == true && result.data != null)
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
        val kmbEtaReq = kmbConnection.getEtaReq("6C", "2", "1", "CA07S68000", "5")
        val call = kmb.getEta(kmbEtaReq.token, kmbEtaReq.t)
        println("url = ${call.request().url()}")
        println("token = ${kmbEtaReq.token}")
        println("t = ${kmbEtaReq.t}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(result != null && result.result == true && result.data != null)
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
        val kmbEtaReq = kmbConnection.getEtaV2Req("6C", "2", "1", "5")
        val call = kmb.getEtaV2(kmbEtaReq)
        println("url = ${call.request().url()}")
        println("d = ${kmbEtaReq.d}")
        println("ctr = ${kmbEtaReq.ctr}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(!result.isNullOrEmpty() && !result[0].eta.isNullOrEmpty())
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
    fun getTimetable() {
        val call = kmb.getTimetable(route = route, bound = bound)
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                println("result = $result")
                assert(result != null && result.result == true && !result.data.isNullOrEmpty())
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