package hoo.etahk.view.base

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class BaseViewHolder<in C, in D>(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBind(context: C?, position: Int, dataSource: List<D>)

}