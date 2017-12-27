package hoo.etahk.common.helper

import android.arch.persistence.room.Room
import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hoo.etahk.common.Constants
import hoo.etahk.model.AppDatabase
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object AppHelper {
    lateinit var gson: Gson private set
    lateinit var db: AppDatabase private set
    lateinit var okHttp: OkHttpClient private set

    fun init(context: Context) {
        //gson = Gson()
        gson = GsonBuilder()
                .serializeNulls()
                .create()

        db = Room.databaseBuilder(context, AppDatabase::class.java, "db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()

        okHttp = when (SharedPrefsHelper.getAppMode()) {
            Constants.AppMode.DEV ->
                OkHttpClient().newBuilder()
                        .addNetworkInterceptor(StethoInterceptor())
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build()
            else ->
                OkHttpClient().newBuilder()
                        .addNetworkInterceptor(StethoInterceptor())
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build()
        }
    }
}