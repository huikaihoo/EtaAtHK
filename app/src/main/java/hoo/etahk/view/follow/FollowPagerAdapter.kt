package hoo.etahk.view.follow

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import hoo.etahk.R
import hoo.etahk.common.helper.AppHelper
import hoo.etahk.model.relation.LocationAndGroups


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class FollowPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
    var dataSource: LocationAndGroups? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return FollowFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return dataSource?.groups?.size?: 0
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ( if (dataSource!!.location.pin) "" else AppHelper.getString(R.string.to_prefix) ) + dataSource!!.groups[position].name
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}