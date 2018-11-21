package hoo.etahk.common.helper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import hoo.etahk.BuildConfig
import hoo.etahk.common.Constants

object SharedPrefsHelper {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getAppMode(): Long {
        return when(BuildConfig.DEBUG) {
            true -> Constants.AppMode.DEV
            false -> {
                when (getString(Constants.Prefs.APP_MODE, "")) {
                    "dev" -> Constants.AppMode.DEV
                    "beta" -> Constants.AppMode.BETA
                    else -> Constants.AppMode.RELEASE
                }
            }
        }
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
}