package za.co.sprinta.app.model

import androidx.compose.runtime.Immutable

@Immutable
data class SocialPost(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String? = null,
    val activityType: String,
    val distanceFormatted: String,
    val durationFormatted: String,
    val paceFormatted: String,
    val timestampFormatted: String,
    val kudosCount: Int,
    val isKudosGiven: Boolean = false,
    val caption: String = ""
)

@Immutable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val weeklyDistanceKm: Double,
    val totalWorkouts: Int,
    val isCurrentUser: Boolean = false
)
