package hoo.etahk.transfer.repo

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import hoo.etahk.R
import hoo.etahk.common.Constants.SharePrefs.DEFAULT_PAGED_LIST_PAGE_SIZE
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.misc.BaseMisc
import hoo.etahk.model.misc.RouteFavourite
import hoo.etahk.model.misc.RouteHistory
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object MiscRepo {

    private val pagedListConfig = PagedList.Config.Builder()
        .setPageSize(SharedPrefsHelper.get(R.string.param_paged_list_page_size, DEFAULT_PAGED_LIST_PAGE_SIZE))
        .build()

    fun getRouteFavourite(): LiveData<PagedList<RouteFavouriteEx>> {
        return LivePagedListBuilder(
            AppHelper.db.miscFavouriteDao().selectDS(),
            pagedListConfig
        ).build()
    }

    fun getRouteHistory(): LiveData<PagedList<RouteHistoryEx>> {
        return LivePagedListBuilder(
            AppHelper.db.miscHistoryDao().selectDS(),
            pagedListConfig
        ).build()
    }

    fun insertRouteFavourite(routeKey: RouteKey) {
        GlobalScope.launch(Dispatchers.Default) {
            val favourite = AppHelper.db.miscFavouriteDao().selectOnce(routeKey.company, routeKey.routeNo)
            if (favourite == null) {
                insert(
                    RouteFavourite(
                        company = routeKey.company,
                        routeNo = routeKey.routeNo
                    )
                )
            }
        }
    }

    fun insertOrUpdateRouteHistory(routeKey: RouteKey) {
        GlobalScope.launch(Dispatchers.Default) {
            val history = AppHelper.db.miscHistoryDao().selectOnce(routeKey.company, routeKey.routeNo)
            if (history == null) {
                insert(RouteHistory(
                    company = routeKey.company,
                    routeNo = routeKey.routeNo,
                    freq = 1L)
                )
            } else {
                history.freq++
                update(history)
            }
        }
    }

    fun insert(misc: BaseMisc) {
        GlobalScope.launch(Dispatchers.Default) {
            misc.displaySeq = AppHelper.db.miscDao().nextDisplaySeq(misc.miscType)
            misc.updateTime = Utils.getCurrentTimestamp()
            AppHelper.db.miscDao().insert(misc.toMisc())
        }
    }

    fun update(misc: BaseMisc) {
        GlobalScope.launch(Dispatchers.Default) {
            misc.updateTime = Utils.getCurrentTimestamp()
            AppHelper.db.miscDao().update(misc.toMisc())
        }
    }

    fun delete(misc: BaseMisc) {
        GlobalScope.launch(Dispatchers.Default) {
            AppHelper.db.miscDao().delete(misc.toMisc())
        }
    }
}