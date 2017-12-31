package hoo.etahk.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import hoo.etahk.model.dao.RoutesDao
import hoo.etahk.model.dao.StopsDao
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

@Database(entities = [Route::class, Stop::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routesDao(): RoutesDao
    abstract fun stopsDao(): StopsDao
}