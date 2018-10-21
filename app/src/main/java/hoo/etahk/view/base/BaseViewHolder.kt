package hoo.etahk.view.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<in C, in D>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBind(context: C?, position: Int, dataSource: List<D>)

}