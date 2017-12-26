package hoo.etahk.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import hoo.etahk.model.dao.StopsDao
import hoo.etahk.model.data.Stop

@Database(entities = [Stop::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stopsDao(): StopsDao
}