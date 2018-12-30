package hoo.etahk.api

import android.util.Base64
import com.google.android.gms.common.util.IOUtils
import hoo.etahk.BaseUnitTest
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

    private val getGistId = getStringFromResource("gistIdKmb.txt")

    private fun getRawUrl(): List<String> {
        val gistId = getGistId
        val rawUrlList = mutableListOf<String>()

        val call = ConnectionHelper.gist.getGist(gistId)
        System.out.println("url = ${call.request().url()}")

        try {
            val response = call.execute()
            System.out.println("isSuccessful = ${response.isSuccessful}")

            if (response.isSuccessful) {
                val result = response.body()
                System.out.println("[KMB] rawUrl = ${result?.files?.kmb?.rawUrl}")
                assert(result?.files?.kmb?.rawUrl?.isNotEmpty() ?: false)
                rawUrlList.add(result?.files?.kmb?.rawUrl ?: "")
            } else {
                System.out.println("error = ${response.errorBody()?.string()}")
                assert(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }

        return rawUrlList
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
        getRawUrl()
    }

    @Test
    fun getContent() {
        val rawUrlList = getRawUrl()
        for (rawUrl in rawUrlList) {
            val company = rawUrl.substring(rawUrl.lastIndexOf("/") + 1, rawUrl.length)
            val call = ConnectionHelper.gist.getContent(rawUrl)
            System.out.println("url = ${call.request().url()}")

            try {
                val response = call.execute()
                System.out.println("isSuccessful = ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val result = response.body()?.string()
                    assert(!result.isNullOrBlank())

                    // Decode to zip file
                    val file = File(App.instance.filesDir, "$company.zip")
                    val fos = FileOutputStream(file)
                    fos.write(Base64.decode(result!!, Base64.NO_WRAP))
                    fos.close()

                    System.out.println("[$company.zip] exist = ${file.isFile}")
                    assert(file.isFile)

                    val zipFile = ZipFile("${App.instance.filesDir.path}/$company.zip")
                    val jsonList = listOf("metadata.json", "parent.json", "child.json", "stops.json")

                    for (json in jsonList) {
                        // Extract file from zip
                        unzipFile(zipFile, zipFile.getEntry(json), "${App.instance.filesDir.path}/$json")
                        val jsonFile = File(App.instance.filesDir, json)

                        System.out.println("[$json] exist = ${jsonFile.isFile}")
                        assert(jsonFile.isFile)

                        // Check the content of files inside zip
                        val reader = BufferedReader(InputStreamReader(zipFile.getInputStream(zipFile.getEntry(json))))

                        when (json) {
                            "metadata.json" -> {
                                val metadata = gson.fromJson(reader, GistDatabaseRes.Metadata::class.java)

                                System.out.println("[$json] content = $metadata")
                                assert(metadata.parentCnt ?: 0 > 0 && metadata.childCnt ?: 0 > 0 && metadata.stopsCnt ?: 0 > 0 && metadata.routes?.isNotEmpty() ?: false)
                            }
                            "stops.json" -> {
                                val stops = gson.fromJson(reader, Array<Stop>::class.java).toList()

                                System.out.println("[$json] size = ${stops.size}")
                                System.out.println("[$json] first element = ${stops[0]}")
                                assert(stops.isNotEmpty())
                            }
                            else -> {
                                val routes = gson.fromJson(reader, Array<Route>::class.java).toList()

                                System.out.println("[$json] size = ${routes.size}")
                                System.out.println("[$json] first element = ${routes[0]}")
                                assert(routes.isNotEmpty())
                            }
                        }
                    }
                } else {
                    System.out.println("error = ${response.errorBody()?.string()}")
                    assert(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                assert(false)
            }
        }
    }

}