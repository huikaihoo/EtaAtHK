package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

object FollowRepo {

    fun getLocations(): LiveData<List<LocationAndGroups>> {
        return AppHelper.db.locationGroupsDao().select()
    }

    fun getItems(groupId: Long): LiveData<List<ItemAndStop>> {
        return AppHelper.db.itemStopDao().select(groupId)
    }

    fun updateEta(items: List<ItemAndStop>?) {
        launch(CommonPool) {
            if (items != null && items.isNotEmpty())
                ConnectionHelper.updateItemsEta(items)
        }
    }
}