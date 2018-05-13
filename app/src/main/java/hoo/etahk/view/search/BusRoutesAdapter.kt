package hoo.etahk.view.search

import android.annotation.SuppressLint
import android.view.View
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.startActivity
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import hoo.etahk.R
import hoo.etahk.common.Constants.Argument
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.ParentRouteDiffCallback
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.base.FilterDiffAdapter
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.*

class BusRoutesAdapter : FilterDiffAdapter<BusSearchFragment, Route>(), FastScrollRecyclerView.SectionedAdapter {

    init {
        useDiff = false
    }

    override fun getDiffCallback(oldData: List<Route>, newData: List<Route>): BaseDiffCallback<Route> = ParentRouteDiffCallback(oldData, newData)

    override fun getItemViewId(position: Int, dataSource: List<Route>): Int = R.layout.item_route

    override fun instantiateViewHolder(view: View?, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View?) : BaseViewHolder<BusSearchFragment, Route>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: BusSearchFragment?, position: Int, dataSource: List<Route>) {
            val item = dataSource[position]
            val directionArrow = App.instance.getString(
                    when (item.direction) {
                        0L -> R.string.arrow_circular
                        1L -> R.string.arrow_one_way
                        else -> R.string.arrow_two_ways
                    })

            itemView.route_no.text = item.routeKey.routeNo
            itemView.from_to.text = item.from.value + directionArrow + item.to.value
            itemView.route_desc.text = item.getParentDesc()

            itemView.setOnClickListener { startRouteActivity(context, item.routeKey) }
        }

        private fun startRouteActivity(context: BusSearchFragment?, routeKey: RouteKey){
            context?.activity?.startActivity<RouteActivity>(Bundle {
                putString(Argument.ARG_COMPANY, routeKey.company)
                putString(Argument.ARG_ROUTE_NO, routeKey.routeNo)
                putLong(Argument.ARG_TYPE_CODE, routeKey.typeCode)
                putLong(Argument.ARG_GOTO_BOUND, -1L)
                putLong(Argument.ARG_GOTO_SEQ, -1L)
            })
        }
    }

    // For FastScroll
    override fun getSectionName(position: Int): String {
        return dataSource[position].routeKey.routeNo
    }

    // For Filtering
    override fun performFiltering(constraint: String): List<Route> {
        val result: MutableList<Route> = mutableListOf()

        if (constraint.isBlank()) {
            result.addAll(dataSource)
        } else {
            dataSource.forEach {
                if (it.routeKey.routeNo.contains(constraint))
                    result.add(it)
            }
        }

        return result.toList()
    }

    override fun scrollToPosition(position: Int) {
        context?.scrollToPosition(position)
    }
}
