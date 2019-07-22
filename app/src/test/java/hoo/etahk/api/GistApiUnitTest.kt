package hoo.etahk.api

import android.util.Base64
import com.google.android.gms.common.util.IOUtils
import hoo.etahk.BaseUnitTest
import hoo.etahk.R
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.Stop
import hoo.etahk.remote.response.GistDatabaseRes
import hoo.etahk.view.App
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GistApiUnitTest: BaseUnitTest() {

    private val gistIdMap = hashMapOf(
        "kmb" to getStringFromResource(R.string.param_gist_id_kmb),
        "nwfb" to getStringFromResource(R.string.param_gist_id_nwfb),
        "mtrb" to getStringFromResource(R.string.param_gist_id_mtrb))

    private fun getRawUrl(company: String, gistId: String): String {
        var rawUrl: String? = null

        val call = ConnectionHelper.gist.getGist(gistId)
        println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                assert(!result?.files.isNullOrEmpty())

                rawUrl = result?.files?.get(company)?.rawUrl
                println("[${company.toUpperCase()}] rawUrl = $rawUrl")
                assert(!rawUrl.isNullOrBlank())
            } else {
                println("error = ${response.errorBody()?.string()}")
                assert(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }

        return rawUrl ?: ""
    }

    private fun unzipFile(zipFile: ZipFile, zipEntry: ZipEntry, outputPath: String) {
        val input = zipFile.getInputStream(zipEntry)
        val output = FileOutputStream(File(outputPath))

        IOUtils.copyStream(input, output)

        input.close()
        output.close()
    }

    @Test
    fun getGist() {
        gistIdMap.forEach { company, gistId ->
            if (gistId.isNotEmpty())
                getRawUrl(company, gistId)
        }
    }

    @Test
    fun getContent() {
        val rawUrlList = gistIdMap.map {
            println("$it / ${it.value.isNotEmpty()}")
            if(it.value.isNotEmpty()) getRawUrl(it.key, it.value) else ""
        }.filter { it.isNotEmpty() }

        for (rawUrl in rawUrlList) {
            val company = rawUrl.substring(rawUrl.lastIndexOf("/") + 1, rawUrl.length)
            val call = ConnectionHelper.gist.getContent(rawUrl)
            println("url = ${call.request().url()}")

            try {
                val response = call.execute()
                println("isSuccessful = ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val result = response.body()?.string()
                    assert(!result.isNullOrBlank())

                    // Decode to zip file
                    val file = File(App.instance.filesDir, "$company.zip")
                    val fos = FileOutputStream(file)
                    fos.write(Base64.decode(result!!, Base64.NO_WRAP))
                    fos.close()

                    println("[$company.zip] exist = ${file.isFile}")
                    assert(file.isFile)

                    val zipFile = ZipFile("${App.instance.filesDir.path}/$company.zip")
                    val jsonList = listOf("metadata.json", "parent.json", "child.json", "stops.json")

                    for (json in jsonList) {
                        // Extract file from zip
                        unzipFile(zipFile, zipFile.getEntry(json), "${App.instance.filesDir.path}/$json")
                        val jsonFile = File(App.instance.filesDir, json)

                        println("[$json] exist = ${jsonFile.isFile}")
                        assert(jsonFile.isFile)

                        // Check the content of files inside zip
                        val reader = BufferedReader(InputStreamReader(zipFile.getInputStream(zipFile.getEntry(json))))

                        when (json) {
                            "metadata.json" -> {
                                val metadata = gson.fromJson(reader, GistDatabaseRes.Metadata::class.java)

                                println("[$json] content = $metadata")
                                assert(metadata.parentCnt ?: 0 > 0 && metadata.childCnt ?: 0 > 0 && metadata.stopsCnt ?: 0 > 0 && metadata.routes?.isNotEmpty() ?: false)
                            }
                            "stops.json" -> {
                                val stops = gson.fromJson(reader, Array<Stop>::class.java).toList()

                                println("[$json] size = ${stops.size}")
                                println("[$json] first element = ${stops[0]}")
                                assert(stops.isNotEmpty())
                            }
                            else -> {
                                val routes = gson.fromJson(reader, Array<Route>::class.java).toList()

                                println("[$json] size = ${routes.size}")
                                println("[$json] first element = ${routes[0]}")
                                assert(routes.isNotEmpty())
                            }
                        }
                    }
                } else {
                    println("error = ${response.errorBody()?.string()}")
                    assert(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                assert(false)
            }
        }
    }

}
