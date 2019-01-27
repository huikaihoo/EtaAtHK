package hoo.etahk.common.extensions

import android.app.Activity
import android.content.Intent
import getValue
import org.jetbrains.anko.internals.AnkoInternals

inline fun <reified T: Activity> Activity.startActivity(flags: Int, vararg params: Pair<String, Any?>) =
    startActivity(AnkoInternals.createIntent(this, T::class.java, params).addFlags(flags))

inline fun <reified T> Intent.getExtra(key: String): T =
    this.extras?.getValue(key) ?: ("" as T)

inline fun <reified T> Intent.getExtra(key: String, defaultValue: T): T =
    this.extras?.getValue(key, defaultValue) ?: defaultValue
