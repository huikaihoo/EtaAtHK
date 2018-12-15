package hoo.etahk.common.extensions

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun LatLng.toLocation(): Location {
    val location = Location("")
    location.latitude = this.latitude
    location.longitude = this.longitude
    return location
}