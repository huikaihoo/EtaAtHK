package hoo.etahk.transfer.repo

import androidx.lifecycle.LiveData
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.data.FollowLocation
import hoo.etahk.model.data.Stop
import hoo.etahk.model.relation.ItemAndStop
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object FollowRepo {

    fun initLocationsAndGroups() {
        GlobalScope.launch(Dispatchers.Default) {
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
                            displaySeq = 1L,
                            updateTime = Utils.getCurrentTimestamp()))
                    }
                }
            }
        }
    }

    fun getLocationOnce(locationId: Long): FollowLocation {
        return AppHelper.db.locationDao().selectOnce(locationId)
    }

    fun getLocations(): LiveData<List<LocationAndGroups>> {
        return AppHelper.db.locationGroupsDao().select()
    }

    fun getLocationsOnce(): List<LocationAndGroups> {
        return AppHelper.db.locationGroupsDao().selectOnce()
    }

    @Deprecated("Use 'insertLocation(FollowLocation): Unit' instead.")
    fun insertLocation(name: String) {
        GlobalScope.launch(Dispatchers.Default) {
            val location = FollowLocation(
                name = name,
                displaySeq = AppHelper.db.locationDao().nextDisplaySeq(),
                updateTime = Utils.getCurrentTimestamp())
            AppHelper.db.locationDao().insert(location)
        }
    }

    fun insertLocation(location: FollowLocation) {
        GlobalScope.launch(Dispatchers.Default) {
            location.displaySeq = AppHelper.db.locationDao().nextDisplaySeq()
            location.updateTime = Utils.getCurrentTimestamp()
            AppHelper.db.locationDao().insert(location)
        }
    }

    fun updateLocation(location: FollowLocation) {
        GlobalScope.launch(Dispatchers.Default) {
            location.updateTime = Utils.getCurrentTimestamp()
            AppHelper.db.locationDao().update(location)
        }
    }

    fun deleteLocation(location: FollowLocation) {
        GlobalScope.launch(Dispatchers.Default) {
            AppHelper.db.locationDao().delete(location)
        }
    }

    fun insertGroup(locationId: Long, name: String) {
        GlobalScope.launch(Dispatchers.Default) {
            val group = FollowGroup(
                locationId = locationId,
                name = name,
                displaySeq = AppHelper.db.groupDao().nextDisplaySeq(locationId),
                updateTime = Utils.getCurrentTimestamp())

            AppHelper.db.groupDao().insert(group)
        }
    }

    fun updateGroup(group: FollowGroup) {
        GlobalScope.launch(Dispatchers.Default) {
            group.updateTime = Utils.getCurrentTimestamp()
            AppHelper.db.groupDao().update(group)
        }
    }

    fun deleteGroup(group: FollowGroup) {
        GlobalScope.launch(Dispatchers.Default) {
            AppHelper.db.groupDao().delete(group)
        }
    }

    fun getItems(groupId: Long): LiveData<List<ItemAndStop>> {
        return AppHelper.db.itemStopDao().select(groupId)
    }

    fun insertItem(groupId: Long, stop: Stop) {
        GlobalScope.launch(Dispatchers.Default) {
            val item = FollowItem(
                groupId = groupId,
                routeKey = stop.routeKey,
                seq = stop.seq,
                displaySeq = AppHelper.db.itemDao().nextDisplaySeq(groupId),
                updateTime = Utils.getCurrentTimestamp())

            AppHelper.db.itemDao().insert(item)
        }
    }

    fun updateItems(items: List<FollowItem>, newDisplaySeq: Boolean = false) {
        GlobalScope.launch(Dispatchers.Default) {
            val t  = Utils.getCurrentTimestamp()
            if (newDisplaySeq && items.isNotEmpty()) {
                var displaySeq = AppHelper.db.itemDao().nextDisplaySeq(items[0].groupId)
                items.forEach {
                    it.displaySeq = displaySeq++
                    it.updateTime = t
                }
            }
            AppHelper.db.itemDao().update(items)
        }
    }

    fun deleteItem(item: FollowItem) {
        GlobalScope.launch(Dispatchers.Default) {
            AppHelper.db.itemDao().delete(item)
        }
    }
}