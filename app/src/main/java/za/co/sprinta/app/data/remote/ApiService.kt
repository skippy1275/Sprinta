package za.co.sprinta.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import za.co.sprinta.app.BuildConfig
import za.co.sprinta.app.model.AuthResponse
import za.co.sprinta.app.model.LoginRequest
import za.co.sprinta.app.model.RegisterRequest
import za.co.sprinta.app.model.SyncResponse
import za.co.sprinta.app.model.WorkoutDto

interface SprintaApiService {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/runs/sync")
    suspend fun syncWorkout(
        @Header("Authorization") token: String,
        @Body workout: WorkoutDto
    ): Response<SyncResponse>

    @GET("api/v1/runs/user/{userId}")
    suspend fun getUserWorkouts(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<List<WorkoutDto>>
}
