package za.co.sprinta.app.util

import android.location.Location
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {
    private const val EARTH_RADIUS_METERS = 6_371_000.0
    private const val MAX_ACCURACY_METERS = 10.0f
    private const val MAX_SPEED_MPS = 15.0 // 54 km/h

    fun haversineDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun isReliableLocation(newLocation: Location, lastLocation: Location?): Boolean {
        if (newLocation.hasAccuracy() && newLocation.accuracy > MAX_ACCURACY_METERS) {
            return false
        }
        if (lastLocation != null) {
            val timeDiffSec = (newLocation.time - lastLocation.time) / 1000.0
            if (timeDiffSec > 0) {
                val distance = haversineDistanceMeters(
                    lastLocation.latitude, lastLocation.longitude,
                    newLocation.latitude, newLocation.longitude
                )
                val speed = distance / timeDiffSec
                if (speed > MAX_SPEED_MPS) {
                    return false
                }
            }
        }
        return true
    }

    fun formatPace(secondsPerKm: Double): String {
        if (secondsPerKm <= 0.0 || secondsPerKm.isInfinite() || secondsPerKm.isNaN()) {
            return "--:-- /km"
        }
        val totalSec = secondsPerKm.toInt()
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return "%02d:%02d /km".format(Locale.US, minutes, seconds)
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%02d:%02d:%02d".format(Locale.US, h, m, s)
        else "%02d:%02d".format(Locale.US, m, s)
    }
}
