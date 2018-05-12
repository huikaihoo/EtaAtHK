package hoo.etahk.model.dao

import android.arch.persistence.room.*
import hoo.etahk.model.data.FollowItem

@Dao
abstract class ItemDao {

    // Count
    @Query("SELECT COUNT(*) FROM followItem")
    abstract fun count(): Int

    // Select
    @Query("SELECT IFNULL(MAX(displaySeq)+1, 1) FROM FollowItem WHERE groupId = :groupId")
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
}

