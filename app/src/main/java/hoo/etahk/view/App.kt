package hoo.etahk.view

import android.app.Application
import com.facebook.stetho.Stetho
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.SharedPrefsHelper

class App : Application() {

    companion object {
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initStetho()
        initSharePrefs()
        initAppHelper()
    }

    private fun initStetho() {
        Stetho.initializeWithDefaults(this)
    }

    private fun initSharePrefs() {
        SharedPrefsHelper.init(this)
    }

    private fun initAppHelper() {
        AppHelper.init(this)
    }

}