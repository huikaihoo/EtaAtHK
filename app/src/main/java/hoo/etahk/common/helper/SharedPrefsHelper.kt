package hoo.etahk.common.helper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import hoo.etahk.common.Constants

object SharedPrefsHelper {
    lateinit private var mSharedPreferences: SharedPreferences

    fun init(context: Context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getAppMode(): Long {
        return when (getString(Constants.Prefs.APP_MODE, "")) {
            "dev" -> Constants.AppMode.DEV
            "beta" -> Constants.AppMode.BETA
            else -> Constants.AppMode.RELEASE
        }
    }

    fun getString(key: String, defaultValue: String): String {
        return mSharedPreferences.getString(key, defaultValue)
    }
}