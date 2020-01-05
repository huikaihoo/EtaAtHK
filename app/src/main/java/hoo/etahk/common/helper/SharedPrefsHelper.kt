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
import hoo.etahk.common.Utils
import put
import java.util.UUID


object SharedPrefsHelper {
    lateinit var default: SharedPreferences
    @SuppressLint("StaticFieldLeak")
    lateinit var remote: FirebaseRemoteConfig

    private val minimumFetchInterval
        get() = if (getAppMode() == DEV) 10L else 43200L

    fun init(context: Context) {
        default = PreferenceManager.getDefaultSharedPreferences(context)

        if (!Utils.isUnitTest) {
            remote = FirebaseRemoteConfig.getInstance()

            remote.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(minimumFetchInterval)
                .build())
            remote.setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }

    fun getAppMode(): Long {
        return when (BuildConfig.DEBUG) {
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

    fun getUserUUID(): String {
        var uuid = get<String>(R.string.param_user_uuid)
        if (uuid.isBlank()) {
            uuid = UUID.randomUUID().toString()
            put(R.string.param_user_uuid, uuid)
        }
        return uuid
    }

    inline fun <reified T> get(@StringRes resId: Int): T {
        return default.get(AppHelper.getString(resId))
    }

    inline fun <reified T> get(@StringRes resId: Int, defaultValue: T): T {
        return default.get(AppHelper.getString(resId), defaultValue)
    }

    inline fun <reified T> put(@StringRes resId: Int, value: T) {
        default.put(AppHelper.getString(resId), value)
    }

    inline fun <reified T> putFromRemote(@StringRes resId: Int, ignoreValue: T) {
        if (get(R.string.param_enable_remote_config, true)) {
            val remoteValue = remote.get<T>(AppHelper.getString(resId))
            if (T::class == Boolean::class || remoteValue != ignoreValue) {
                put(resId, remote.get<T>(AppHelper.getString(resId)))
            }
        }
    }
}
