package hoo.etahk.common.extensions

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.IconCompat
import hoo.etahk.common.Utils.getThemeColorPrimary
import hoo.etahk.common.browser.CustomTabsHelper

/**
 * Method to launch a Custom Tabs Activity.
 * @param url The URL to load in the Custom Tab.
 */
fun Context.startCustomTabs(url: String) = startCustomTabs(this, url, getThemeColorPrimary(this))

/**
 * Method to launch a Custom Tabs Activity.
 * @param context The source Context.
 * @param url The URL to load in the Custom Tab.
 * @param color The color of Toolbar
 */
private fun startCustomTabs(context: Context, url: String, @ColorInt color: Int) {
    val packageName = CustomTabsHelper.getPackageNameToUse(context)

    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.setShowTitle(true)
        .addDefaultShareMenuItem()
        .enableUrlBarHiding()
        .setToolbarColor(color)
        .build()

    if (packageName != null) {
        customTabsIntent.intent.setPackage(packageName)
    }

    customTabsIntent.launchUrl(context, Uri.parse(url))
}


fun Context.createShortcut(shortcutId: String, @StringRes shortLabelResId: Int, @StringRes longLabelResId: Int, @DrawableRes iconResId: Int, intent: Intent) =
    createShortcut(this, shortcutId, this.getString(shortLabelResId), this.getString(longLabelResId), iconResId, intent)

fun Context.createShortcut(shortcutId: String, label: String, @DrawableRes iconResId: Int, intent: Intent) =
    createShortcut(this, shortcutId, label, label, iconResId, intent)

/**
 * Source: https://developer.android.com/guide/topics/ui/shortcuts/creating-shortcuts
 */
private fun createShortcut(context: Context, shortcutId: String, shortLabel: String, longLabel: String, @DrawableRes iconResId: Int, intent: Intent) {
    intent.action = Intent.ACTION_VIEW

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager

        //check if device supports Pin Shortcut or not
        if (shortcutManager.isRequestPinShortcutSupported) {

            val pinShortcutInfo =
                ShortcutInfo.Builder(context, shortcutId)
                    .setShortLabel(shortLabel)
                    .setLongLabel(longLabel)
                    .setIcon(Icon.createWithResource(context, iconResId))
                    .setIntent(intent)
                    .build()

            // Create the PendingIntent object only if your app needs to be notified
            // that the user allowed the shortcut to be pinned. Note that, if the
            // pinning operation fails, your app isn't notified. We assume here that the
            // app has implemented a method called createShortcutResultIntent() that
            // returns a broadcast intent.
            val pinnedShortcutCallbackIntent =
                shortcutManager.createShortcutResultIntent(pinShortcutInfo)

            // Configure the intent so that your app's broadcast receiver gets
            // the callback successfully.
            val successCallback =
                PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent, 0)

            //finally ask user to add the shortcut to home screen
            shortcutManager.requestPinShortcut(
                pinShortcutInfo,
                successCallback.intentSender
            )
        }
    } else {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val pinShortcutInfo =
                ShortcutInfoCompat.Builder(context, shortcutId)
                    .setShortLabel(shortLabel)
                    .setLongLabel(longLabel)
                    .setIcon(IconCompat.createWithResource(context, iconResId))
                    .setIntent(intent)
                    .build()

            // Create the PendingIntent object only if your app needs to be notified
            // that the user allowed the shortcut to be pinned. Note that, if the
            // pinning operation fails, your app isn't notified. We assume here that the
            // app has implemented a method called createShortcutResultIntent() that
            // returns a broadcast intent.
            val pinnedShortcutCallbackIntent =
                ShortcutManagerCompat.createShortcutResultIntent(context, pinShortcutInfo)

            // Configure the intent so that your app's broadcast receiver gets
            // the callback successfully.
            val successCallback =
                PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent, 0)

            //finally ask user to add the shortcut to home screen
            ShortcutManagerCompat.requestPinShortcut(
                context,
                pinShortcutInfo,
                successCallback.intentSender
            )
        }
    }
}


/**
 * Source: https://stackoverflow.com/questions/33696488/getting-bitmap-from-vector-drawable
 */
fun Context.getBitmapFromVectorDrawable(@DrawableRes resId: Int): Bitmap {
    var drawable = ContextCompat.getDrawable(this, resId)
    drawable = DrawableCompat.wrap(drawable!!).mutate()

    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}
