package hoo.etahk.api

import hoo.etahk.BaseUnitTest
import hoo.etahk.remote.api.KmbApi
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class KmbApiUnitTest: BaseUnitTest() {

    override val printLog = false

    private val kmb: KmbApi by inject(named("kmbApi"))

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