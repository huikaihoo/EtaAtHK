package hoo.etahk.view.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.model.data.Route
import hoo.etahk.model.repo.RoutesRepo

class BusSearchFragmentViewModel : ViewModel() {
    private var mParentRoutes: LiveData<List<Route>>? = null

    var index: Int = -1
        set(value) {
            field = value
            if (value >= 0)
                subscribeToRepo()
        }

    fun getParentRoutes(): LiveData<List<Route>> {
        return mParentRoutes!!
    }

    private fun subscribeToRepo() {
        val config = BusSearchActivity.searchList[index]
        mParentRoutes = RoutesRepo.getParentRoutes(config.routeTypes, config.orderBy)
    }
}