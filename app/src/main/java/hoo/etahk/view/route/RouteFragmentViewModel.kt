package hoo.etahk.view.route

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.repo.RoutesRepo
import hoo.etahk.model.repo.StopsRepo

class RouteFragmentViewModel : ViewModel() {
    private var mChildRoutes: LiveData<List<Route>>? = null
    private var mStops: LiveData<List<Stop>>? = null
    private var mlastUpdateEtaTime = 0L

    var hasGetStopsFromRemote = false

    var routeKey: RouteKey? = null
        set(value) {
            field = value
            if (value != null)
                subscribeChildRoutesToRepo()
        }

    fun getChildRoutes(): LiveData<List<Route>> {
        return mChildRoutes!!
    }

    fun getStops(): LiveData<List<Stop>> {
        return mStops!!
    }

    fun updateEta(stops: List<Stop>) {
        StopsRepo.updateEta(stops)
    }

    fun updateAllEta(time: Long) {
        //Log.d("XXX", "$mlastUpdateEtaTime $time")
        if (mStops != null && mlastUpdateEtaTime < time){
            mlastUpdateEtaTime = time
            StopsRepo.updateEta(mStops!!.value)
        }
    }

    private fun subscribeChildRoutesToRepo() {
        mChildRoutes = RoutesRepo.getChildRoutes(routeKey!!.company, routeKey!!.routeNo, routeKey!!.bound)
        //mStops = StopsRepo.getStops()
    }

    fun subscribeStopsToRepo() {
        // TODO("Support variant")
        mStops = StopsRepo.getStops(mChildRoutes!!.value!![0])
    }
}