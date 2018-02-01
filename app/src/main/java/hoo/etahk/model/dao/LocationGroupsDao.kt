package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import hoo.etahk.model.data.FollowLocation
import hoo.etahk.model.relation.LocationAndGroups

@Dao
abstract class LocationGroupsDao {

    // Count
    @Query("SELECT COUNT(*) FROM followLocation")
    abstract fun count(): Int

    // Select
    @Transaction
    @Query("SELECT * FROM followLocation where displaySeq > 0 ORDER BY displaySeq")
    abstract fun select(): LiveData<List<LocationAndGroups>>

    @Transaction
    @Query("SELECT * FROM followLocation ORDER BY ABS(displaySeq)")
    abstract fun selectAll(): LiveData<List<LocationAndGroups>>

    // Insert / Update (list)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(location: FollowLocation)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(location: FollowLocation)

    @Transaction
    open fun insertOrUpdate(location: FollowLocation) {
        insert(location)
        update(location)
    }
}
