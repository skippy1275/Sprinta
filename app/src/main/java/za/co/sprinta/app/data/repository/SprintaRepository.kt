package za.co.sprinta.app.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
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

    fun observeGear(userId: String): Flow<List<za.co.sprinta.app.data.local.GearEntity>> =
        database.gearDao().observeGear(userId)

    suspend fun addGear(userId: String, brand: String, model: String, maxKm: Double = 500.0) {
        val defaultGear = database.gearDao().getDefaultGear(userId)
        val isDefault = defaultGear == null
        val gear = za.co.sprinta.app.data.local.GearEntity(
            shoeId = java.util.UUID.randomUUID().toString(),
            userId = userId,
            brand = brand.trim(),
            model = model.trim(),
            totalKm = 0.0,
            maxKm = maxKm,
            isDefault = isDefault
        )
        database.gearDao().insertGear(gear)
    }

    suspend fun setDefaultGear(userId: String, shoeId: String) {
        database.gearDao().clearDefaultGear(userId)
        database.gearDao().setDefaultGear(shoeId)
    }

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
        val activeGear = database.gearDao().getDefaultGear(workout.userId)
        if (activeGear != null && workout.distanceMeters > 0) {
            val addKm = workout.distanceMeters / 1000.0
            database.gearDao().incrementGearKm(activeGear.shoeId, addKm)
        }
        publishToFirestoreFeed(workout)
        return newRecords.map { it.type }
    }

    private fun publishToFirestoreFeed(workout: WorkoutEntity) {
        val sessionUser = session.load()
        val userName = sessionUser?.get("name") ?: "Athlete"
        val postMap = hashMapOf(
            "id" to workout.workoutId,
            "userId" to workout.userId,
            "userName" to userName,
            "activityType" to workout.activityType,
            "distanceFormatted" to "%.2f km".format(workout.distanceMeters / 1000.0),
            "durationFormatted" to za.co.sprinta.app.util.GeoUtils.formatDuration(workout.durationSeconds),
            "paceFormatted" to za.co.sprinta.app.util.GeoUtils.formatPace(workout.averagePaceSecondsPerKm),
            "timestampFormatted" to "Just now",
            "timestampMillis" to System.currentTimeMillis(),
            "kudosCount" to 0,
            "caption" to workout.notes
        )
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("community_feed")
            .document(workout.workoutId)
            .set(postMap)
    }

    fun observeFirestoreFeed(): Flow<List<za.co.sprinta.app.model.SocialPost>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("community_feed")
            .orderBy("timestampMillis", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        za.co.sprinta.app.model.SocialPost(
                            id = doc.getString("id") ?: doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "Athlete",
                            activityType = doc.getString("activityType") ?: "RUN",
                            distanceFormatted = doc.getString("distanceFormatted") ?: "0.00 km",
                            durationFormatted = doc.getString("durationFormatted") ?: "00:00",
                            paceFormatted = doc.getString("paceFormatted") ?: "--:-- /km",
                            timestampFormatted = doc.getString("timestampFormatted") ?: "Recently",
                            kudosCount = (doc.getLong("kudosCount") ?: 0L).toInt(),
                            caption = doc.getString("caption") ?: ""
                        )
                    }.getOrNull()
                }
                trySend(posts)
            }
        awaitClose { listener.remove() }
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
