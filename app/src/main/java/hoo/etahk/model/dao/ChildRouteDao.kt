package hoo.etahk.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import hoo.etahk.model.data.Route

@Dao
abstract class ChildRouteDao {

    // Count
    @Query("SELECT COUNT(*) FROM route WHERE bound > 0 AND variant > 0")
    abstract fun count(): Int

    @Query("SELECT IFNULL(MIN(updateTime), 0) FROM route WHERE company = :company AND routeNo = :routeNo AND bound > 0 AND variant > 0")
    abstract fun lastUpdate(company: String, routeNo: String): Long

    // Select
    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND bound > 0 AND variant > 0 ORDER BY bound, variant")
    abstract fun select(company: String, routeNo: String): LiveData<List<Route>>

    @Query("SELECT * FROM route " +
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND variant > 0 " +
            "ORDER BY variant")
    abstract fun select(company: String, routeNo: String, bound: Long): LiveData<List<Route>>

    @Query("SELECT * FROM route " +
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND variant = :variant")
    abstract fun select(company: String, routeNo: String, bound: Long, variant: Long): LiveData<Route>

    // Insert / Update (single)
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    abstract fun insert(route: Route)
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    abstract fun update(route: Route)

    // Insert / Update (list)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(routes: List<Route>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(routes: List<Route>)

    @Transaction
    open fun insertOrUpdate(routes: List<Route>, updateTime: Long? = null) {
        insert(routes)
        update(routes)
        if (updateTime != null && routes.isNotEmpty()) {
            delete(routes[0].routeKey.company,
                    routes[0].routeKey.routeNo,
                    routes[0].routeKey.bound,
                    updateTime)
        }
    }

    // Delete
//    @Delete
//    abstract fun delete(route: Route)

    @Query("DELETE FROM route " +
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND updateTime < :updateTime")
    abstract fun delete(company: String,
                        routeNo: String,
                        bound: Long,
                        updateTime: Long)
}
