package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import hoo.etahk.model.data.Stop

@Dao
interface StopsDao {

    @Query("SELECT COUNT(*) FROM stop")
    fun count(): Int

    @Query("SELECT * FROM stop")
    fun selectAll(): LiveData<List<Stop>>

    @Insert(onConflict = REPLACE)
    fun insert(stop: Stop)

    @Update
    fun update(stop: Stop)

    @Delete
    fun delete(stop: Stop)
}