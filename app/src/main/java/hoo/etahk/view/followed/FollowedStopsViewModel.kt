package hoo.etahk.view.followed

import android.arch.lifecycle.LiveData
import hoo.etahk.model.data.Stop
import hoo.etahk.model.repo.StopsRepo
import hoo.etahk.view.base.BaseViewModel

class FollowedStopsViewModel() : BaseViewModel() {
    private var mFollowedStops: LiveData<List<Stop>>? = null

    init {
        subscribeToRepo()
    }

    fun getFollowStops(): LiveData<List<Stop>> {
        return mFollowedStops!!
    }

    fun insertStops() {
        StopsRepo.insertStop()
    }

    fun updateEta(stops: List<Stop>) {
        StopsRepo.updateEta(stops)
    }

    fun updateAllEta() {
        if (mFollowedStops != null)
            StopsRepo.updateEta(mFollowedStops!!.value)
    }

    private fun subscribeToRepo() {
        mFollowedStops = StopsRepo.getFollowedStops()
    }
}