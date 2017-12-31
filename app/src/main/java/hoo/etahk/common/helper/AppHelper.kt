package hoo.etahk.common.helper

import android.arch.persistence.room.Room
import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hoo.etahk.common.Constants
import hoo.etahk.model.AppDatabase
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object AppHelper {
    lateinit var gson: Gson private set
    lateinit var db: AppDatabase private set
    lateinit var okHttp: OkHttpClient private set

    fun init() {
        gson = GsonBuilder()
                .serializeNulls()
                .create()
    }

    fun init(context: Context) {
        //gson = Gson()
        gson = GsonBuilder()
                .serializeNulls()
                .create()

        db = Room.databaseBuilder(context, AppDatabase::class.java, "db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = Constants.SharePrefs.DEFAULT_MAX_REQUESTS
        dispatcher.maxRequestsPerHost = Constants.SharePrefs.DEFAULT_MAX_REQUESTS_PER_HOST

        okHttp = when (SharedPrefsHelper.getAppMode()) {
            Constants.AppMode.DEV ->
                OkHttpClient().newBuilder()
                        .addNetworkInterceptor(StethoInterceptor())
                        .connectTimeout(Constants.SharePrefs.DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                        .dispatcher(dispatcher)
                        .build()
            else ->
                OkHttpClient().newBuilder()
                        .addNetworkInterceptor(StethoInterceptor())
                        .connectTimeout(Constants.SharePrefs.DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                        .dispatcher(dispatcher)
                        .build()
        }
    }
}