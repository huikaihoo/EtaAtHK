package hoo.etahk.common.tools

import com.facebook.stetho.okhttp3.StethoInterceptor
import hoo.etahk.common.Constants.AppMode
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.NetworkType
import hoo.etahk.common.Constants.SharePrefs
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ConnectionFactory {

    fun createClient(networkType: Long, company: String = ""): OkHttpClient {
        val dispatcher = Dispatcher()

        dispatcher.maxRequests = SharePrefs.DEFAULT_MAX_REQUESTS_VAL
        dispatcher.maxRequestsPerHost = when {
            networkType != NetworkType.ETA -> SharePrefs.DEFAULT_MAX_REQUESTS_PER_HOST_VAL
            company == Company.KMB -> SharePrefs.ETA_KMB_MAX_REQUESTS_PER_HOST_VAL
            company == Company.NWFB -> SharePrefs.ETA_NWFB_MAX_REQUESTS_PER_HOST_VAL
            else -> SharePrefs.DEFAULT_MAX_REQUESTS_PER_HOST_VAL
        }

        var builder = OkHttpClient().newBuilder().dispatcher(dispatcher)

        builder = when (networkType) {
            NetworkType.DEFAULT -> builder
            NetworkType.STOP ->
                builder.connectTimeout(SharePrefs.STOP_CONNECTION_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                        .readTimeout(SharePrefs.STOP_READ_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                        .writeTimeout(SharePrefs.STOP_WRITE_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
            NetworkType.ETA ->
                when (company) {
                    Company.KMB ->
                        builder.connectTimeout(SharePrefs.ETA_KMB_CONNECTION_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                                .readTimeout(SharePrefs.ETA_KMB_READ_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                                .writeTimeout(SharePrefs.ETA_KMB_WRITE_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                    Company.NWFB ->
                        builder.connectTimeout(SharePrefs.ETA_NWFB_CONNECTION_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                                .readTimeout(SharePrefs.ETA_NWFB_READ_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                                .writeTimeout(SharePrefs.ETA_NWFB_WRITE_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                    else -> builder
                }
            else -> builder
        }

        /**
         * Source: https://www.jianshu.com/p/4132b381f07e
         */
        builder.addInterceptor( { chain ->
            val request = chain.request()
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", SharePrefs.USER_AGENT)
                //.addHeader("User-Agent", WebSettings.getDefaultUserAgent(App.instance))
                .build()
            chain.proceed(request)
        } )

        builder = when (SharedPrefsHelper.getAppMode()) {
            AppMode.DEV -> builder.addNetworkInterceptor(StethoInterceptor())
            else -> builder.addNetworkInterceptor(StethoInterceptor())
        }

        return builder.build()
    }

    fun createRetrofit(client: OkHttpClient, url: String): Retrofit {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(AppHelper.gson))
                .build()
    }

    /**
     * Source: https://www.jianshu.com/p/d878daad0fbd
     */
    class RetryInterceptor(var maxRetry: Long) : Interceptor {

        private var retryCnt = 0L

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response = chain.proceed(request)

            while (!response.isSuccessful && retryCnt < maxRetry) {
                retryCnt++
                logd("retryCnt = $retryCnt")
                response = chain.proceed(request)
            }

            return response
        }
    }
}
