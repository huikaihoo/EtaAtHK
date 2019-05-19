package hoo.etahk.view.search

import android.annotation.SuppressLint
import android.view.View
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import hoo.etahk.R
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.BaseDiffCallback
import hoo.etahk.model.diff.ParentRouteDiffCallback
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

    override fun instantiateViewHolder(view: View, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View) : BaseViewHolder<BusSearchFragment, Route>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: BusSearchFragment?, position: Int, dataSource: List<Route>) {
            val route = dataSource[position]

            itemView.route_no.text = route.routeKey.routeNo
            itemView.from_to.text = route.from.value + route.getDirectionArrow() + route.to.value
            itemView.route_desc.text = route.getParentDesc()

            itemView.setOnClickListener {
                if (route.companyDetails.size > 1) {
                    when (SharedPrefsHelper.get<String>(R.string.pref_bus_jointly)) {
                        "0" -> startRouteActivity(context, route.routeKey, route.routeKey.company, route.anotherCompany)
                        "1" -> startRouteActivity(context, route.routeKey, route.anotherCompany, route.routeKey.company)
                        "2" -> context?.showCompaniesPopupMenu(itemView, route)
                    }
                } else {
                    startRouteActivity(context, route.routeKey, route.routeKey.company, route.anotherCompany)
                }
            }
            itemView.setOnLongClickListener { context?.showRoutePopupMenu(itemView, route); true }
        }

        private fun startRouteActivity(context: BusSearchFragment?, routeKey: RouteKey, company: String, anotherCompany: String = ""){
            context?.activity?.startActivity<RouteActivity>(
                Argument.ARG_COMPANY to company,
                Argument.ARG_ROUTE_NO to routeKey.routeNo,
                Argument.ARG_TYPE_CODE to routeKey.typeCode,
                Argument.ARG_ANOTHER_COMPANY to anotherCompany,
                Argument.ARG_GOTO_BOUND to -1L,
                Argument.ARG_GOTO_SEQ to -1L
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
