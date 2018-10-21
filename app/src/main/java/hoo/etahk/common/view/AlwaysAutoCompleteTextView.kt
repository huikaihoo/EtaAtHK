package hoo.etahk.common.view

import android.content.Context
import android.graphics.Rect
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import android.util.AttributeSet


/**
 * Source: https://stackoverflow.com/questions/2126717/android-autocompletetextview-show-suggestions-when-no-text-entered
 */
class AlwaysAutoCompleteTextView : AppCompatAutoCompleteTextView {

    private var isInit = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun enoughToFilter(): Boolean = true

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!isInit) {
            if (focused && adapter != null) {
                if (!isInit) {
                    performFiltering(text, 0)
                }
            }
        } else {
            isInit = false
        }
    }
}
