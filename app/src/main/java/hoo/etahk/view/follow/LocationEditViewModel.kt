package hoo.etahk.view.follow

import android.arch.lifecycle.ViewModel
import hoo.etahk.transfer.repo.FollowRepo

class LocationEditViewModel : ViewModel() {
    var locationId: Long? = null
        set(value) {
            field = if (value != null && value > 0L) value else null
        }

    var name: String = ""

    var latitude: Double? = null
        set(value) {
            field =  if (value != null && value > 0f) value else null
        }

    var longitude: Double? = null
        set(value) {
            field =  if (value != null && value > 0f) value else null
        }

    fun insertLocation() {
        FollowRepo.insertLocation(name)
    }

    fun updateLocation() {
        //FollowRepo.updateLocation(location!!)
    }
}