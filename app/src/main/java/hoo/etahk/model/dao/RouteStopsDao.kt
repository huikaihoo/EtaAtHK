package hoo.etahk.model.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import hoo.etahk.model.relation.RouteAndStops

@Dao
abstract class RouteStopsDao {

    // Select
    @Transaction
    @Query("SELECT * FROM route WHERE company = :company AND routeNo = :routeNo AND bound > 0 AND variant > 0 ORDER BY bound, variant")
    abstract fun select(company: String, routeNo: String): LiveData<List<RouteAndStops>>

    @Transaction
    @Query("SELECT * FROM route " +
            "WHERE company = :company " +
            "AND routeNo = :routeNo " +
            "AND bound = :bound " +
            "AND variant > 0 " +
            "ORDER BY variant")
    abstract fun select(company: String, routeNo: String, bound: Long): LiveData<List<RouteAndStops>>
}