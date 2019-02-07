package hoo.etahk.common.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.common.Utils
import hoo.etahk.common.extensions.applyLocale
import hoo.etahk.common.extensions.yn
import hoo.etahk.model.AppDatabase
import java.util.*


@SuppressLint("StaticFieldLeak")
object AppHelper {
    private lateinit var languageContext: Context

    lateinit var gson: Gson private set
    lateinit var db: AppDatabase private set
    lateinit var notificationManager: NotificationManagerCompat private set

    fun init(context: Context) {
        languageContext = context

        gson = GsonBuilder()
            .serializeNulls()
            .create()

        db = when (Utils.isUnitTest) {
            true ->
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            false ->
                Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
        }

        notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelList = listOf(
                NotificationChannel(
                    context.getString(R.string.nc_id_update_routes),
                    context.getString(R.string.nc_name_update_routes),
                    NotificationManager.IMPORTANCE_LOW
                )
            )

            val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            manager?.createNotificationChannels(channelList)
        }
    }

    fun applyAppLocale(language: String) {
        languageContext = languageContext.applyLocale((language.isBlank()).yn(Locale.getDefault().language, language))
    }

    fun getString(@StringRes resId: Int): String {
        return languageContext.getString(resId)
    }
}

