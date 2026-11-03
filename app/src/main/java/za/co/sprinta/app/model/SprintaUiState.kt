package za.co.sprinta.app.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileUiState(
    val name: String = "Athlete",
    val email: String = "athlete@sprinta.app",
    val bio: String = "Crushing miles daily ⚡",
    val location: String = "Cape Town, South Africa",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false
)

@Immutable
data class FeedUiState(
    val posts: List<SocialPost> = emptyList(),
    val isLoading: Boolean = false
)

@Immutable
data class LeaderboardUiState(
    val isGlobal: Boolean = true,
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false
)

@Immutable
data class PerformanceUiState(
    val predicted5k: String = "21:45",
    val predicted10k: String = "44:30",
    val predictedHalf: String = "1:38:15",
    val predictedMarathon: String = "3:25:00",
    val estimatedVo2Max: Int = 54,
    val hrZonePercentages: List<Float> = listOf(0.15f, 0.25f, 0.35f, 0.15f, 0.10f),
    val weeklyVolumeKm: List<Float> = listOf(12f, 18f, 25f, 30f, 22f, 15f, 35f, 28f, 40f, 32f, 45f, 50f)
)
