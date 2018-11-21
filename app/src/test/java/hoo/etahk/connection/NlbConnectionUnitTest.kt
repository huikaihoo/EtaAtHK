package hoo.etahk.connection

import hoo.etahk.common.Constants
import hoo.etahk.remote.connection.NlbConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NlbConnectionUnitTest {

    private val routeId = "1"
    private val stopId = "28"

    @Test
    fun getParentRoutes() {
        val result = NlbConnection.getParentRoutes(Constants.Company.NLB)
        result?.forEach { routeId, route ->
            System.out.println("routeId = $routeId route=$route")
        }
    }
}