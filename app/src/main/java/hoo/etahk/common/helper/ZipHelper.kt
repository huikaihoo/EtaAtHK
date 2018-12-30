package hoo.etahk.common.helper

import hoo.etahk.common.Constants
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.remote.response.GistDatabaseRes
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipFile


object ZipHelper {
    fun zipToGistDatabaseRes(zipFile: ZipFile, updateTime: Long): GistDatabaseRes {
        val gistDatabaseRes = GistDatabaseRes()
        val entries = zipFile.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val reader = BufferedReader(InputStreamReader(zipFile.getInputStream(entry)))

            when (entry.name) {
                "metadata.json" ->
                    gistDatabaseRes.metadata = AppHelper.gson.fromJson(reader, GistDatabaseRes.Metadata::class.java)
                "parent.json" ->
                    gistDatabaseRes.parentRoutes = AppHelper.gson.fromJson(reader, Array<Route>::class.java).toList()
                "child.json" ->
                    gistDatabaseRes.childRoutes = AppHelper.gson.fromJson(reader, Array<Route>::class.java).toList()
                "stops.json" ->
                    gistDatabaseRes.stops = AppHelper.gson.fromJson(reader, Array<Stop>::class.java).toList()

            }
        }

        gistDatabaseRes.parentRoutes.forEach {
            it.updateTime = updateTime
        }

        gistDatabaseRes.childRoutes.forEach {
            it.updateTime = updateTime
        }

        gistDatabaseRes.stops.forEach {
            it.etaStatus = Constants.EtaStatus.NONE
            it.etaResults = emptyList()
            it.etaUpdateTime = 0L
            it.updateTime = updateTime
        }

        return gistDatabaseRes
    }
}