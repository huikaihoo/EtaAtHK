package hoo.etahk.view.base

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

abstract class BasePagerAdapter<D>(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount() = dataSource.size

    fun getDataSource(position: Int) = dataSource[position]
}