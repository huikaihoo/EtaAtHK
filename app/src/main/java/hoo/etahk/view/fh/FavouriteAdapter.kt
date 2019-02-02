package hoo.etahk.view.fh

import android.annotation.SuppressLint
import android.view.View
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.common.constants.Argument
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.diff.RouteFavouriteDiffCallback
import hoo.etahk.model.relation.RouteFavouriteEx
import hoo.etahk.view.base.BasePagedAdapter
import hoo.etahk.view.base.BaseViewHolder
import hoo.etahk.view.route.RouteActivity
import kotlinx.android.synthetic.main.item_route.view.*
import org.jetbrains.anko.startActivity

class FavouriteAdapter : BasePagedAdapter<FHActivity, RouteFavouriteEx>(RouteFavouriteDiffCallback()) {

    override fun getItemViewId(position: Int, dataSource: RouteFavouriteEx?): Int = R.layout.item_route

    override fun instantiateViewHolder(view: View, viewType: Int): ViewHolder = ViewHolder(view)

    class ViewHolder(itemView: View) : BaseViewHolder<FHActivity, RouteFavouriteEx>(itemView) {

        @SuppressLint("SetTextI18n")
        override fun onBind(context: FHActivity?, position: Int, dataSource: List<RouteFavouriteEx>) {
            val route = if (dataSource.isEmpty()) null else dataSource[0].route

            if (route == null) {
                val favourite = dataSource[0].favourite

                itemView.route_no.text = favourite.routeNo
                itemView.from_to.text = "NOT EXIST"
                itemView.route_desc.text = Utils.getStringResourceByName(favourite.company)
            } else {
                itemView.route_no.text = route.routeKey.routeNo
                itemView.from_to.text = route.from.value + route.getDirectionArrow() + route.to.value
                itemView.route_desc.text = route.getParentDesc()

                itemView.setOnClickListener { startRouteActivity(context, route.routeKey, route.anotherCompany) }
                //itemView.setOnLongClickListener { context?.showRoutePopupMenu(itemView, route); true }
            }
        }

        private fun startRouteActivity(context: FHActivity?, routeKey: RouteKey, anotherCompany: String = ""){
            context?.startActivity<RouteActivity>(
                Argument.ARG_COMPANY to routeKey.company,
                Argument.ARG_ROUTE_NO to routeKey.routeNo,
                Argument.ARG_TYPE_CODE to routeKey.typeCode,
                Argument.ARG_ANOTHER_COMPANY to anotherCompany,
                Argument.ARG_GOTO_BOUND to -1L,
                Argument.ARG_GOTO_SEQ to -1L
            )
        }
    }
}