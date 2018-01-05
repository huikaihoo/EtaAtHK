package hoo.etahk.view.search

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import hoo.etahk.view.App
import hoo.etahk.view.test.SimpleFragment

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class BusSearchPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return SimpleFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return BusSearchActivity.searchList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return App.instance.getString(BusSearchActivity.titleList[position])
    }

//    override fun getItemPosition(`object`: Any): Int {
//        // Causes adapter to reload all Fragments when
//        // notifyDataSetChanged is called
//        return PagerAdapter.POSITION_NONE
//    }
}
