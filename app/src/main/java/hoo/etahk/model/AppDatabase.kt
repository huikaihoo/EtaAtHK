package hoo.etahk.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import hoo.etahk.model.dao.ChildRouteDao
import hoo.etahk.model.dao.ParentRouteDao
import hoo.etahk.model.dao.RouteStopsDao
import hoo.etahk.model.dao.StopDao
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

@Database(entities = [Route::class, Stop::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parentRouteDao(): ParentRouteDao
    abstract fun childRouteDao(): ChildRouteDao
    abstract fun stopDao(): StopDao
    abstract fun routeStopsDao(): RouteStopsDao
}