package za.co.sprinta.app.util

import za.co.sprinta.app.data.local.PersonalRecordEntity
import za.co.sprinta.app.data.local.WorkoutEntity
import za.co.sprinta.app.model.NewRecord
import java.time.Instant

object PersonalRecordUtils {
    private data class Target(val type: String, val minimumDistanceMeters: Double)

    private val targets = listOf(
        Target("1K", 1_000.0),
        Target("5K", 5_000.0),
        Target("10K", 10_000.0)
    )

    fun detectNewRecords(
        workout: WorkoutEntity,
        existing: List<PersonalRecordEntity>
    ): List<NewRecord> {
        val newRecords = mutableListOf<NewRecord>()
        for (target in targets) {
            if (workout.distanceMeters < target.minimumDistanceMeters) continue
            val current = existing.firstOrNull { it.recordType == target.type }
            if (current == null || workout.averagePaceSecondsPerKm < current.valueSecondsPerKm) {
                newRecords += NewRecord(target.type, workout.averagePaceSecondsPerKm)
            }
        }
        return newRecords
    }

    fun toEntity(record: NewRecord, userId: String, workoutId: String): PersonalRecordEntity =
        PersonalRecordEntity(
            key = "$userId-${record.type}",
            userId = userId,
            recordType = record.type,
            valueSecondsPerKm = record.valueSecondsPerKm,
            workoutId = workoutId,
            achievedAtIso = Instant.now().toString()
        )
}
