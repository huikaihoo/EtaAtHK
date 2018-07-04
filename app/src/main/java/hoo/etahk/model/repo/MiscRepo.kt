package hoo.etahk.model.repo

import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.misc.BaseMisc
import hoo.etahk.model.misc.RouteFavourite
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

object MiscRepo {

    private const val TAG = "MiscRepo"

    fun insertRouteFavourite(routeKey: RouteKey) {
        insert(
            RouteFavourite(
                company = routeKey.company,
                routeNo = routeKey.routeNo)
        )
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