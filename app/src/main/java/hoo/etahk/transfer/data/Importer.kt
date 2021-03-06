package hoo.etahk.transfer.data

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import hoo.etahk.BuildConfig
import hoo.etahk.R
import hoo.etahk.common.Constants.DATABASE_NAME
import hoo.etahk.common.Constants.DATABASE_VERSION
import hoo.etahk.common.constants.SharedPrefs
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.view.App
import java.io.File
import java.io.FileInputStream

class Importer: FilesWorker() {

    private var fileMap = mapOf<String, File>()

    private class DBHelper :SQLiteOpenHelper(App.instance, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase?) {}
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
    }

    fun getBackupList(): Array<CharSequence> {
        return try {
            val directory = File(Environment.getExternalStorageDirectory().absolutePath, backupFolder)

            fileMap = directory.listFiles { dir, name ->
                name.startsWith("EtaAtHK_") && name.endsWith(".json", true)
            }.map{
                it.name.replace("^EtaAtHK_(.+).json\$".toRegex(), "$1")
                    .replace("([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2})([0-9]{2})".toRegex(),
                        "$1-$2-$3 $4:$5:$6") to it
            }.toMap()

            fileMap.keys.sortedDescending().toList().toTypedArray()
        } catch (e: Exception) {
            loge("Get backup list failed!", e)
            arrayOf()
        }
    }

    fun import(backupName: String): String {

        if (!prepare(backupName)) {
            return "File not exist / Cannot open file"
        }

        if (appData.databaseData != null && !restoreDatabase()) {
            return "Cannot restore data"
        }

        if (appData.sharedPrefData != null && !restoreSharedPref()) {
            return "Cannot restore shared preferences"
        }

        return "The backup ($backupName) has been restored successfully"
    }

    private fun prepare(backupName: String): Boolean {
        logd("Start Prepare Data From $backupName")

        val file = fileMap[backupName]

        if (file == null || !file.exists()) {
            return false
        }

        return try {
            val fis = FileInputStream(file)
            val data = ByteArray(file.length().toInt())
            fis.read(data)
            fis.close()

            val str = data.toString(Charsets.UTF_8)
            appData = gson.fromJson(str, AppData::class.java)

            return appData.packageName == BuildConfig.APPLICATION_ID
        } catch (e: Exception) {
            loge("Prepare failed!", e)
            false
        }
    }

    private fun restoreDatabase(): Boolean {
        logd("Start Restore Database")

        return try {
            val wdb = DBHelper().writableDatabase
            wdb.execSQL("DELETE FROM sqlite_sequence")
            wdb.close()

            if (appData.databaseData!!.version == DATABASE_VERSION) {
                if (appData.databaseData!!.misc.isNotEmpty()) {
                    AppHelper.db.miscDao().deleteAll()
                    AppHelper.db.miscDao().importData(appData.databaseData!!.misc)
                }
                if (appData.databaseData!!.followLocation.isNotEmpty()) {
                    AppHelper.db.locationDao().deleteAll()
                    AppHelper.db.locationDao().importData(appData.databaseData!!.followLocation)
                }
                if (appData.databaseData!!.followGroup.isNotEmpty()) {
                    AppHelper.db.groupDao().deleteAll()
                    AppHelper.db.groupDao().importData(appData.databaseData!!.followGroup)
                }
                if (appData.databaseData!!.followItem.isNotEmpty()) {
                    AppHelper.db.itemDao().deleteAll()
                    AppHelper.db.itemDao().importData(appData.databaseData!!.followItem)
                }
            }

            true
        } catch (e: Exception) {
            loge("Restore database failed!", e)
            false
        }
    }

    private fun restoreSharedPref(): Boolean {
        logd("Start Restore Shared Preferences")

        return try {
            if (appData.sharedPrefData!!.busJointly.isNotBlank())
                SharedPrefs.busJointly = appData.sharedPrefData!!.busJointly
            if (appData.sharedPrefData!!.appMode.isNotBlank())
                SharedPrefsHelper.put(R.string.param_app_mode, appData.sharedPrefData!!.appMode)
            if (appData.sharedPrefData!!.pagedListPageSize.isNotBlank())
                SharedPrefs.pagedListPageSize = appData.sharedPrefData!!.pagedListPageSize.toInt()
            if (appData.sharedPrefData!!.gistIdKmb.isNotBlank())
                SharedPrefs.gistIdKmb = appData.sharedPrefData!!.gistIdKmb
            if (appData.sharedPrefData!!.gistIdNwfb.isNotBlank())
                SharedPrefs.gistIdNwfb = appData.sharedPrefData!!.gistIdNwfb
            if (appData.sharedPrefData!!.gistIdMtrb.isNotBlank())
                SharedPrefs.gistIdMtrb = appData.sharedPrefData!!.gistIdMtrb
            if (appData.sharedPrefData!!.userAgent.isNotBlank())
                SharedPrefs.userAgent = appData.sharedPrefData!!.userAgent

            true
        } catch (e: Exception) {
            loge("Restore database failed!", e)
            false
        }
    }
}