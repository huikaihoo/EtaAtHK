package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import hoo.etahk.common.Constants.SharePrefs.DEFAULT_PAGE_SIZE
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.misc.BaseMisc
import hoo.etahk.model.misc.RouteFavourite
import hoo.etahk.model.misc.RouteHistory
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.model.relation.RouteHistoryEx
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

object MiscRepo {

    private const val TAG = "MiscRepo"

    fun getRouteFavourite(): LiveData<PagedList<RouteFavouriteEx>> {
        return LivePagedListBuilder(
            AppHelper.db.miscFavouriteDao().selectDS(),
            PagedList.Config.Builder().setPageSize(DEFAULT_PAGE_SIZE).build()
        ).build()
    }

    fun getRouteHistory(): LiveData<PagedList<RouteHistoryEx>> {
        return LivePagedListBuilder(
            AppHelper.db.miscHistoryDao().selectDS(),
            PagedList.Config.Builder().setPageSize(DEFAULT_PAGE_SIZE).build()
        ).build()
    }

    fun insertRouteFavourite(routeKey: RouteKey) {
        launch(CommonPool) {
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
        launch(CommonPool) {
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
        launch(CommonPool) {
            misc.displaySeq = AppHelper.db.miscDao().nextDisplaySeq(misc.miscType)
            misc.updateTime = Utils.getCurrentTimestamp()
            AppHelper.db.miscDao().insert(misc.toMisc())
        }
    }

    fun update(misc: BaseMisc) {
        launch(CommonPool) {
            misc.updateTime = Utils.getCurrentTimestamp()
            AppHelper.db.miscDao().update(misc.toMisc())
        }
    }

    fun delete(misc: BaseMisc) {
        launch(CommonPool) {
            AppHelper.db.miscDao().delete(misc.toMisc())
        }
    }
}