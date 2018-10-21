package hoo.etahk.view.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hoo.etahk.common.Constants
import hoo.etahk.transfer.repo.RoutesRepo

class BusSearchViewModel : ViewModel() {
    val searchText: MutableLiveData<String> = MutableLiveData()
    private var hasUpdateParentRoutes = false

    var selectedTabPosition: Int = -1

    fun updateParentRoutes() {
        if (!hasUpdateParentRoutes) {
            hasUpdateParentRoutes = true
            RoutesRepo.updateParentRoutes(Constants.Company.BUS)
        }
    }
}
