package hoo.etahk.model.dao

import android.arch.persistence.room.*
import hoo.etahk.model.data.FollowItem

@Dao
abstract class ItemDao {

    // Count
    @Query("SELECT COUNT(*) FROM followItem")
    abstract fun count(): Int

    // Export / Import
    @Query("SELECT * FROM followItem ORDER BY Id")
    abstract fun exportData(): List<FollowItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun importData(followGroups: List<FollowItem>)

    // Select
    @Query("SELECT IFNULL(MAX(displaySeq)+1, 1) FROM followItem WHERE groupId = :groupId")
    abstract fun nextDisplaySeq(groupId: Long): Long

    // Insert / Update (single)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(item: FollowItem)

    // Insert / Update (list)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(items: List<FollowItem>)

    // Delete
    @Delete
    abstract fun delete(item: FollowItem)

    @Query("DELETE FROM followItem")
    abstract fun deleteAll()
}

