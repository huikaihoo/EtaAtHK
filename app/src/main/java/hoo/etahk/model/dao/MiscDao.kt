package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import hoo.etahk.common.Constants
import hoo.etahk.model.data.Misc
import hoo.etahk.model.misc.RouteFavourite
import hoo.etahk.model.misc.RouteHistory

@Dao
abstract class MiscDao {

    // Count
    @Query("SELECT COUNT(*) FROM Misc")
    abstract fun count(): Int

    // Select
    @Query("SELECT IFNULL(MAX(displaySeq)+1, 1) FROM Misc WHERE miscType = :miscType")
    abstract fun nextDisplaySeq(miscType: Constants.MiscType): Long

    @Query("SELECT * FROM Misc WHERE miscType = :miscType")
    abstract fun select(miscType: Constants.MiscType): LiveData<List<Misc>>

    @Query("SELECT Id, miscType, relationStr, dataStrA, dataStrB, displaySeq, updateTime FROM Misc WHERE miscType = 1")
    abstract fun selectRouteFavourite(): LiveData<List<RouteFavourite>>

    @Query("SELECT Id, miscType, relationStr, dataStrA, dataStrB, freq, displaySeq, updateTime FROM Misc WHERE miscType = 2")
    abstract fun selectRouteHistory(): LiveData<List<RouteHistory>>

    // Insert / Update (single)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(item: Misc)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(item: Misc)

    // Delete
    @Delete
    abstract fun delete(item: Misc)
}

