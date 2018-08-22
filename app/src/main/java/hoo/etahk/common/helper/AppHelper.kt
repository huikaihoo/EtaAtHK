package hoo.etahk.common.helper

import android.arch.persistence.room.Room
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hoo.etahk.common.Constants.DATABASE_NAME
import hoo.etahk.model.AppDatabase


object AppHelper {
    lateinit var gson: Gson private set
    lateinit var db: AppDatabase private set
    //lateinit var okHttp: OkHttpClient private set

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

        db = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()

//        val dispatcher = Dispatcher()
//        dispatcher.maxRequests = Constants.SharePrefs.DEFAULT_MAX_REQUESTS_VAL
//        dispatcher.maxRequestsPerHost = Constants.SharePrefs.DEFAULT_MAX_REQUESTS_PER_HOST_VAL
//
//        okHttp = when (SharedPrefsHelper.getAppMode()) {
//            Constants.AppMode.DEV ->
//                OkHttpClient().newBuilder()
//                        .addNetworkInterceptor(StethoInterceptor())
//                        .dispatcher(dispatcher)
//                        .build()
//            else ->
//                OkHttpClient().newBuilder()
//                        .addNetworkInterceptor(StethoInterceptor())
//                        .dispatcher(dispatcher)
//                        .build()
//        }
    }
}

