package hoo.etahk.view.route

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import hoo.etahk.R
import hoo.etahk.common.Utils
import hoo.etahk.model.data.Route

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class RoutePagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    var dataSource: Route? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return RouteFragment.newInstance((position + 1).toLong())
    }

    override fun getCount(): Int {
        return dataSource?.boundCount?.toInt()?: 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return Utils.getString(R.string.to_prefix) +
                when (position) {
                    0 -> dataSource!!.to.value
                    1 -> dataSource!!.from.value
                    else -> ""
                }
    }
}