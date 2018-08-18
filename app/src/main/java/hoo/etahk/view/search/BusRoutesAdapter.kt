package hoo.etahk.view.search

import android.annotation.SuppressLint
import android.view.View
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import hoo.etahk.R
import hoo.etahk.common.Constants
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.ParentRouteDiffCallback
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.base.FilterDiffAdapter
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.*
import org.jetbrains.anko.startActivity

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
            val route = dataSource[position]
            val directionArrow = App.instance.getString(
                    when (route.direction) {
                        0L -> R.string.arrow_circular
                        1L -> R.string.arrow_one_way
                        else -> R.string.arrow_two_ways
                    })

            itemView.route_no.text = route.routeKey.routeNo
            itemView.from_to.text = route.from.value + directionArrow + route.to.value
            itemView.route_desc.text = route.getParentDesc()

            itemView.setOnClickListener { startRouteActivity(context, route.routeKey) }
            itemView.setOnLongClickListener { context?.showRoutePopupMenu(itemView, route); true }
        }

        private fun startRouteActivity(context: BusSearchFragment?, routeKey: RouteKey){
            context?.activity?.startActivity<RouteActivity>(
                Constants.Argument.ARG_COMPANY to routeKey.company,
                Constants.Argument.ARG_ROUTE_NO to routeKey.routeNo,
                Constants.Argument.ARG_TYPE_CODE to routeKey.typeCode,
                Constants.Argument.ARG_GOTO_BOUND to -1L,
                Constants.Argument.ARG_GOTO_SEQ to -1L
            )
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
