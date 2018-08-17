package hoo.etahk.view.fh

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.PagedList
import hoo.etahk.R
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import hoo.etahk.model.repo.MiscRepo

class FHViewModel : ViewModel() {

    companion object {
        private const val TAG = "FHViewModel"
    }

    var currentItemId: Int = R.id.nav_favourite
    val favouritePagedList: LiveData<PagedList<RouteFavouriteEx>>? = MiscRepo.getRouteFavourite()
    val historyPagedList: LiveData<PagedList<RouteHistoryEx>>? = MiscRepo.getRouteHistory()
}