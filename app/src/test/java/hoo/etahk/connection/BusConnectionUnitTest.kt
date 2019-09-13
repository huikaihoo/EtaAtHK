package hoo.etahk.connection

import hoo.etahk.BaseUnitTest
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.remote.connection.BusConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BusConnectionUnitTest: BaseUnitTest() {

    override val printLog = false

    private val busConnection: BusConnection by inject()

    private val gistIdMap = hashMapOf(
        R.string.param_gist_id_kmb to getStringFromResource(R.string.param_gist_id_kmb),
        R.string.param_gist_id_nwfb to getStringFromResource(R.string.param_gist_id_nwfb),
        R.string.param_gist_id_mtrb to getStringFromResource(R.string.param_gist_id_mtrb)
    )

    private val companies = listOf(
        Constants.Company.KMB, Constants.Company.LWB,
        Constants.Company.NWFB, Constants.Company.CTB,
        Constants.Company.NLB, Constants.Company.MTRB
    )

    @Test
    fun getParentRoutes() {
        gistIdMap.forEach { (resId, gistId) ->
            if (gistId.isNotEmpty())
                SharedPrefsHelper.put(resId, gistId)
        }

        val result = busConnection.getParentRoutes(Constants.Company.BUS)?.getAll()?.sortedWith(compareBy({it.routeKey.typeCode}, {it.displaySeq}, {it.routeKey.variant}))
        result?.forEach {
            println("route = $it")
        }

        // Check Parent Routes
        val parentRouteCount = result?.size ?: 0
        println("parentRouteCount = $parentRouteCount")

        assert(parentRouteCount > 0 && !result.isNullOrEmpty())

        val companyCountMap = result!!.groupingBy { it.routeKey.company }.eachCount()
        companyCountMap.forEach { (company, count) ->
            println("[$company] = $count")
        }

        companies.forEach {
            assert(companyCountMap.containsKey(it))
        }
    }
}