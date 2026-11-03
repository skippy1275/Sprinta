package za.co.sprinta.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val workoutId: String,
    val userId: String,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Double,
    val startTimeIso: String,
    val totalElevation: Double,
    val notes: String,
    val routePointsJson: String,
    val isSynced: Boolean = false,
    val activityType: String = "RUN"
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey val key: String,
    val userId: String,
    val recordType: String,
    val valueSecondsPerKm: Double,
    val workoutId: String,
    val achievedAtIso: String
)

@Dao
@JvmSuppressWildcards
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTimeIso DESC")
    fun observeWorkouts(userId: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isSynced = 0 ORDER BY startTimeIso ASC")
    suspend fun getUnsynced(): List<WorkoutEntity>

    @Query("UPDATE workouts SET isSynced = 1 WHERE workoutId = :workoutId")
    suspend fun markSynced(workoutId: String): Int

    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTimeIso DESC")
    suspend fun getWorkouts(userId: String): List<WorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long
}

@Dao
@JvmSuppressWildcards
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records WHERE userId = :userId")
    suspend fun getRecords(userId: String): List<PersonalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonalRecordEntity): Long
}

@Database(
    entities = [WorkoutEntity::class, PersonalRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SprintaDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun personalRecordDao(): PersonalRecordDao
}
