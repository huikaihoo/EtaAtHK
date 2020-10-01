package hoo.etahk.view

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import get
import hoo.etahk.common.Constants.AppMode.DEV
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.applyLocale
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.ConnectionHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.Module

open class App : Application() {

    companion object {
        lateinit var instance: App private set
        val networkFlipperPlugin: NetworkFlipperPlugin = NetworkFlipperPlugin()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.applyLocale(PreferenceManager.getDefaultSharedPreferences(base).get(base.getString(hoo.etahk.R.string.pref_language))))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initKoin()
        initAppHelper()
        initSharePrefs()
        initCrashlytics()
        initStetho()
        initFlipper()
    }

    protected open fun getModules(): List<Module> {
        return ConnectionHelper.modules
    }

    private fun initKoin() {
        startKoin {
            androidLogger()             // use AndroidLogger as Koin Logger - default Level.INFO
            androidContext(this@App)    // use the Android context given there
            androidFileProperties()     // load properties from assets/koin.properties file
            modules(getModules())
        }
    }

    private fun initAppHelper() {
        AppHelper.init(this)
    }

    private fun initSharePrefs() {
        SharedPrefsHelper.init(this)
    }

    private fun initCrashlytics() {
        AppHelper.crashlytics.setCrashlyticsCollectionEnabled(!Utils.isUnitTest)
        if (!Utils.isUnitTest) {
            AppHelper.crashlytics.setUserId(SharedPrefsHelper.getUserUUID())
        }
    }

    private fun initStetho() {
        if (SharedPrefsHelper.getAppMode() == DEV && !Utils.isUnitTest)
            Stetho.initializeWithDefaults(this)
    }

    private fun initFlipper() {
        if (SharedPrefsHelper.getAppMode() == DEV && !Utils.isUnitTest) {
            SoLoader.init(this, false)

            if (FlipperUtils.shouldEnableFlipper(this)) {
                val client = AndroidFlipperClient.getInstance(this)

                client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
                client.addPlugin(networkFlipperPlugin)
                client.addPlugin(DatabasesFlipperPlugin(this))
                client.addPlugin(SharedPreferencesFlipperPlugin(this))

                client.start()
            }
        }
    }

    fun getVersionName(): String {
        return packageManager.getPackageInfo(packageName, 0).versionName
    }
}