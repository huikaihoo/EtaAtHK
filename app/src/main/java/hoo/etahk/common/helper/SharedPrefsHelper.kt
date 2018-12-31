package hoo.etahk.common.helper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import get
import hoo.etahk.BuildConfig
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.view.App
import put

object SharedPrefsHelper {
    lateinit var default: SharedPreferences

    fun init(context: Context) {
        default = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getAppMode(): Long {
        return when(BuildConfig.DEBUG) {
            true -> Constants.AppMode.DEV
            false -> {
                when (get<String>(R.string.param_app_mode)) {
                    "dev" -> Constants.AppMode.DEV
                    "beta" -> Constants.AppMode.BETA
                    else -> Constants.AppMode.RELEASE
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
}
