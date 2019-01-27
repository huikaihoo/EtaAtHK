package hoo.etahk.common.extensions

import android.app.Activity
import android.os.Bundle
import getValue

val Activity.extras: Bundle
    get() = this.intent.extras ?: Bundle()

inline fun <reified T> Activity.getExtra(key: String): T =
    this.extras.getValue(key)

inline fun <reified T> Activity.getExtra(key: String, defaultValue: T): T =
    this.extras.getValue(key, defaultValue)
