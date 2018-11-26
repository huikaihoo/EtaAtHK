package hoo.etahk.model.dao

import androidx.room.*
import hoo.etahk.model.data.FollowLocation

@Dao
abstract class LocationDao {

    // Count
    @Query("SELECT COUNT(*) FROM followLocation")
    abstract fun count(): Int

    // Export / Import
    @Query("SELECT * FROM followLocation ORDER BY Id")
    abstract fun exportData(): List<FollowLocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun importData(items: List<FollowLocation>)

    // Select
    @Query("SELECT IFNULL(MAX(displaySeq)+1, 1) FROM followLocation")
    abstract fun nextDisplaySeq(): Long

    @Query("SELECT * FROM followLocation WHERE displaySeq > 0 ORDER BY displaySeq")
    abstract fun selectOnce(): List<FollowLocation>

    @Query("SELECT * FROM followLocation WHERE id = :locationId AND displaySeq > 0 ORDER BY displaySeq")
    abstract fun selectOnce(locationId: Long): FollowLocation

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

    // Delete
    @Delete
    abstract fun delete(item: FollowLocation)

    @Query("DELETE FROM followLocation")
    abstract fun deleteAll()
}
