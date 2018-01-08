package hoo.etahk.common.helper

import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import hoo.etahk.common.Constants
import hoo.etahk.model.AppDatabase
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException


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
                        //.addInterceptor(RetryInterceptor(Constants.SharePrefs.DEFAULT_MAX_RETRY_ON_FAILED))
                        //.connectTimeout(Constants.SharePrefs.DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                        //.readTimeout(Constants.SharePrefs.DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                        //.writeTimeout(Constants.SharePrefs.DEFAULT_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                        .dispatcher(dispatcher)
                        .build()
            else ->
                OkHttpClient().newBuilder()
                        .addNetworkInterceptor(StethoInterceptor())
                        //.addInterceptor(RetryInterceptor(Constants.SharePrefs.DEFAULT_MAX_RETRY_ON_FAILED))
                        //.connectTimeout(Constants.SharePrefs.DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                        //.readTimeout(Constants.SharePrefs.DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                        //.writeTimeout(Constants.SharePrefs.DEFAULT_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                        .dispatcher(dispatcher)
                        .build()
        }
    }

    /**
     * Source: https://www.jianshu.com/p/d878daad0fbd
     */
    class RetryInterceptor(var maxRetry: Long) : Interceptor {

        companion object {
            private const val TAG = "RetryIntercepter"
        }

        private var retryCnt = 0L

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response = chain.proceed(request)

            while (!response.isSuccessful && retryCnt < maxRetry) {
                retryCnt++
                Log.d(TAG, "retryCnt = $retryCnt")
                response = chain.proceed(request)
            }

            return response
        }
    }
}

