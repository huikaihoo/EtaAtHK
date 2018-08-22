package hoo.etahk.view.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.transfer.repo.RoutesRepo

class NavViewModel : ViewModel() {

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