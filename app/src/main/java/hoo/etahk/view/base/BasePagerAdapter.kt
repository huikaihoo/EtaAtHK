package hoo.etahk.view.base

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

abstract class BasePagerAdapter<D>(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var dataSource: List<D> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount() = dataSource.size

    fun getDataSource(position: Int) = dataSource[position]
}