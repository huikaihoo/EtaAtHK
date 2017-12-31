package hoo.etahk.view.route

import android.arch.lifecycle.LiveData
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.repo.RoutesRepo
import hoo.etahk.view.base.BaseViewModel

class RouteViewModel: BaseViewModel() {
    private var mRoute: LiveData<Route>? = null

    var routeKey: RouteKey? = null
    set(value) {
        field = value
        if (value != null)
            subscribeToRepo()
    }

    fun insertRoutes() {
        RoutesRepo.insertRoute()
    }

    fun getRoute(): LiveData<Route> {
        return mRoute!!
    }

    private fun subscribeToRepo() {
        mRoute = RoutesRepo.getParentRoute(routeKey!!.company, routeKey!!.routeNo)
    }
}
