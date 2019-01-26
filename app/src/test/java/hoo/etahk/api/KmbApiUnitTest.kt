package hoo.etahk.api

import hoo.etahk.common.helper.ConnectionHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class KmbApiUnitTest {

    private val route = "101"
    private val bound = "1"

    @Test
    fun getRouteBound() {
        val call = ConnectionHelper.kmb.getRouteBound(route = route)
        System.out.println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            System.out.println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                System.out.println("result = $result")
                assert(result != null && result.result == true && !result.data.isNullOrEmpty())
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
    fun getTimetable() {
        val call = ConnectionHelper.kmb.getTimetable(
            route = route,
            bound = bound)
        System.out.println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            System.out.println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                System.out.println("result = $result")
                assert(result != null && result.result == true && !result.data.isNullOrEmpty())
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