package hoo.etahk.view.route

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import hoo.etahk.model.data.Route
import hoo.etahk.model.data.RouteKey
import hoo.etahk.model.data.Stop
import hoo.etahk.model.json.StringLang
import hoo.etahk.model.repo.StopsRepo

class RouteFragmentViewModel : ViewModel() {
    private var mStops: LiveData<List<Stop>>? = null

    var routeKey: RouteKey? = null
        set(value) {
            field = value
            if (value != null)
                subscribeToRepo()
        }

    fun getStops(): LiveData<List<Stop>> {
        return mStops!!
    }

    fun insertStops() {
        StopsRepo.insertStop()
    }

    fun updateEta(stops: List<Stop>) {
        StopsRepo.updateEta(stops)
    }

    fun updateAllEta() {
        if (mStops != null)
            StopsRepo.updateEta(mStops!!.value)
    }

    private fun subscribeToRepo() {
        mStops = StopsRepo.getStops(Route(//id = 1,
                routeKey = routeKey!!.copy(variant = 1),
                direction = 0,
                companyDetails = listOf("KMB"),
                from = StringLang("A", ""),
                to = StringLang("B", "")))
    }
}