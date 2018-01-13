package hoo.etahk.view.search

import android.arch.lifecycle.MutableLiveData
import hoo.etahk.model.repo.RoutesRepo
import hoo.etahk.view.base.RefreshViewModel

class BusSearchViewModel : RefreshViewModel() {
    var searchText: MutableLiveData<String> = MutableLiveData()
    private var hasUpdateParentRoutes = false

    var selectedTabPosition = -1

    fun updateParentRoutes() {
        if (!hasUpdateParentRoutes) {
            hasUpdateParentRoutes = true
            RoutesRepo.updateParentRoutes()
        }
    }
}
