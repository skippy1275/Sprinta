package za.co.sprinta.app.util

import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Validators {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())
    fun isValidPassword(password: String): Boolean = passwordRegex.matches(password)
}

object GeoUtils {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun haversineMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun paceSecondsPerKm(distanceMeters: Double, durationSeconds: Long): Double {
        if (distanceMeters <= 0.0) return 0.0
        return durationSeconds.toDouble() / (distanceMeters / 1000.0)
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%02d:%02d:%02d".format(Locale.US, h, m, s)
        else "%02d:%02d".format(Locale.US, m, s)
    }

    fun formatPace(secondsPerKm: Double): String {
        if (secondsPerKm <= 0.0 || secondsPerKm.isInfinite() || secondsPerKm.isNaN()) return "--:-- /km"
        val total = secondsPerKm.toInt()
        return "%02d:%02d /km".format(Locale.US, total / 60, total % 60)
    }
}
