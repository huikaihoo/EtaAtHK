package hoo.etahk.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
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
}

