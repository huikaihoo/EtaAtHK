package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import hoo.etahk.model.relation.ItemAndStop

@Dao
abstract class ItemStopDao {

    // Select
    @Transaction
    @Query("SELECT * FROM followItem WHERE groupId = :groupId and displaySeq > 0 ORDER BY displaySeq")
    abstract fun select(groupId: Long): LiveData<List<ItemAndStop>>

    @Transaction
    @Query("SELECT * FROM followItem WHERE groupId = :groupId ORDER BY ABS(displaySeq)")
    abstract fun selectAll(groupId: Long): LiveData<List<ItemAndStop>>

    @Query("SELECT IFNULL(MAX(displaySeq)+1, 1) FROM FollowItem WHERE groupId = :groupId")
    abstract fun nextItemDisplaySeq(groupId: Long): Long
}

