package hoo.etahk.common.tools

import android.annotation.SuppressLint
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.stetho.okhttp3.StethoInterceptor
import hoo.etahk.R
import hoo.etahk.common.Constants.AppMode
import hoo.etahk.common.Constants.Company
import hoo.etahk.common.Constants.NetworkType
import hoo.etahk.common.constants.SharePrefs
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.view.App.Companion.networkFlipperPlugin
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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

        val sslTrustManager = SSLTrustManager()
        val sslSocketFactory = createSSLSocketFactory(sslTrustManager)

        if (sslSocketFactory != null) {
            builder = builder.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(TrustAllHostnameVerifier())
        }

        builder = when (networkType) {
            NetworkType.DEFAULT -> builder
            NetworkType.LONG ->
                builder.connectTimeout(SharePrefs.LONG_CONNECTION_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                        .readTimeout(SharePrefs.LONG_READ_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
                        .writeTimeout(SharePrefs.LONG_WRITE_TIMEOUT_VAL, TimeUnit.MILLISECONDS)
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
        builder.addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", SharedPrefsHelper.get(R.string.param_user_agent, SharePrefs.DEFAULT_USER_AGENT))
                //.addHeader("User-Agent", WebSettings.getDefaultUserAgent(App.instance))
                .build()
            chain.proceed(request)
        }

        builder = when (SharedPrefsHelper.getAppMode()) {
            AppMode.DEV -> builder.addNetworkInterceptor(StethoInterceptor()).addNetworkInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
            else -> builder
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

    /**
     * Source: https://www.jianshu.com/p/cc7ae2f96b64
     */
    private fun createSSLSocketFactory(trustManager: TrustManager): SSLSocketFactory? {
        return try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf(trustManager), SecureRandom())
            sc.socketFactory
        } catch (e: Exception) {
            loge("createSSLSocketFactory Failed!", e)
            null
        }
    }

    @SuppressLint("TrustAllX509TrustManager")
    class SSLTrustManager : X509TrustManager {
        override fun checkClientTrusted(
            x509Certificates: Array<java.security.cert.X509Certificate>,
            s: String
        ) {
        }

        override fun checkServerTrusted(
            x509Certificates: Array<java.security.cert.X509Certificate>,
            s: String
        ) {
        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate?> {
            return arrayOfNulls(0)
        }
    }

    @SuppressLint("BadHostnameVerifier")
    private class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }
}
