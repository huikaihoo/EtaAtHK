package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import hoo.etahk.common.Constants
import hoo.etahk.model.data.Misc
import hoo.etahk.model.misc.RouteFav
import hoo.etahk.model.misc.RouteHist

@Dao
abstract class MiscDao {

    // Count
    @Query("SELECT COUNT(*) FROM Misc")
    abstract fun count(): Int

    // Select
    @Query("SELECT * FROM Misc WHERE miscType = :miscType")
    abstract fun select(miscType: Constants.MiscType): LiveData<List<Misc>>

    @Query("SELECT Id, miscType, dataStrA, dataStrB, displaySeq, updateTime FROM Misc WHERE miscType = 1")
    abstract fun selectRouteFav(): LiveData<List<RouteFav>>

    @Query("SELECT Id, miscType, dataStrA, dataStrB, freq, displaySeq, updateTime FROM Misc WHERE miscType = 2")
    abstract fun selectRouteHist(): LiveData<List<RouteHist>>

    // Insert / Update (single)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(item: Misc)

    // Insert / Update (list)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(items: List<Misc>)

    // Delete
    @Delete
    abstract fun delete(item: Misc)
}

