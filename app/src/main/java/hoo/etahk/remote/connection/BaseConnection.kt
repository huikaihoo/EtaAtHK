package hoo.etahk.remote.connection

import android.util.Base64
import com.mcxiaoke.koi.ext.closeQuietly
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.helper.ZipHelper
import hoo.etahk.common.tools.ParentRoutesMap
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.EtaResult
import hoo.etahk.remote.response.GistDatabaseRes
import hoo.etahk.remote.response.GistRes
import hoo.etahk.view.App
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

interface BaseConnection {
    /**
     * Get List of Routes No that support ETA
     *
     * @param company company code
     * @return list of route no
     */
    fun getEtaRoutes(company: String = ""): List<String>?

    /**
     * Get List of Parent Routes
     *
     * @param company company code
     * @return map of route no to list of parent routes
     */
    fun getParentRoutes(company: String = ""): ParentRoutesMap?

    /**
     * Get Parent Route by Route No
     *
     * @param routeKey key of parent route
     * @return parent route
     */
    fun getParentRoute(routeKey: RouteKey): Route?

    /**
     * Get Child Route by Parent Route and update into DB
     *
     *  @param parentRoute parent route
     */
    fun getChildRoutes(parentRoute: Route)

    /**
     * Get list of stops and path by Child Route and update into DB
     *
     * @param route Child Route
     * @param needEtaUpdate update eta of stops as well if true
     */
    fun getStops(route: Route, needEtaUpdate: Boolean)

    /**
     * Get url of timetable of route
     *
     * @param route Child Route
     */
    fun getTimetableUrl(route: Route): String?

    /**
     * Get timetable of route
     *
     * @param route Child Route
     * @return timetable of child route
     */
    fun getTimetable(route: Route): String?

    @Deprecated("Use 'updateEta(List<Stop>): Unit' instead.")
    fun updateEta(stop: Stop)

    /**
     * Get Eta of list of stops and update into DB
     *
     * @param stops list of stops
     */
    fun updateEta(stops: List<Stop>)

    /**
     * Convert Eta result with message only (without time) to EtaResult
     *
     * @param stop stop where eta result belongs to
     * @param msg message of eta result
     * @return EtaResult that only contain message only
     */
    fun toEtaResult(stop: Stop, msg: String): EtaResult {
        return EtaResult(
            company = stop.routeKey.company,
            etaTime = -1L,
            msg = msg,
            scheduleOnly = false,
            distance = -1L)
    }

    /**
     * Convert Gist File to GistDatabaseRes
     *
     * @param gistFile Gist File
     * @param updateTime update time of GistDatabaseRes
     * @return Gist Database
     */
    fun toGistDatabaseRes(company: String, gistFile: GistRes.File, updateTime: Long): GistDatabaseRes {
        try {
            val rawUrl = gistFile.rawUrl ?: ""
            val version = rawUrl.substring(rawUrl.lastIndexOf("raw/") + 4, rawUrl.lastIndexOf("/"))
            val file = File("${App.instance.cacheDir.path}/$company", "$version.zip")

            logd("company = $company; version = $version; rawUrl = $rawUrl")

            if (version.isNotBlank() && company.isNotBlank()) {
                file.parentFile.mkdirs()

                if (!file.isFile) {
                    // Remove the old cache files
                    val oldFiles = file.parentFile.listFiles()
                    for (f in oldFiles)
                        f.delete()

                    // Get the result string
                    var result = ""
                    if (gistFile.truncated == false) {
                        // Get from gistFile
                        result = gistFile.content ?: ""
                    } else {
                        // Get from rawUrl
                        val response = ConnectionHelper.gist.getContent(rawUrl).execute()
                        logd("[$version] isSuccessful = ${response.isSuccessful}")

                        if (response.isSuccessful) {
                            result = response.body()?.string() ?: ""
                        }
                    }
                    logd("[$version] result.length = ${result.length}")

                    // Decode to zip file
                    if (result.isNotBlank()) {
                        val fos = FileOutputStream(file)
                        fos.write(Base64.decode(result, Base64.NO_WRAP))
                        fos.closeQuietly()
                    }
                } else {
                    logd("[$version] file already exist")
                }
            }

            if (file.isFile) {
                return ZipHelper.zipToGistDatabaseRes(ZipFile("${App.instance.cacheDir.path}/$company/$version.zip"), updateTime)
            } else {
                loge("[$version] file not exist")
            }
        } catch (e: Exception) {
            loge("toGistDatabaseRes failed!", e)
        }

        return GistDatabaseRes()
    }
}