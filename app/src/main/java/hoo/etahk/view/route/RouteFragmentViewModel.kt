package hoo.etahk.view.route

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.common.Constants
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.repo.RoutesRepo
import hoo.etahk.model.repo.StopsRepo

class RouteFragmentViewModel : ViewModel() {
    private var mChildRoutes: LiveData<List<Route>>? = null
    private var mStops: LiveData<List<Stop>>? = null
    private var lastUpdateEtaTime = 0L
    private var hasUpdateStops = false

    var routeKey: RouteKey? = null
        set(value) {
            field = value
            if (value != null)
                subscribeChildRoutesToRepo()
        }

    var etaStatus = Constants.EtaStatus.LOADING

    fun getChildRoutes(): LiveData<List<Route>> {
        return mChildRoutes!!
    }

    fun getStops(): LiveData<List<Stop>> {
        return mStops!!
    }

    fun updateStops(childRoutes: List<Route>, needEtaUpdate: Boolean = true) {
        if (!hasUpdateStops && childRoutes.isNotEmpty()) {
            hasUpdateStops = true
            StopsRepo.updateStops(childRoutes[0], needEtaUpdate)
        }
    }

    fun updateEta(stops: List<Stop>) {
        StopsRepo.updateEta(stops)
    }

    fun updateAllEta(time: Long) {
        //Log.d("XXX", "$lastUpdateEtaTime $time")
        if (mStops != null && lastUpdateEtaTime < time){
            lastUpdateEtaTime = time
            etaStatus = Constants.EtaStatus.LOADING
            StopsRepo.updateEta(mStops!!.value)
        }
    }

    private fun subscribeChildRoutesToRepo() {
        mChildRoutes = RoutesRepo.getChildRoutes(routeKey!!.company, routeKey!!.routeNo, routeKey!!.bound)
    }

    fun subscribeStopsToRepo() {
        // TODO("Support variant")
        mStops = StopsRepo.getStops(mChildRoutes!!.value!![0])
    }
}