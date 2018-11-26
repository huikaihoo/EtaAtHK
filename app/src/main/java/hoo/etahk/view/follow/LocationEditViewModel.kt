package hoo.etahk.view.follow

import androidx.lifecycle.ViewModel
import hoo.etahk.model.data.FollowLocation
import hoo.etahk.transfer.repo.FollowRepo

class LocationEditViewModel : ViewModel() {
    var isInit = false

    private var followLocation: FollowLocation = FollowLocation()

    var locationId: Long?
        get() = followLocation.Id
        set(value) {
            if (value != null && value > 0L) {
                followLocation = FollowRepo.getLocationOnce(value)
            }
        }

    var name: String
        get() = followLocation.name
        set(value) { followLocation.name = value }

    var latitude: Double?
        get() {
            val value = followLocation.latitude
            return if (value >= 0f) value else null
        }
        set(value) {
            if (value != null && value >= 0.0) {
                followLocation.latitude = value
            }
        }

    var longitude: Double?
        get() {
            val value = followLocation.longitude
            return if (value >= 0f) value else null
        }
        set(value) {
            if (value != null && value >= 0.0) {
                followLocation.longitude = value
            }
        }

    val nameHistory: HashSet<String> = hashSetOf()

    fun saveLocation(): Boolean {
        return when (name.isNotBlank() && latitude != null && longitude != null) {
            true -> {
                if (locationId == null) {
                    FollowRepo.insertLocation(followLocation)
                } else {
                    FollowRepo.updateLocation(followLocation)
                }
                true
            }
            false -> false
        }
    }
}