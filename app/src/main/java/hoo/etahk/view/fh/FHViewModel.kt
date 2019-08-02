package hoo.etahk.view.fh

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import hoo.etahk.common.Constants.MiscType
import hoo.etahk.model.misc.BaseMisc
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.transfer.repo.MiscRepo

class FHViewModel : ViewModel() {
    var currentType: MiscType = MiscType.ROUTE_FAVOURITE
    val favouritePagedList: LiveData<PagedList<RouteFavouriteEx>>? = MiscRepo.getRouteFavourite()
    val historyPagedList: LiveData<PagedList<RouteHistoryEx>>? = MiscRepo.getRouteHistory()

    fun deleteMisc(misc: BaseMisc) {
        MiscRepo.delete(misc)
    }
}