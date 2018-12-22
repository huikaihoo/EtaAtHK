package hoo.etahk.view.route

import androidx.lifecycle.LiveData
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.transfer.repo.MiscRepo
import hoo.etahk.transfer.repo.RoutesRepo
import hoo.etahk.view.base.TimerViewModel

class RouteViewModel: TimerViewModel() {
    private var parentRoute: LiveData<Route>? = null
    private var hasUpdateChildRoutes = false

    var selectedTabPosition: Int = 0

    var routeKey: RouteKey? = null
        set(value) {
            field = value
            if (value != null)
                subscribeToRepo()
        }
    var anotherCompany = ""

    var isGotoBoundUsed: Boolean = false
    var isGotoSeqUsed: Boolean = false

    fun getParentRoute(): LiveData<Route> {
        return parentRoute!!
    }

    fun updateChildRoutes(parentRoute: Route) {
        if (!hasUpdateChildRoutes) {
            hasUpdateChildRoutes = true
            RoutesRepo.updateChildRoutes(parentRoute)
        }
    }

    fun insertRouteFavourite() {
        getParentRoute().value?.let {
            val routeKey = RouteKey(
                company = it.companyDetails[0],
                routeNo = it.routeKey.routeNo,
                bound = 0L,
                variant = 0L
            )
            MiscRepo.insertRouteFavourite(routeKey)
        }
    }

    private fun subscribeToRepo() {
        parentRoute = RoutesRepo.getParentRoute(routeKey!!.company, routeKey!!.routeNo)
        MiscRepo.insertOrUpdateRouteHistory(routeKey!!)
    }
}
