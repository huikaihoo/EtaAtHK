package hoo.etahk.model.dao

import android.arch.persistence.room.*
import hoo.etahk.model.data.FollowLocation

@Dao
abstract class LocationDao {

    // Count
    @Query("SELECT COUNT(*) FROM followLocation")
    abstract fun count(): Int

    // Select
    @Query("SELECT * FROM FollowLocation WHERE displaySeq > 0 ORDER BY displaySeq")
    abstract fun selectOnce(): List<FollowLocation>

    // Insert / Update
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
