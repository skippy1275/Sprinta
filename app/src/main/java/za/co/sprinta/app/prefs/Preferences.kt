package za.co.sprinta.app.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sprinta_settings")

data class AppPreferences(
    val darkMode: Boolean = false,
    val useMiles: Boolean = false,
    val audioIntervalKm: Int = 1,
    val language: String = "en",
    val weeklyGoalKm: Float = 20.0f
)

class PreferencesStore(private val context: Context) {
    private val darkKey = booleanPreferencesKey("dark_mode")
    private val milesKey = booleanPreferencesKey("use_miles")
    private val audioKey = intPreferencesKey("audio_interval")
    private val languageKey = stringPreferencesKey("language")
    private val goalKey = floatPreferencesKey("weekly_goal")

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { p ->
        AppPreferences(
            darkMode = p[darkKey] ?: false,
            useMiles = p[milesKey] ?: false,
            audioIntervalKm = p[audioKey] ?: 1,
            language = p[languageKey] ?: "en",
            weeklyGoalKm = p[goalKey] ?: 20.0f
        )
    }

    suspend fun setDarkMode(value: Boolean) = context.dataStore.edit { it[darkKey] = value }
    suspend fun setUseMiles(value: Boolean) = context.dataStore.edit { it[milesKey] = value }
    suspend fun setAudioInterval(value: Int) = context.dataStore.edit { it[audioKey] = value }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[languageKey] = value }
    suspend fun setWeeklyGoal(value: Float) = context.dataStore.edit { it[goalKey] = value }
}

class SecureSessionStore(context: Context) {
    private val prefs = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "sprinta_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun save(id: String, name: String, email: String, token: String) {
        prefs.edit()
            .putString("id", id)
            .putString("name", name)
            .putString("email", email)
            .putString("token", token)
            .apply()
    }

    fun load(): Map<String, String>? {
        val id = prefs.getString("id", null) ?: return null
        val name = prefs.getString("name", null) ?: return null
        val email = prefs.getString("email", null) ?: return null
        val token = prefs.getString("token", null) ?: return null
        return mapOf("id" to id, "name" to name, "email" to email, "token" to token)
    }

    fun clear() = prefs.edit().clear().apply()
}
