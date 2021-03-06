package hoo.etahk.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ThemedSpinnerAdapter
import hoo.etahk.R
import hoo.etahk.model.relation.RouteAndStops
import hoo.etahk.view.base.BaseArrayAdapter
import kotlinx.android.synthetic.main.item_spinner_multiple.view.subtitle
import kotlinx.android.synthetic.main.item_spinner_multiple.view.title

class RoutesSpinnerAdapter(context: Context): BaseArrayAdapter<RouteAndStops>(context, R.layout.item_spinner_multiple) {
    private val dropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        view = if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            val inflater = dropDownHelper.dropDownViewInflater
            inflater.inflate(R.layout.item_spinner_multiple, parent, false)
        } else {
            convertView
        }

        val route = getItem(position).route

        // TODO("Add Alert icon to variant")
        view.title.text = route.from.value + route.getDirectionArrow() + route.to.value
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

