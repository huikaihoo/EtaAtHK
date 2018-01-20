package hoo.etahk.view.base

import android.content.Context
import android.widget.ArrayAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

abstract class BaseArrayAdapter<D>(context: Context, resource: Int): ArrayAdapter<D>(context, resource) {
    open var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            launch(UI) {
                super.clear()
                super.addAll(dataSource)
                notifyDataSetChanged()
            }
        }
}
