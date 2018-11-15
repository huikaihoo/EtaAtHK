package hoo.etahk.view.base

import android.content.Context
import android.widget.ArrayAdapter
import kotlinx.coroutines.*

abstract class BaseArrayAdapter<D>(context: Context, resource: Int): ArrayAdapter<D>(context, resource) {
    open var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            GlobalScope.launch(Dispatchers.Main) {
                super.clear()
                super.addAll(dataSource)
                notifyDataSetChanged()
            }
        }
}
