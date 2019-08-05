package hoo.etahk.connection

import hoo.etahk.BaseUnitTest
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.remote.connection.MtrbConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MtrbConnectionUnitTest: BaseUnitTest() {

    override val printLog = false

    private val gistId = getStringFromResource(R.string.param_gist_id_mtrb)

    private val routeNo = "K12"
    private val bound = 1L
    private val variant = 1L

    @Test
    fun getParentRoutes() {
        SharedPrefs.gistIdMtrb = gistId

        val result = MtrbConnection.getParentRoutes(Constants.Company.MTRB)?.getAll()?.sortedBy { it.routeKey.routeNo }
        result?.forEach {
            println("route = $it")
        }

        // Check Parent Routes
        val parentRouteCount = result?.size ?: 0
        println("parentRouteCount = $parentRouteCount")

        assert(parentRouteCount > 0 && result != null && result.isNotEmpty())

        // Check Child Routes
        val childRouteCount = AppHelper.db.childRouteDao().count()
        val childRoute = AppHelper.db.childRouteDao().selectOnce(Constants.Company.MTRB, routeNo)
        println("childRouteCount = $childRouteCount")
        println(gson.toJson(childRoute))

        assert(childRouteCount > 0 && childRoute.isNotEmpty())

        // Check Stops
        val stopCount = AppHelper.db.stopDao().count()
        val stopList = AppHelper.db.stopDao().selectOnce(Constants.Company.MTRB, routeNo, bound, variant)

        println("stopCount = $stopCount")
        println(gson.toJson(stopList))

        assert(stopCount > 0 && stopList.isNotEmpty())
    }
}