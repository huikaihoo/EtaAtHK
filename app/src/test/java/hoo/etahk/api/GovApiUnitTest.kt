package hoo.etahk.api

import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.remote.connection.GovConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GovApiUnitTest {
    @Test
    fun getParentRoutes() {
        val call = ConnectionHelper.gov.getParentRoutes(syscode = GovConnection.getSystemCode())
        System.out.println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            System.out.println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()?.string()
                System.out.println("result = $result")
                assert(!result.isNullOrBlank())
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