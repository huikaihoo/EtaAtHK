package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.json.Info
import hoo.etahk.model.json.StringLang
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

object RoutesRepo {

    private const val TAG = "RoutesRepo"

    // Parents routes by type codes
    fun getParentRoutes(typeCodes: List<Long>, orderBy: Long): LiveData<List<Route>> {
        return AppHelper.db.parentRoutesDao().select(typeCodes, orderBy)
    }

    fun updateParentRoutes(company: String) {
        launch(CommonPool) {
            // TODO("Only get from remote when data outdated")
            ConnectionHelper.getParentRoutes(company)
        }
    }

    // Parents route by Company and routeNo
    fun getParentRoute(company: String, routeNo: String): LiveData<Route> {
        return AppHelper.db.parentRoutesDao().select(company, routeNo)
    }

    fun getChildRoutes(company: String, routeNo: String, bound: Long): LiveData<List<Route>> {
        return AppHelper.db.childRoutesDao().select(company, routeNo, bound)
    }

    fun updateChildRoutes(parentRoute: Route) {
        launch(CommonPool) {
            // TODO("Only get from remote when data outdated")
            ConnectionHelper.getChildRoutes(parentRoute)
        }
    }

    // Testing only
    fun insertRoute() {
        val route1 = Route(//id = 1,
                routeKey = RouteKey("CTB", "E23", 0L, 0L),
                direction = 2,
                companyDetails = listOf("CTB"),
                info = Info(boundIds = listOf("E23--Airport_(Ground_Transportation_Centre)", "E23--Tsz_Wan_Shan_(South)")),
                //info = Info(rdv="E23-TWS-1", bound="I", startSeq = 1L, endSeq = 31L),
                from = StringLang("A", ""),
                to = StringLang("B", ""))

        AppHelper.db.parentRoutesDao().insert(route1)
    }
}