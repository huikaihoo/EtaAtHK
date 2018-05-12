package hoo.etahk.model.dao

import android.arch.persistence.room.*
import hoo.etahk.model.data.FollowGroup

@Dao
abstract class GroupDao {

    // Count
    @Query("SELECT COUNT(*) FROM followGroup")
    abstract fun count(): Int

    // Select

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
}
