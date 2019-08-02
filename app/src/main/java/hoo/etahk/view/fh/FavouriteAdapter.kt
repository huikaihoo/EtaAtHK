package hoo.etahk.view.fh

import android.annotation.SuppressLint
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.common.constants.SharePrefs
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.RouteFavouriteDiffCallback
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.view.base.BasePagedAdapter
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.from_to
import kotlinx.android.synthetic.main.item_route.view.route_desc
import kotlinx.android.synthetic.main.item_route.view.route_no
import org.jetbrains.anko.startActivity

class FavouriteAdapter : BasePagedAdapter<FHActivity, RouteFavouriteEx>(RouteFavouriteDiffCallback()) {

    override fun getItemViewId(position: Int, dataSource: RouteFavouriteEx?): Int = R.layout.item_route

    override fun instantiateViewHolder(view: View, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View) : BaseViewHolder<FHActivity, RouteFavouriteEx>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: FHActivity?, position: Int, dataSource: List<RouteFavouriteEx>) {
            val route = if (dataSource.isEmpty()) null else dataSource[0].route
            val favourite = dataSource[0].favourite

            if (route == null) {
                itemView.route_no.text = favourite.routeNo
                itemView.from_to.text = "NOT EXIST"
                itemView.route_desc.text = Utils.getStringResourceByName(favourite.company)
            } else {
                itemView.route_no.text = route.routeKey.routeNo
                itemView.from_to.text = route.from.value + route.getDirectionArrow() + route.to.value
                itemView.route_desc.text = route.getParentDesc()

                itemView.setOnClickListener {
                    if (route.companyDetails.size > 1) {
                        when (SharedPrefsHelper.get<String>(R.string.pref_bus_jointly)) {
                            SharePrefs.BUS_JOINTLY_DEFAULT_KMB_LWB -> startRouteActivity(context, route.routeKey, route.routeKey.company, route.anotherCompany)
                            SharePrefs.BUS_JOINTLY_DEFAULT_NWFB_CTB -> startRouteActivity(context, route.routeKey, route.anotherCompany, route.routeKey.company)
                            else -> context?.showCompaniesPopupMenu(itemView, route)
                        }
                    } else {
                        startRouteActivity(context, route.routeKey, route.routeKey.company, route.anotherCompany)
                    }
                }
                itemView.setOnLongClickListener { context?.showRoutePopupMenu(itemView, route, favourite); true }
            }
        }

        private fun startRouteActivity(context: FHActivity?, routeKey: RouteKey, company: String, anotherCompany: String = ""){
            context?.startActivity<RouteActivity>(
                Argument.ARG_COMPANY to company,
                Argument.ARG_ROUTE_NO to routeKey.routeNo,
                Argument.ARG_TYPE_CODE to routeKey.typeCode,
                Argument.ARG_ANOTHER_COMPANY to anotherCompany,
                Argument.ARG_GOTO_BOUND to -1L,
                Argument.ARG_GOTO_SEQ to -1L
            )
        }
    }
}