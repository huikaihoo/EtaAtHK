package hoo.etahk.common.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatAutoCompleteTextView;


/**
 * Source: https://stackoverflow.com/questions/2126717/android-autocompletetextview-show-suggestions-when-no-text-entered
 * TODO("Convert to Kotlin")
 */
public class AlwaysAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    private boolean isInit = true;

    public AlwaysAutoCompleteTextView(Context context) {
        super(context);
    }

    public AlwaysAutoCompleteTextView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public AlwaysAutoCompleteTextView(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!isInit) {
            if (focused && getAdapter() != null) {
                if (!isInit) {
                    performFiltering(getText(), 0);
                }
            }
        } else {
            isInit = false;
        }
    }
}
