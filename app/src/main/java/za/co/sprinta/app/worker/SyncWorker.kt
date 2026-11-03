package za.co.sprinta.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.sprinta.app.AppGraph

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val token = AppGraph.session.load()?.get("token")
            ?: return@withContext Result.failure()

        val unsynced = AppGraph.repository.getUnsyncedWorkouts()
        if (unsynced.isEmpty()) return@withContext Result.success()

        return@withContext try {
            for (workout in unsynced) {
                val response = AppGraph.api.syncWorkout(
                    "Bearer $token",
                    AppGraph.repository.entityToDto(workout)
                )

                when {
                    response.isSuccessful -> AppGraph.repository.markSynced(workout.workoutId)
                    response.code() in 500..599 -> return@withContext Result.retry()
                    response.code() == 401 -> return@withContext Result.failure()
                    else -> return@withContext Result.failure()
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
