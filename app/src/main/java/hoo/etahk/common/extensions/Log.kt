package hoo.etahk.common.extensions

import android.util.Log
import com.crashlytics.android.Crashlytics

/**
 * Sources: https://github.com/Kotlin/anko/blob/27fba1aa00811470f58ec6b61851e1cd8c2d9999/anko/library/static/commons/src/main/java/Logging.kt
 */
fun kotlin.Any.tag(): String {
    val tag = javaClass.simpleName
    return if (tag.length <= 23) tag else tag.substring(0, 23)
}

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.v].
 */
fun kotlin.Any.logv(message: Any?, thr: Throwable? = null) {
    if (thr != null) {
        Log.v(tag(), message?.toString() ?: "null", thr)
    } else {
        Log.v(tag(), message?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.d].
 */
fun kotlin.Any.logd(message: Any?, thr: Throwable? = null) {
    if (thr != null) {
        Log.d(tag(), message?.toString() ?: "null", thr)
    } else {
        Log.d(tag(), message?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO]
 *   (it is the default level).
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.i].
 */
fun kotlin.Any.logi(message: Any?, thr: Throwable? = null) {
    if (thr != null) {
        Log.i(tag(), message?.toString() ?: "null", thr)
    } else {
        Log.i(tag(), message?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.w].
 */
fun kotlin.Any.logw(message: Any?, thr: Throwable? = null) {
    if (thr != null) {
        Log.w(tag(), message?.toString() ?: "null", thr)
    } else {
        Log.w(tag(), message?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.e].
 */
fun kotlin.Any.loge(message: Any?, thr: Throwable? = null) {
    if (thr != null) {
        Log.e(tag(), message?.toString() ?: "null", thr)
        Crashlytics.logException(thr)
    } else {
        Crashlytics.log(Log.ERROR, tag(), message?.toString() ?: "null")
    }
}

/**
 * Send a log message with the "What a Terrible Failure" severity.
 * Report an exception that should never happen.
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.wtf].
 */
fun kotlin.Any.logwtf(message: Any?, thr: Throwable? = null) {
    if (thr != null) {
        Log.wtf(tag(), message?.toString() ?: "null", thr)
    } else {
        Log.wtf(tag(), message?.toString() ?: "null")
    }
}

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.v].
 */
inline fun kotlin.Any.logv(message: () -> Any?) {
    Log.v(tag(), message()?.toString() ?: "null")
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.d].
 */
inline fun kotlin.Any.logd(message: () -> Any?) {
    Log.d(tag(), message()?.toString() ?: "null")
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.i].
 */
inline fun kotlin.Any.logi(message: () -> Any?) {
    Log.i(tag(), message()?.toString() ?: "null")
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.w].
 */
inline fun kotlin.Any.logw(message: () -> Any?) {
    Log.v(tag(), message()?.toString() ?: "null")
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.e].
 */
inline fun kotlin.Any.loge(message: () -> Any?) {
    Crashlytics.log(Log.ERROR, tag(), message()?.toString() ?: "null")
}
