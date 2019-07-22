package hoo.etahk.common.extensions

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import hoo.etahk.view.App
import kotlin.math.roundToInt

fun TextView.prependImage(@DrawableRes resId: Int, spannableStringBuilder: SpannableStringBuilder): SpannableStringBuilder =
    prependImageToString((this.lineHeight * 0.8).roundToInt(), this.currentTextColor, resId, spannableStringBuilder)

private fun prependImageToString(size: Int, color: Int, @DrawableRes resId: Int, spannableStringBuilder: SpannableStringBuilder): SpannableStringBuilder {
    var drawable = ContextCompat.getDrawable(App.instance, resId)
    if (drawable != null) {
        drawable = DrawableCompat.wrap(drawable)
        drawable.setBounds(0, 0, size, size)
        DrawableCompat.setTint(drawable.mutate(), color)

        val imageSpan = ImageSpan(drawable!!, ImageSpan.ALIGN_BASELINE)
        val originalLength = spannableStringBuilder.length
        spannableStringBuilder.append("  ")
        spannableStringBuilder.setSpan(imageSpan, originalLength, originalLength + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    return spannableStringBuilder
}
