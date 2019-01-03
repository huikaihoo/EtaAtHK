package hoo.etahk.connection

import hoo.etahk.BaseUnitTest
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.remote.connection.KmbConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class KmbConnectionUnitTest: BaseUnitTest() {

    override val printLog = false

    private val gistId = getStringFromResource(R.string.param_gist_id_kmb)

    private val routeNo = "224R"
    private val bound = 1L
    private val variant = 1L

    @Test
    fun getParentRoutes() {
        SharedPrefsHelper.put(R.string.param_gist_id_kmb, gistId)

        val result = KmbConnection.getParentRoutes(Constants.Company.KMB)?.getAll()?.sortedBy { it.routeKey.routeNo }
        result?.forEach {
            System.out.println("route = $it")
        }

        // Check Parent Routes
        val parentRouteCount = result?.size ?: 0
        System.out.println("parentRouteCount = $parentRouteCount")

        assert(parentRouteCount > 0 && result != null && result.isNotEmpty())

        // Check Child Routes
        val childRouteCount = AppHelper.db.childRouteDao().count()
        val childRoute = AppHelper.db.childRouteDao().selectOnce(Constants.Company.KMB, routeNo)
        System.out.println("childRouteCount = $childRouteCount")
        System.out.println(gson.toJson(childRoute))

        assert(childRouteCount > 0 && childRoute.isNotEmpty())

        // Check Stops
        val stopCount = AppHelper.db.stopDao().count()
        val stopList = AppHelper.db.stopDao().selectOnce(Constants.Company.KMB, routeNo, bound, variant)

        System.out.println("stopCount = $stopCount")
        System.out.println(gson.toJson(stopList))

        assert(stopCount > 0 && stopList.isNotEmpty())
    }
}