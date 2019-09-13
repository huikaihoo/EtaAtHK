package hoo.etahk.api

import hoo.etahk.BaseUnitTest
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.remote.api.NwfbApi
import hoo.etahk.remote.connection.NwfbConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NwfbApiUnitTest: BaseUnitTest() {

    override val printLog = false

    private val nwfb: NwfbApi by inject(named("nwfbApi"))
    private val nwfbConnection: NwfbConnection by inject()

    @Test
    fun getParentRoutes() {
        val call = nwfb.getParentRoutes(
            m = SharedPrefs.NWFB_API_PARAMETER_TYPE_ALL_BUS,
            syscode = nwfbConnection.getSystemCode()
        )
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