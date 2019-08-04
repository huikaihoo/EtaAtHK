package hoo.etahk.view.fh

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import hoo.etahk.common.Constants.MiscType
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.misc.BaseMisc
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.transfer.repo.MiscRepo

class FHViewModel : ViewModel() {
    var currentType: MiscType = MiscType.ROUTE_FAVOURITE
    val favouritePagedList: LiveData<PagedList<RouteFavouriteEx>>? = MiscRepo.getRouteFavourite()
    val historyPagedList: LiveData<PagedList<RouteHistoryEx>>? = MiscRepo.getRouteHistory()

    fun insertRouteFavourite(route: Route) {
        val routeKey = RouteKey(
            company = route.companyDetails[0],
            routeNo = route.routeKey.routeNo,
            bound = 0L,
            variant = 0L
        )
        val anotherCompany = if (route.companyDetails.size > 1) route.companyDetails[1] else null
        MiscRepo.insertRouteFavourite(routeKey, anotherCompany)
    }

    fun deleteMisc(misc: BaseMisc) {
        MiscRepo.delete(misc)
    }

}