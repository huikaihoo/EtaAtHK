package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import hoo.etahk.model.relation.LocationAndGroups

@Dao
abstract class LocationGroupsDao {

    // Select
    @Transaction
    @Query("SELECT * FROM followLocation where displaySeq > 0 ORDER BY displaySeq")
    abstract fun select(): LiveData<List<LocationAndGroups>>

    @Transaction
    @Query("SELECT * FROM followLocation ORDER BY ABS(displaySeq)")
    abstract fun selectAll(): LiveData<List<LocationAndGroups>>

    @Transaction
    @Query("SELECT * FROM followLocation where displaySeq >= 0 ORDER BY displaySeq")
    abstract fun selectOnce(): List<LocationAndGroups>
}
