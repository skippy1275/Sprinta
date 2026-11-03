package za.co.sprinta.app.model

import com.google.gson.annotations.SerializedName

// ---------- API models ----------
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto?
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String
)

data class LocationPointDto(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Long
)

data class WorkoutDto(
    val workoutId: String,
    val userId: String,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Double,
    val startTimeIso: String,
    val totalElevation: Double,
    val notes: String,
    val routePoints: List<LocationPointDto>
)

data class SyncResponse(
    val status: String,
    val message: String,
    val serverTimestamp: Long
)

// ---------- Local models ----------
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Long
)

data class NewRecord(
    val type: String,
    val valueSecondsPerKm: Double
)

// ---------- UI models ----------
data class UserSession(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)
