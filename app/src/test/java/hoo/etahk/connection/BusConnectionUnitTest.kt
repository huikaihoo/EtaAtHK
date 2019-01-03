package hoo.etahk.connection

import hoo.etahk.BaseUnitTest
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.remote.connection.BusConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BusConnectionUnitTest: BaseUnitTest() {

    override val printLog = false

    private val gistId = getStringFromResource(R.string.param_gist_id_kmb)

    @Test
    fun getParentRoutes() {
        SharedPrefsHelper.put(R.string.param_gist_id_kmb, gistId)

        val result = BusConnection.getParentRoutes(Constants.Company.BUS)?.getAll()?.sortedWith(compareBy({it.routeKey.typeCode}, {it.displaySeq}, {it.routeKey.variant}))
        result?.forEach {
            System.out.println("route = $it")
        }

        // Check Parent Routes
        val parentRouteCount = result?.size ?: 0
        System.out.println("parentRouteCount = $parentRouteCount")

        assert(parentRouteCount > 0 && result != null && result.isNotEmpty())
    }
}