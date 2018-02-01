package hoo.etahk.view.route

import android.arch.lifecycle.LiveData
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.repo.RoutesRepo
import hoo.etahk.view.base.TimerViewModel

class RouteViewModel: TimerViewModel() {
    private var mParentRoute: LiveData<Route>? = null
    private var hasUpdateChildRoutes = false

    var selectedTabPosition: Int = 0

    var routeKey: RouteKey? = null
        set(value) {
            field = value
            if (value != null)
                subscribeToRepo()
        }

    fun getParentRoute(): LiveData<Route> {
        return mParentRoute!!
    }

    fun updateChildRoutes(parentRoute: Route) {
        if (!hasUpdateChildRoutes) {
            hasUpdateChildRoutes = true
            RoutesRepo.updateChildRoutes(parentRoute)
        }
    }

    private fun subscribeToRepo() {
        mParentRoute = RoutesRepo.getParentRoute(routeKey!!.company, routeKey!!.routeNo)
    }
}
