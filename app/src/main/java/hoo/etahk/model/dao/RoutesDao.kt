package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import hoo.etahk.model.data.Route

@Dao
abstract class RoutesDao {

    @Query("SELECT COUNT(*) FROM route")
    abstract fun count(): Int

    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND bound = :bound AND variant = :variant")
    abstract fun select(company: String, routeNo: String, bound: Long, variant: Long): LiveData<Route>

    @Query("SELECT * FROM route WHERE company = :company AND variant = 0 ORDER BY seq, routeNo")
    abstract fun selectParent(company: String): LiveData<List<Route>>

    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND variant = 0 LIMIT 1")
    abstract fun selectParent(company: String, routeNo: String): LiveData<Route>

    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND variant > 0 ORDER BY bound, variant")
    abstract fun selectChild(company: String, routeNo: String): LiveData<List<Route>>

    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND bound = :bound AND variant > 0 ORDER BY variant")
    abstract fun selectChild(company: String, routeNo: String, bound: Long): LiveData<List<Route>>

    @Insert(onConflict = REPLACE)
    abstract fun insert(route: Route)

    @Update
    abstract fun update(route: Route)

    @Delete
    abstract fun delete(route: Route)
}
