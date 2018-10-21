package hoo.etahk.view.follow

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.widget.ThemedSpinnerAdapter
import android.view.View
import android.view.ViewGroup
import hoo.etahk.R
import hoo.etahk.model.relation.LocationAndGroups
import hoo.etahk.view.base.BaseArrayAdapter
import kotlinx.android.synthetic.main.item_spinner_toolbar.view.*

class FollowSpinnerAdapter(context: Context): BaseArrayAdapter<LocationAndGroups>(context, R.layout.item_spinner_toolbar) {
    private val dropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            val inflater = dropDownHelper.dropDownViewInflater
            view = inflater.inflate(R.layout.item_spinner_toolbar, parent, false)
        } else {
            view = convertView
        }

        view.title.text = getItem(position).location.name

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

