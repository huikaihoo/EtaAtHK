package hoo.etahk.common.extensions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import getValue

val Activity.extras: Bundle
    get() = this.intent.extras ?: Bundle()

inline fun <reified T> Activity.getExtra(key: String): T =
    this.extras.getValue(key)

inline fun <reified T> Activity.getExtra(key: String, defaultValue: T): T =
    this.extras.getValue(key, defaultValue)

fun Activity.restart(intent: Intent) {
    this.overridePendingTransition(0, 0)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    this.finish()
    this.overridePendingTransition(0, 0)
    this.startActivity(intent)
}
