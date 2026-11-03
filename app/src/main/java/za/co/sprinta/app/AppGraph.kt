package za.co.sprinta.app

import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import za.co.sprinta.app.data.local.SprintaDatabase
import za.co.sprinta.app.data.remote.SprintaApiService
import za.co.sprinta.app.data.repository.SprintaRepository
import za.co.sprinta.app.prefs.PreferencesStore
import za.co.sprinta.app.prefs.SecureSessionStore
import java.util.concurrent.TimeUnit

object AppGraph {
    lateinit var database: SprintaDatabase
        private set
    lateinit var api: SprintaApiService
        private set
    lateinit var repository: SprintaRepository
        private set
    lateinit var preferences: PreferencesStore
        private set
    lateinit var session: SecureSessionStore
        private set

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context,
            SprintaDatabase::class.java,
            "sprinta.db"
        ).fallbackToDestructiveMigration().build()

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()

        val rawBaseUrl = runCatching { BuildConfig.API_BASE_URL }.getOrNull()
        val formattedUrl = if (!rawBaseUrl.isNullOrBlank() && rawBaseUrl.startsWith("http")) {
            rawBaseUrl.trimEnd('/') + "/"
        } else {
            "http://10.0.2.2:8080/"
        }

        api = Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SprintaApiService::class.java)

        preferences = PreferencesStore(context)
        session = SecureSessionStore(context)
        repository = SprintaRepository(database, api, session)
    }
}
