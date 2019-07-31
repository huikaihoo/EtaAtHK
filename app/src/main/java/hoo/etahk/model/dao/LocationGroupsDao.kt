package hoo.etahk.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import hoo.etahk.model.relation.LocationAndGroups

@Dao
abstract class LocationGroupsDao {

    // Select
    @Transaction
    @Query("SELECT * FROM followLocation where displaySeq > 0 ORDER BY pin DESC, displaySeq")
    abstract fun select(): LiveData<List<LocationAndGroups>>

    @Transaction
    @Query("SELECT * FROM followLocation ORDER BY ABS(displaySeq)")
    abstract fun selectAll(): LiveData<List<LocationAndGroups>>

    @Transaction
    @Query("SELECT * FROM followLocation where displaySeq >= 0 ORDER BY displaySeq")
    abstract fun selectOnce(): List<LocationAndGroups>
}
