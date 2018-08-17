package hoo.etahk.model.dao

import android.arch.persistence.room.*
import hoo.etahk.common.Constants
import hoo.etahk.model.data.Misc

@Dao
abstract class MiscDao {

    // Count
    @Query("SELECT COUNT(*) FROM Misc")
    abstract fun count(): Int

    // Select
    @Query("SELECT IFNULL(MAX(displaySeq)+1, 1) FROM Misc WHERE miscType = :miscType")
    abstract fun nextDisplaySeq(miscType: Constants.MiscType): Long

    // Insert / Update (single)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(item: Misc)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(item: Misc)

    // Delete
    @Delete
    abstract fun delete(item: Misc)
}

