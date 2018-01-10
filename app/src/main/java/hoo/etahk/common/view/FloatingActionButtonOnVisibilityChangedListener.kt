package hoo.etahk.common.view

import android.support.design.widget.FloatingActionButton
import android.view.View

/**
 * Source: http://stackoverflow.com/questions/41142711/25-1-0-android-support-lib-is-breaking-fab-behavior
 */
class FloatingActionButtonOnVisibilityChangedListener : FloatingActionButton.OnVisibilityChangedListener() {
    override fun onHidden(fab: FloatingActionButton?) {
        super.onHidden(fab)
        fab!!.visibility = View.INVISIBLE
    }
}
