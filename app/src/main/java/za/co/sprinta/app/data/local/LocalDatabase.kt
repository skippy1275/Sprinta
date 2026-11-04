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

@Entity(tableName = "gear")
data class GearEntity(
    @PrimaryKey val shoeId: String,
    val userId: String,
    val brand: String,
    val model: String,
    val totalKm: Double = 0.0,
    val maxKm: Double = 500.0,
    val isDefault: Boolean = false
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

@Dao
@JvmSuppressWildcards
interface GearDao {
    @Query("SELECT * FROM gear WHERE userId = :userId ORDER BY isDefault DESC")
    fun observeGear(userId: String): Flow<List<GearEntity>>

    @Query("SELECT * FROM gear WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultGear(userId: String): GearEntity?

    @Query("UPDATE gear SET totalKm = totalKm + :addKm WHERE shoeId = :shoeId")
    suspend fun incrementGearKm(shoeId: String, addKm: Double): Int

    @Query("UPDATE gear SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultGear(userId: String): Int

    @Query("UPDATE gear SET isDefault = 1 WHERE shoeId = :shoeId")
    suspend fun setDefaultGear(shoeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGear(gear: GearEntity): Long
}

@Database(
    entities = [WorkoutEntity::class, PersonalRecordEntity::class, GearEntity::class],
    version = 3,
    exportSchema = false
)
abstract class SprintaDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun gearDao(): GearDao
}
