package hoo.etahk.common.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import get
import hoo.etahk.BuildConfig
import hoo.etahk.R
import hoo.etahk.common.Constants.AppMode.BETA
import hoo.etahk.common.Constants.AppMode.DEV
import hoo.etahk.common.Constants.AppMode.RELEASE
import hoo.etahk.view.App
import put



object SharedPrefsHelper {
    lateinit var default: SharedPreferences
    @SuppressLint("StaticFieldLeak")
    lateinit var remote: FirebaseRemoteConfig

    val remoteCacheExpiration
        get() = if (getAppMode() == DEV) 10L else 43200L

    fun init(context: Context) {
        default = PreferenceManager.getDefaultSharedPreferences(context)
        remote = FirebaseRemoteConfig.getInstance()

        remote.setConfigSettings(FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(getAppMode() == DEV)
            .build())
        remote.setDefaults(R.xml.remote_config_defaults)
    }

    fun getAppMode(): Long {
        return when(BuildConfig.DEBUG) {
            true -> DEV
            false -> {
                when (get<String>(R.string.param_app_mode)) {
                    "dev" -> DEV
                    "beta" -> BETA
                    else ->  RELEASE
                }
            }
        }
    }

    inline fun <reified T> get(@StringRes resId: Int): T {
        return default.get(App.instance.getString(resId))
    }

    inline fun <reified T> get(@StringRes resId: Int, defaultValue: T): T {
        return default.get(App.instance.getString(resId), defaultValue)
    }

    inline fun <reified T> put(@StringRes resId: Int, value: T) {
        default.put(App.instance.getString(resId), value)
    }

    inline fun <reified T> putFromRemote(@StringRes resId: Int, ignoreValue: T) {
        if (get(R.string.param_enable_remote_config)) {
            val remoteValue = remote.get<T>(App.instance.getString(resId))
            if (remoteValue != ignoreValue) {
                put(resId, remote.get<T>(App.instance.getString(resId)))
            }
        }
    }
}
