package hoo.etahk.api

import hoo.etahk.BaseUnitTest
import hoo.etahk.remote.api.GovApi
import hoo.etahk.remote.connection.GovConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GovApiUnitTest: BaseUnitTest() {

    override val printLog = false

    private val gov: GovApi by inject()
    private val govConnection: GovConnection by inject()

    @Test
    fun getParentRoutes() {
        val call = gov.getParentRoutes(syscode = govConnection.getSystemCode())
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