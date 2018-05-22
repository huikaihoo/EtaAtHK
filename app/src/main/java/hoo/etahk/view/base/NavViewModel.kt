package hoo.etahk.view.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.model.repo.RoutesRepo

class NavViewModel : ViewModel() {

    companion object {
        private const val TAG = "NavViewModel"
    }

    private var lastUpdate: LiveData<Long>? = null

    fun getLastUpdate(): LiveData<Long> {
        return lastUpdate!!
    }

    fun subscribeToRepo() {
        if (lastUpdate == null) {
            lastUpdate = RoutesRepo.getLastUpdate()
        }
    }

}