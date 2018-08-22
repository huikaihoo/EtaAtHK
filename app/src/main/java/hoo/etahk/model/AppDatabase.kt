package hoo.etahk.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import hoo.etahk.common.Constants.DATABASE_VERSION
import hoo.etahk.model.dao.*
import hoo.etahk.model.data.*

@Database(entities = [Route::class, Stop::class, Path::class,
                      FollowLocation::class, FollowGroup::class, FollowItem::class,
                      Misc::class], version = DATABASE_VERSION)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Route and Stop
    abstract fun parentRouteDao(): ParentRouteDao
    abstract fun childRouteDao(): ChildRouteDao
    abstract fun stopDao(): StopDao
    abstract fun pathDao(): PathDao
    abstract fun routeStopsDao(): RouteStopsDao
    // Follow Location / Group / Item
    abstract fun locationDao(): LocationDao
    abstract fun groupDao(): GroupDao
    abstract fun itemDao(): ItemDao
    abstract fun locationGroupsDao(): LocationGroupsDao
    abstract fun itemStopDao(): ItemStopDao
    // Misc
    abstract fun miscDao(): MiscDao
    abstract fun miscFavouriteDao(): MiscFavouriteDao
    abstract fun miscHistoryDao(): MiscHistoryDao
}