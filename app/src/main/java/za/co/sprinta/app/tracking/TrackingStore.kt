package za.co.sprinta.app.tracking

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import za.co.sprinta.app.model.RoutePoint
import za.co.sprinta.app.util.GeoUtils

object TrackingStore {
    data class State(
        val active: Boolean = false,
        val startTimeMillis: Long = 0L,
        val distanceMeters: Double = 0.0,
        val durationSeconds: Long = 0L,
        val paceSecondsPerKm: Double = 0.0,
        val totalElevation: Double = 0.0,
        val points: List<RoutePoint> = emptyList()
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun start() {
        _state.value = State(
            active = true,
            startTimeMillis = System.currentTimeMillis()
        )
    }

    fun addLocation(location: Location) {
        val current = _state.value
        if (!current.active) return

        val newPoint = RoutePoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            timestamp = location.time
        )

        val previous = current.points.lastOrNull()
        val incrementalDistance = previous?.let {
            GeoUtils.haversineMeters(
                it.latitude,
                it.longitude,
                newPoint.latitude,
                newPoint.longitude
            )
        } ?: 0.0

        val elevationGain = previous?.let {
            maxOf(0.0, newPoint.altitude - it.altitude)
        } ?: 0.0

        val elapsed = ((System.currentTimeMillis() - current.startTimeMillis) / 1000L).coerceAtLeast(0L)
        val distance = current.distanceMeters + incrementalDistance
        val pace = GeoUtils.paceSecondsPerKm(distance, elapsed)

        _state.value = current.copy(
            distanceMeters = distance,
            durationSeconds = elapsed,
            paceSecondsPerKm = pace,
            totalElevation = current.totalElevation + elevationGain,
            points = current.points + newPoint
        )
    }

    fun stop() {
        val current = _state.value
        val elapsed = ((System.currentTimeMillis() - current.startTimeMillis) / 1000L).coerceAtLeast(current.durationSeconds)
        _state.value = current.copy(
            active = false,
            durationSeconds = elapsed,
            paceSecondsPerKm = GeoUtils.paceSecondsPerKm(current.distanceMeters, elapsed)
        )
    }

    fun reset() {
        _state.value = State()
    }
}
