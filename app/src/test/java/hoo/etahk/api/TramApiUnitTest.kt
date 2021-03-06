package hoo.etahk.api

import hoo.etahk.BaseUnitTest
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.remote.api.TramApi
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TramApiUnitTest: BaseUnitTest() {

    override val printLog = false

    private val tram: TramApi by inject(named("tramApi"))

    private val stopId = "93E"

    @Test
    fun getParentRoutes() {
        val call = tram.getDatabase()
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

    @Test
    fun getEta() {
        val call = tram.getEta(stopId)
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()?.string()
                println("result = $result")

                val x = Jsoup.parse(result!!).body().getElementsByTag("metadata")
                for (xe in x) {
                    println("xe = ${xe.attributes()}")

                    val etaTime = Utils.dateStrToTimestamp(xe.attr(Constants.Eta.TRAM_ETA_RECORD_ETA_TIME), "MMM d yyyy  h:mma")
                    val msg = Utils.timestampToTimeStr(etaTime) + " " + AppHelper.getString(R.string.to) + xe.attr(Constants.Eta.TRAM_ETA_RECORD_DEST_TC)
                    println(msg)
                }

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