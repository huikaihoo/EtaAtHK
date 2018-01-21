package hoo.etahk.view.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.common.Constants
import hoo.etahk.model.repo.RoutesRepo

class BusSearchViewModel : ViewModel() {
    val searchText: MutableLiveData<String> = MutableLiveData()
    private var hasUpdateParentRoutes = false

    var selectedTabPosition = -1

    fun updateParentRoutes() {
        if (!hasUpdateParentRoutes) {
            hasUpdateParentRoutes = true
            RoutesRepo.updateParentRoutes(Constants.Company.BUS)
        }
    }
}
