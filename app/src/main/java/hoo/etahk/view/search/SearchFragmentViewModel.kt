package hoo.etahk.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.transfer.repo.MiscRepo
import hoo.etahk.transfer.repo.RoutesRepo

class SearchFragmentViewModel : ViewModel() {
    private var parentRoutes: LiveData<List<Route>>? = null

    var config: SearchTabConfig? = null
        set(value) {
            field = value
            if (value != null)
                subscribeToRepo()
        }

    fun getParentRoutes(): LiveData<List<Route>> {
        return parentRoutes!!
    }

    fun insertRouteFavourite(routeKey: RouteKey, anotherCompany: String) {
        MiscRepo.insertRouteFavourite(routeKey, if (anotherCompany.isBlank()) null else anotherCompany)
    }

    private fun subscribeToRepo() {
        parentRoutes = RoutesRepo.getParentRoutes(config!!.routeTypes, config!!.orderBy)
    }
}