package za.co.sprinta.app

import org.junit.Assert.assertEquals
import org.junit.Test
import za.co.sprinta.app.data.local.PersonalRecordEntity
import za.co.sprinta.app.data.local.WorkoutEntity
import za.co.sprinta.app.util.PersonalRecordUtils

class PersonalRecordUtilsTest {
    private fun workout(pace: Double) = WorkoutEntity(
        workoutId = "run-1",
        userId = "user-1",
        distanceMeters = 5000.0,
        durationSeconds = (pace * 5).toLong(),
        averagePaceSecondsPerKm = pace,
        startTimeIso = "2026-09-21T10:00:00Z",
        totalElevation = 20.0,
        notes = "",
        routePointsJson = "[]",
        isSynced = false
    )

    @Test fun fasterFiveKmCreatesRecord() {
        val current = PersonalRecordEntity(
            key = "user-1-5K",
            userId = "user-1",
            recordType = "5K",
            valueSecondsPerKm = 360.0,
            workoutId = "old",
            achievedAtIso = "2026-09-01T10:00:00Z"
        )

        val records = PersonalRecordUtils.detectNewRecords(workout(340.0), listOf(current))
        assertEquals(1, records.count { it.type == "5K" })
    }
}
