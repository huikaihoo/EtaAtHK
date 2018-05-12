package hoo.etahk.model.repo

import android.arch.lifecycle.LiveData
import hoo.etahk.R
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.data.FollowLocation
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.App
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

object FollowRepo {

    fun initLocationsAndGroups() {
        launch(CommonPool) {
            if (AppHelper.db.locationDao().count() <= 0) {
                AppHelper.db.locationDao().insert(FollowLocation(name = App.instance.getString(R.string.home), displaySeq = 1))
                AppHelper.db.locationDao().insert(FollowLocation(name = App.instance.getString(R.string.work), displaySeq = 2))

                AppHelper.db.locationDao().selectOnce().forEach {
                    val groupName = when (it.name) {
                        App.instance.getString(R.string.home) -> App.instance.getString(R.string.work)
                        App.instance.getString(R.string.work) -> App.instance.getString(R.string.home)
                        else -> ""
                    }
                    if (!groupName.isBlank()) {
                        AppHelper.db.groupDao().insert(FollowGroup(
                            locationId = it.Id!!,
                            name = groupName,
                            displaySeq = 1L))
                    }
                }
            }
        }
    }

    fun getLocations(): LiveData<List<LocationAndGroups>> {
        return AppHelper.db.locationGroupsDao().select()
    }

    fun getLocationsOnce(): List<LocationAndGroups> {
        return AppHelper.db.locationGroupsDao().selectOnce()
    }

    fun getItems(groupId: Long): LiveData<List<ItemAndStop>> {
        return AppHelper.db.itemStopDao().select(groupId)
    }

    fun insertItem(groupId: Long, stop: Stop) {
        launch(CommonPool) {
            val item = FollowItem(
                groupId = groupId,
                routeKey = stop.routeKey,
                seq = stop.seq,
                displaySeq = AppHelper.db.itemStopDao().nextItemDisplaySeq(groupId))

            AppHelper.db.itemDao().insert(item)
        }
    }

    fun updateItems(items: List<FollowItem>, newDisplaySeq: Boolean = false) {
        launch(CommonPool) {
            if (newDisplaySeq && items.isNotEmpty()) {
                var displaySeq = AppHelper.db.itemStopDao().nextItemDisplaySeq(items[0].groupId)
                items.forEach { it.displaySeq = displaySeq++ }
            }
            AppHelper.db.itemDao().update(items)
        }
    }

    fun deleteItem(item: FollowItem) {
        launch(CommonPool) {
            AppHelper.db.itemDao().delete(item)
        }
    }

    fun updateEta(items: List<ItemAndStop>?) {
        launch(CommonPool) {
            if (items != null && items.isNotEmpty())
                ConnectionHelper.updateItemsEta(items)
        }
    }
}