package hoo.etahk.view

import android.app.Application
import com.facebook.stetho.Stetho
import hoo.etahk.common.Constants.AppMode.DEV
import hoo.etahk.common.Utils
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.helper.SharedPrefsHelper

class App : Application() {

    companion object {
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initSharePrefs()
        initStetho()
        initAppHelper()
        initConnectionHelper()
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
}