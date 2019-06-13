package hoo.etahk.model.custom

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import hoo.etahk.model.data.Stop

@Entity
data class NearbyStop(
    @Embedded
    var stop: Stop,
    var distance: Double) {

    @Ignore
    var showHeader: Boolean = false
}
