package hoo.etahk.model.dao

import androidx.room.*
import hoo.etahk.model.data.FollowGroup

@Dao
abstract class GroupDao {

    // Count
    @Query("SELECT COUNT(*) FROM followGroup")
    abstract fun count(): Int

    // Export / Import
    @Query("SELECT * FROM followGroup ORDER BY Id")
    abstract fun exportData(): List<FollowGroup>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun importData(followGroups: List<FollowGroup>)

    // Select
    @Query("SELECT IFNULL(MAX(displaySeq)+1, 1) FROM followGroup WHERE locationId = :locationId")
    abstract fun nextDisplaySeq(locationId: Long): Long

    // Insert / Update
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(group: FollowGroup)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(group: FollowGroup)

    @Transaction
    open fun insertOrUpdate(group: FollowGroup) {
        insert(group)
        update(group)
    }

    // Delete
    @Delete
    abstract fun delete(item: FollowGroup)

    @Query("DELETE FROM followGroup")
    abstract fun deleteAll()
}
