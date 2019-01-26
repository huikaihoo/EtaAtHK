package hoo.etahk.view.route

import androidx.lifecycle.ViewModel
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.Route
import hoo.etahk.transfer.repo.RoutesRepo

class TimetableViewModel: ViewModel() {
    var route: Route? = null
        private set
    var content = ""
        private set

    fun init(company: String, routeNo: String, bound: Long, variant: Long) {
        route = AppHelper.db.childRouteDao().selectOnce(company, routeNo, bound, variant)
        if (route != null)
            content = RoutesRepo.getTimetable(route!!)
    }
}
