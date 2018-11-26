package hoo.etahk.common.extensions

import android.app.Activity
import org.jetbrains.anko.internals.AnkoInternals

inline fun <reified T: Activity> Activity.startActivity(flags: Int, vararg params: Pair<String, Any?>) =
    startActivity(AnkoInternals.createIntent(this, T::class.java, params).addFlags(flags))