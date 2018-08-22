package hoo.etahk.transfer.data

import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import java.io.File
import java.io.FileOutputStream

class Exporter: FilesWorker() {

    var backupFullPath = ""
        private set

    fun export(database: Boolean = true, sharedPreferences: Boolean = true): String {

        appData.createTime = Utils.getDateTimeString(Utils.getCurrentTimestamp(), Constants.Time.PATTERN_BACKUP_FILE)

        if (database && !prepareDatabase()) {
            return "Error 1"
        }

        if (sharedPreferences && !prepareSharedPref()) {
            return "Error 2"
        }

        if (!backup()) {
            return "Error 3"
        }

        return "The backup has been successfully saved to $backupFullPath"
    }


    private fun prepareDatabase(): Boolean {
        logd("Start Prepare Database")

        return try {
            val databaseData = DatabaseData(Constants.DATABASE_VERSION)

            databaseData.misc = AppHelper.db.miscDao().exportData()
            databaseData.followLocation = AppHelper.db.locationDao().exportData()
            databaseData.followGroup = AppHelper.db.groupDao().exportData()
            databaseData.followItem = AppHelper.db.itemDao().exportData()

            appData.databaseData = databaseData

            true
        } catch (e: Exception) {
            loge("prepareDatabase failed!", e)
            false
        }
    }

    private fun prepareSharedPref(): Boolean {
        logd("Start Prepare Shared Preferences")

        return true
    }

    private fun backup(): Boolean {
        if (appData.createTime.isBlank() || Environment.getExternalStorageState() != MEDIA_MOUNTED) {
            return false
        }

        val backupName = backupFolder + "EtaAtHK_" + appData.createTime + ".json"
        logd("Start Export to $backupName")

        return try {
            val str = gson.toJson(appData)

            val file = File(Environment.getExternalStorageDirectory().absolutePath, backupName)
            file.parentFile.mkdirs()

            val fos = FileOutputStream(file)
            fos.write(str.toByteArray())
            fos.close()

            backupFullPath = file.absolutePath

            true
        } catch (e: Exception) {
            loge("backup failed!", e)
            false
        }
    }
}