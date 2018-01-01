package hoo.etahk.view.route

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.ViewGroup
import hoo.etahk.R
import hoo.etahk.model.data.Route
import hoo.etahk.view.App

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
        return App.instance.getString(R.string.to) +
                when(position) {
                    0 -> dataSource!!.to.value
                    1 -> dataSource!!.from.value
                    else -> ""
                }
    }

    override fun getItemPosition(`object`: Any): Int {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return PagerAdapter.POSITION_NONE
    }

    /**
     * Here we can finally safely save a reference to the created
     * Fragment, no matter where it came from (either getItem() or
     * FragmentManger). Simply save the returned Fragment from
     * super.instantiateItem() into an appropriate reference depending
     * on the ViewPager position.
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val createdFragment = super.instantiateItem(container, position)
        return createdFragment
    }
}