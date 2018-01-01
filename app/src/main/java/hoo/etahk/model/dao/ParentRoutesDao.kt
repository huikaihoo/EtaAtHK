package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import hoo.etahk.model.data.Route

@Dao
abstract class ParentRoutesDao {

    // Count
    @Query("SELECT COUNT(*) FROM route WHERE bound = 0 AND variant = 0")
    abstract fun count(): Int

    // Select
    @Query("SELECT * FROM route WHERE company = :company AND bound = 0 AND variant = 0 ORDER BY seq, routeNo")
    abstract fun select(company: String): LiveData<List<Route>>

    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND bound = 0 AND variant = 0 LIMIT 1")
    abstract fun select(company: String, routeNo: String): LiveData<Route>

    // Insert / Update (single)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(route: Route)
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    abstract fun update(route: Route)

    // Insert / Update (list)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(route: List<Route>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(route: List<Route>)

    @Transaction
    open fun insertOrUpdate(routes: List<Route>, updateTime: Long? = null) {
        insert(routes)
        update(routes)
        if (updateTime != null) {
            delete(updateTime)
        }
    }

    // Delete
    @Delete
    abstract fun delete(route: Route)

    @Query("DELETE FROM route " +
            "WHERE bound = 0 " +
            "AND variant = 0 " +
            "AND updateTime < :updateTime")
    abstract fun delete(updateTime: Long)
}
