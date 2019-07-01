package hoo.etahk.remote.connection

import com.google.firebase.perf.metrics.AddTrace
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.remote.response.GistDatabaseRes

object MtrbConnection: BaseConnection {

    /***************
     * Shared
     ***************/
    private fun getFromTo(routeName: String, index: Int): String {
        return routeName.split(">".toRegex())[index].trim()
    }

    override fun getEtaRoutes(company: String): List<String>? {
        return null
    }

    /**
     * Get List of Parent Routes
     *
     * @param company company code
     * @return map of route no to list of parent routes
     */
    override fun getParentRoutes(company: String): ParentRoutesMap? {
        val t = Utils.getCurrentTimestamp()
        val result = ParentRoutesMap()
        val gistId = SharedPrefsHelper.get<String>(R.string.param_gist_id_mtrb)

        try {
            val response = ConnectionHelper.gist.getGist(gistId).execute()

            logd("gistId = $gistId; isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val gistFile = response.body()?.files?.get(company.toLowerCase())
                val gistDatabaseRes =
                    if (gistFile != null) toGistDatabaseRes(company.toLowerCase(), gistFile, t) else GistDatabaseRes()

                logd("gistDatabaseRes.isValid = ${gistDatabaseRes.isValid}")

                if (gistDatabaseRes.isValid) {
                    result.addAll(gistDatabaseRes.parentRoutes)

                    val childRoutesMap = gistDatabaseRes.childRoutes.groupBy {
                        RouteKey(
                            company = it.routeKey.company,
                            routeNo = it.routeKey.routeNo,
                            bound = it.routeKey.bound,
                            variant = 0L
                        )
                    }
                    childRoutesMap.forEach { (routeKey, childRoutes) ->
                        AppHelper.db.childRouteDao().insertOrUpdate(childRoutes, t)
                    }
                    logd("After insert child routes")

                    if (gistDatabaseRes.stops.isNotEmpty()) {
                        AppHelper.db.stopDao().insertOnDeleteOld(listOf(Constants.Company.MTRB), gistDatabaseRes.stops)
                    }
                }

                logd("response ${result.size}")
            }
        } catch (e: Exception) {
            loge("getParentRoutes failed!", e)
        }

        return result
    }

    override fun getParentRoute(routeKey: RouteKey): Route? {
        return null
    }

    override fun getChildRoutes(parentRoute: Route) {
        return
    }

    override fun getStops(route: Route, needEtaUpdate: Boolean) {
        if (needEtaUpdate) {
            val stops = AppHelper.db.stopDao().selectOnce(
                route.routeKey.company,
                route.routeKey.routeNo,
                route.routeKey.bound,
                route.routeKey.variant)
            updateEta(stops)
        }
        return
    }

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    override fun getTimetableUrl(route: Route): String? {
        return null
    }

    override fun getTimetable(route: Route): String? {
        return null
    }

    override fun updateEta(stop: Stop) {
        return
    }

    @AddTrace(name = "MbrvConnection_updateEta")
    override fun updateEta(stops: List<Stop>) {
        return
    }
}