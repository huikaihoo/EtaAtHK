package hoo.etahk.view

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import hoo.etahk.common.Constants.AppMode.DEV
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import io.fabric.sdk.android.Fabric

class App : Application() {

    companion object {
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initSharePrefs()
        initCrashlytics()
        initStetho()
        initAppHelper()
        initConnectionHelper()
    }

    private fun initCrashlytics() {
        Fabric.with(this, Crashlytics())
        Crashlytics.setUserIdentifier(SharedPrefsHelper.getUserUUID())
    }

    private fun initSharePrefs() {
        SharedPrefsHelper.init(this)
    }

    private fun initStetho() {
        if (SharedPrefsHelper.getAppMode() == DEV && !Utils.isUnitTest)
            Stetho.initializeWithDefaults(this)
    }

    private fun initAppHelper() {
        AppHelper.init(this)
    }

    private fun initConnectionHelper() {
        ConnectionHelper.init()
    }

    fun getVersionName(): String {
        return packageManager.getPackageInfo(packageName, 0).versionName
    }
}