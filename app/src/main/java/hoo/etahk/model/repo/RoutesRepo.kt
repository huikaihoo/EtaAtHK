package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.json.StringLang

object RoutesRepo {

    private val TAG = "RoutesRepo"

    // Parents routes by Company
    fun getParentRoutes(company: String): LiveData<List<Route>> {
        return AppHelper.db.routesDao().selectParent(company)
    }

    // Parents route by Company and routeNo
    fun getParentRoute(company: String, routeNo: String ): LiveData<Route> {
        return AppHelper.db.routesDao().selectParent(company, routeNo)
    }

    // Testing only
    fun insertRoute() {
        val route1 = Route(//id = 1,
                routeKey = RouteKey("KMB", "5M", 0L, 0L),
                direction = 0,
                companyDetails = listOf("KMB"),
                from = StringLang("A", ""),
                to = StringLang("B", ""))

        AppHelper.db.routesDao().insert(route1)
    }
}