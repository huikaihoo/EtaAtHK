package hoo.etahk.connection

import hoo.etahk.BaseUnitTest
import hoo.etahk.common.Constants
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.remote.connection.NlbConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NlbConnectionUnitTest: BaseUnitTest() {

    override val printLog = false

    private val nlbConnection: NlbConnection by inject()

    private val routeNo = "B4"
    private val bound = 1L
    private val variant = 1L

    @Test
    fun getParentRoutes() {
        val result = nlbConnection.getParentRoutes(Constants.Company.NLB)?.getAll()?.sortedBy { it.routeKey.routeNo }
        result?.forEach {
            println("route = $it")
        }

        // Check Parent Routes
        val parentRouteCount = result?.size ?: 0
        println("parentRouteCount = $parentRouteCount")

        assert(parentRouteCount > 0 && result != null && result.isNotEmpty())

        // Check Child Routes
        val childRouteCount = AppHelper.db.childRouteDao().count()
        val childRoute = AppHelper.db.childRouteDao().selectOnce(Constants.Company.NLB, routeNo)
        println("childRouteCount = $childRouteCount")
        println(gson.toJson(childRoute))

        assert(childRouteCount > 0 && childRoute.isNotEmpty())

        // Check Stops
        val stopCount = AppHelper.db.stopDao().count()
        val stopList = AppHelper.db.stopDao().selectOnce(Constants.Company.NLB, routeNo, bound, variant)

        println("stopCount = $stopCount")
        println(gson.toJson(stopList))

        assert(stopCount > 0 && stopList.isNotEmpty())
    }
}