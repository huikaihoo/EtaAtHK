package hoo.etahk.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.ThemedSpinnerAdapter
import android.view.View
import android.view.ViewGroup
import hoo.etahk.R
import hoo.etahk.model.relation.RouteAndStops
import hoo.etahk.view.App
import hoo.etahk.view.base.BaseArrayAdapter
import kotlinx.android.synthetic.main.item_spinner_small.view.*

class RoutesSpinnerAdapter(context: Context): BaseArrayAdapter<RouteAndStops>(context, R.layout.item_spinner_small) {
    private val dropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            val inflater = dropDownHelper.dropDownViewInflater
            view = inflater.inflate(R.layout.item_spinner_small, parent, false)
        } else {
            view = convertView
        }

        val route = getItem(position).route!!

        val directionArrow = App.Companion.instance.getString(
            when (route.direction) {
                0L -> R.string.arrow_circular
                else -> R.string.arrow_one_way
            })

        // TODO("Add Alert icon to variant")
        view.title.text = route.from.value + directionArrow + route.to.value
        view.subtitle.text = route.details.value

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    override fun getDropDownViewTheme(): Resources.Theme? {
        return dropDownHelper.dropDownViewTheme
    }

    override fun setDropDownViewTheme(theme: Resources.Theme?) {
        dropDownHelper.dropDownViewTheme = theme
    }
}

