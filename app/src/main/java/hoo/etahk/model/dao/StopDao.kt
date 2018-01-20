package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop

@Dao
abstract class StopDao {

    // Count
    @Query("SELECT COUNT(*) FROM stop")
    abstract fun count(): Int

    // Select
    @Query("SELECT * FROM stop " +
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND variant = :variant " +
            "ORDER BY seq")
    abstract fun select(company: String,
                        routeNo: String,
                        bound: Long,
                        variant: Long): LiveData<List<Stop>>

//    @Query("SELECT * FROM stop " +
//            "WHERE company = :company " +
//            "AND routeNo = :routeNo " +
//            "AND bound = :bound " +
//            "AND variant = :variant " +
//            "ORDER BY seq")
//    abstract fun selectOnce(company: String,
//                            routeNo: String,
//                            bound: Long,
//                            variant: Long): List<Stop>

    @Query("SELECT * FROM stop")
    abstract fun selectAll(): LiveData<List<Stop>>

    // Insert / Update (single)
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract fun insert(stop: Stop)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(stop: Stop)

    // Insert / Update (list)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(stops: List<Stop>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(stops: List<Stop>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateOnReplace(stops: List<Stop>)

    @Transaction
    open fun insertOrUpdate(route: Route? = null, stops: List<Stop>, updateTime: Long? = null) {
        insert(stops)
        update(stops)
        if (route != null && updateTime != null) {
            delete(route.routeKey.company,
                    route.routeKey.routeNo,
                    route.routeKey.bound,
                    route.routeKey.variant,
                    updateTime)
        }
    }

    // Delete
//    @Delete
//    abstract fun delete(stop: Stop)

    @Query("DELETE FROM stop " +
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND variant = :variant " +
            "AND updateTime < :updateTime")
    abstract fun delete(company: String,
                        routeNo: String,
                        bound: Long,
                        variant: Long,
                        updateTime: Long)
}