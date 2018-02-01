package hoo.etahk.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import hoo.etahk.model.dao.*
import hoo.etahk.model.data.*

@Database(entities = [Route::class, Stop::class,
                      FollowLocation::class, FollowGroup::class, FollowItem::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Route and Stop
    abstract fun parentRouteDao(): ParentRouteDao
    abstract fun childRouteDao(): ChildRouteDao
    abstract fun stopDao(): StopDao
    abstract fun routeStopsDao(): RouteStopsDao
    // Follow Location / Group / Item
    abstract fun locationGroupsDao(): LocationGroupsDao
    abstract fun itemStopDao(): ItemStopDao
}