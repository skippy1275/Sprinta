package za.co.sprinta.app.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import za.co.sprinta.app.data.local.PersonalRecordEntity
import za.co.sprinta.app.data.local.SprintaDatabase
import za.co.sprinta.app.data.local.WorkoutEntity
import za.co.sprinta.app.data.remote.SprintaApiService
import za.co.sprinta.app.model.AuthResponse
import za.co.sprinta.app.model.LoginRequest
import za.co.sprinta.app.model.RegisterRequest
import za.co.sprinta.app.model.RoutePoint
import za.co.sprinta.app.model.WorkoutDto
import za.co.sprinta.app.prefs.SecureSessionStore
import za.co.sprinta.app.util.PersonalRecordUtils

class SprintaRepository(
    private val database: SprintaDatabase,
    private val api: SprintaApiService,
    private val session: SecureSessionStore
) {
    private val gson = Gson()

    fun observeWorkouts(userId: String): Flow<List<WorkoutEntity>> =
        database.workoutDao().observeWorkouts(userId)

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> =
        executeAuth { api.register(RegisterRequest(name.trim(), email.trim(), password)) }

    suspend fun login(email: String, password: String): Result<AuthResponse> =
        executeAuth { api.login(LoginRequest(email.trim(), password)) }

    private suspend fun executeAuth(call: suspend () -> Response<AuthResponse>): Result<AuthResponse> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body()?.user != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IllegalStateException("Authentication request failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveSession(response: AuthResponse) {
        val user = requireNotNull(response.user)
        session.save(user.id, user.name, user.email, response.token)
    }

    fun saveUserSession(id: String, name: String, email: String, token: String = "firebase_token") {
        session.save(id, name, email, token)
    }

    fun clearSession() = session.clear()

    fun currentSession() = session.load()

    suspend fun saveWorkout(workout: WorkoutEntity): List<String> {
        database.workoutDao().insertWorkout(workout)
        val existing = database.personalRecordDao().getRecords(workout.userId)
        val newRecords = PersonalRecordUtils.detectNewRecords(workout, existing)
        newRecords.forEach { record ->
            database.personalRecordDao().insertRecord(
                PersonalRecordUtils.toEntity(record, workout.userId, workout.workoutId)
            )
        }
        return newRecords.map { it.type }
    }

    suspend fun getUnsyncedWorkouts(): List<WorkoutEntity> = database.workoutDao().getUnsynced()

    suspend fun markSynced(workoutId: String) = database.workoutDao().markSynced(workoutId)

    suspend fun refreshFromServer(): Result<Int> {
        val current = session.load() ?: return Result.failure(IllegalStateException("No session"))
        return try {
            val response = api.getUserWorkouts(
                "Bearer ${current["token"]}",
                current["id"]!!
            )
            if (!response.isSuccessful) {
                Result.failure(IllegalStateException("History request failed: ${response.code()}"))
            } else {
                val items = response.body().orEmpty()
                items.forEach { dto ->
                    val entity = WorkoutEntity(
                        workoutId = dto.workoutId,
                        userId = dto.userId,
                        distanceMeters = dto.distanceMeters,
                        durationSeconds = dto.durationSeconds,
                        averagePaceSecondsPerKm = dto.averagePaceSecondsPerKm,
                        startTimeIso = dto.startTimeIso,
                        totalElevation = dto.totalElevation,
                        notes = dto.notes,
                        routePointsJson = gson.toJson(dto.routePoints),
                        isSynced = true
                    )
                    database.workoutDao().insertWorkout(entity)
                }
                Result.success(items.size)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun entityToDto(entity: WorkoutEntity): WorkoutDto {
        val points: List<RoutePoint> = runCatching {
            gson.fromJson(entity.routePointsJson, Array<RoutePoint>::class.java)?.toList() ?: emptyList()
        }.getOrDefault(emptyList())

        return WorkoutDto(
            workoutId = entity.workoutId,
            userId = entity.userId,
            distanceMeters = entity.distanceMeters,
            durationSeconds = entity.durationSeconds,
            averagePaceSecondsPerKm = entity.averagePaceSecondsPerKm,
            startTimeIso = entity.startTimeIso,
            totalElevation = entity.totalElevation,
            notes = entity.notes,
            routePoints = points.map {
                za.co.sprinta.app.model.LocationPointDto(
                    it.latitude,
                    it.longitude,
                    it.altitude,
                    it.timestamp
                )
            }
        )
    }
}
