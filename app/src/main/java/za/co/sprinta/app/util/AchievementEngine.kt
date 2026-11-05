package za.co.sprinta.app.util

import androidx.compose.runtime.Immutable
import za.co.sprinta.app.data.local.WorkoutEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

@Immutable
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean,
    val category: String = "TROPHY"
)

object AchievementEngine {

    fun calculateStreakWeeks(workouts: List<WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 0

        val zoneId = ZoneId.systemDefault()
        val weekFields = java.time.temporal.WeekFields.of(DayOfWeek.MONDAY, 1)
        val now = java.time.ZonedDateTime.now(zoneId)

        val workoutWeeks = workouts.mapNotNull { workout ->
            runCatching {
                val zdt = Instant.parse(workout.startTimeIso).atZone(zoneId)
                Pair(zdt.year, zdt.get(weekFields.weekOfWeekBasedYear()))
            }.getOrNull()
        }.toSet()

        var streak = 0
        var checkTime = now
        while (true) {
            val yr = checkTime.year
            val wk = checkTime.get(weekFields.weekOfWeekBasedYear())
            if (workoutWeeks.contains(Pair(yr, wk))) {
                streak++
                checkTime = checkTime.minusWeeks(1)
            } else {
                break
            }
        }
        return streak
    }

    fun calculateBadges(workouts: List<WorkoutEntity>): List<Badge> {
        val totalDistanceMeters = workouts.sumOf { it.distanceMeters }
        val workoutCount = workouts.size
        val streakWeeks = calculateStreakWeeks(workouts)

        val hasWeekendRun = workouts.any { workout ->
            runCatching {
                val instant = Instant.parse(workout.startTimeIso)
                val day = instant.atZone(ZoneId.systemDefault()).dayOfWeek
                day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
            }.getOrDefault(false)
        }

        return listOf(
            Badge(
                id = "badge_first_blood",
                title = "First Blood",
                description = "Completed your first Sprinta workout.",
                emoji = "🩸",
                isUnlocked = workoutCount >= 1
            ),
            Badge(
                id = "badge_weekend_warrior",
                title = "Weekend Warrior",
                description = "Logged a workout on a Saturday or Sunday.",
                emoji = "🛡️",
                isUnlocked = hasWeekendRun
            ),
            Badge(
                id = "badge_consistency",
                title = "Consistency King",
                description = "3-week workout streak (Monday - Sunday).",
                emoji = "⚡",
                isUnlocked = streakWeeks >= 3 || workoutCount >= 3
            ),
            Badge(
                id = "badge_50k_club",
                title = "50km Club",
                description = "Accumulated 50km total running distance.",
                emoji = "🏆",
                isUnlocked = totalDistanceMeters >= 50_000.0
            )
        )
    }
}
