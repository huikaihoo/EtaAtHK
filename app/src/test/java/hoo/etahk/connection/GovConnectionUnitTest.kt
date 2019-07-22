package hoo.etahk.connection

import hoo.etahk.BaseUnitTest
import hoo.etahk.common.Constants
import hoo.etahk.remote.connection.GovConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GovConnectionUnitTest: BaseUnitTest() {

    override val printLog = false

    @Test
    fun getParentRoutes() {
        val result = GovConnection.getParentRoutes(Constants.Company.GOV)?.getAll()?.sortedBy { it.routeKey.routeNo }
        result?.forEach {
            println("route = $it")
        }

        // Check Parent Routes
        val parentRouteCount = result?.size ?: 0
        println("parentRouteCount = $parentRouteCount")

        assert(parentRouteCount > 0 && result != null && result.isNotEmpty())
    }
}