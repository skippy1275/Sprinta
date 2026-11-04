package za.co.sprinta.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import za.co.sprinta.app.data.local.WorkoutEntity
import za.co.sprinta.app.model.FeedUiState
import za.co.sprinta.app.model.LeaderboardEntry
import za.co.sprinta.app.model.LeaderboardUiState
import za.co.sprinta.app.model.PerformanceUiState
import za.co.sprinta.app.model.ProfileUiState
import za.co.sprinta.app.model.RoutePoint
import za.co.sprinta.app.model.SocialPost
import za.co.sprinta.app.prefs.AppPreferences
import za.co.sprinta.app.tracking.TrackingStore
import za.co.sprinta.app.util.GeoUtils
import za.co.sprinta.app.util.PersonalRecordUtils
import za.co.sprinta.app.util.Validators
import za.co.sprinta.app.worker.SyncWorker
import java.time.Instant
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    enum class Screen { AUTH, DASHBOARD, TRACKING, SUMMARY, SETTINGS, HISTORY, PRO_UPGRADE }

    private val repository = AppGraph.repository
    private val gson = Gson()

    private val _screen = MutableStateFlow(Screen.AUTH)
    val screen: StateFlow<Screen> = _screen

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _newRecords = MutableStateFlow<List<String>>(emptyList())
    val newRecords: StateFlow<List<String>> = _newRecords

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _session = MutableStateFlow(repository.currentSession())
    val session: StateFlow<Map<String, String>?> = _session

    val preferences: StateFlow<AppPreferences> = AppGraph.preferences.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())

    val trackingState = TrackingStore.state

    val workouts: StateFlow<List<WorkoutEntity>> = _session
        .map { it?.get("id") }
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else repository.observeWorkouts(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gearList: StateFlow<List<za.co.sprinta.app.data.local.GearEntity>> = _session
        .map { it?.get("id") }
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else repository.observeGear(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val badges: StateFlow<List<za.co.sprinta.app.util.Badge>> = workouts
        .map { za.co.sprinta.app.util.AchievementEngine.calculateBadges(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Phase 1 Architecture: Immutable UiStates
    private val _profileUiState = MutableStateFlow(
        ProfileUiState(
            name = _session.value?.get("name") ?: "Athlete",
            email = _session.value?.get("email") ?: "athlete@sprinta.app"
        )
    )
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState

    private val _feedUiState = MutableStateFlow(
        FeedUiState(
            posts = listOf(
                SocialPost(
                    id = "post-1",
                    userId = "user-101",
                    userName = "Kipchoge Fan",
                    activityType = "🏃‍♂️ Run",
                    distanceFormatted = "10.00 km",
                    durationFormatted = "42:15",
                    paceFormatted = "04:13 /km",
                    timestampFormatted = "2 hours ago",
                    kudosCount = 14,
                    caption = "Morning threshold run! Feeling great."
                ),
                SocialPost(
                    id = "post-2",
                    userId = "user-102",
                    userName = "Sarah Jenkins",
                    activityType = "🚶‍♂️ Walk",
                    distanceFormatted = "5.20 km",
                    durationFormatted = "52:10",
                    paceFormatted = "10:01 /km",
                    timestampFormatted = "Yesterday",
                    kudosCount = 8,
                    caption = "Recovery walk along the coastline."
                ),
                SocialPost(
                    id = "post-3",
                    userId = "user-103",
                    userName = "David Miller",
                    activityType = "🏋️‍♂️ Workout",
                    distanceFormatted = "0.00 km",
                    durationFormatted = "45:00",
                    paceFormatted = "--:-- /km",
                    timestampFormatted = "2 days ago",
                    kudosCount = 22,
                    caption = "Heavy leg day & core conditioning."
                )
            )
        )
    )
    val feedUiState: StateFlow<FeedUiState> = _feedUiState

    private val _leaderboardUiState = MutableStateFlow(
        LeaderboardUiState(
            entries = listOf(
                LeaderboardEntry(1, "u1", "Alex Vance", 62.4, 6),
                LeaderboardEntry(2, "u2", "Elena Rostova", 58.1, 5),
                LeaderboardEntry(3, "u3", "Lethabo Mokoena", 51.0, 4),
                LeaderboardEntry(4, "u4", "Sipho Dlamini", 44.5, 4),
                LeaderboardEntry(5, "u5", "Thabo Mbeki", 38.2, 3, isCurrentUser = true)
            )
        )
    )
    val leaderboardUiState: StateFlow<LeaderboardUiState> = _leaderboardUiState

    private val _performanceUiState = MutableStateFlow(PerformanceUiState())
    val performanceUiState: StateFlow<PerformanceUiState> = _performanceUiState

    // Intent Handlers
    fun onNameChanged(newName: String) {
        _profileUiState.value = _profileUiState.value.copy(name = newName)
    }

    fun onBioChanged(newBio: String) {
        _profileUiState.value = _profileUiState.value.copy(bio = newBio)
    }

    fun onLocationChanged(newLoc: String) {
        _profileUiState.value = _profileUiState.value.copy(location = newLoc)
    }

    fun onAvatarUrlChanged(newUrl: String) {
        _profileUiState.value = _profileUiState.value.copy(avatarUrl = newUrl)
    }

    fun addCustomGear(brand: String, model: String) {
        val userId = _session.value?.get("id") ?: return
        viewModelScope.launch {
            repository.addGear(userId, brand, model)
            _message.value = "Custom shoe added."
        }
    }

    fun setDefaultGear(shoeId: String) {
        val userId = _session.value?.get("id") ?: return
        viewModelScope.launch {
            repository.setDefaultGear(userId, shoeId)
            _message.value = "Default shoe updated."
        }
    }

    fun onSaveProfile() {
        val current = _profileUiState.value
        _profileUiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            val sessionData = repository.currentSession()
            if (sessionData != null) {
                repository.saveUserSession(
                    id = sessionData["id"] ?: "user",
                    name = current.name,
                    email = current.email,
                    token = sessionData["token"] ?: ""
                )
                _session.value = repository.currentSession()
            }
            _profileUiState.value = _profileUiState.value.copy(
                isSaving = false,
                isSavedSuccess = true
            )
            _message.value = "Profile updated successfully."
        }
    }

    fun toggleKudos(postId: String) {
        val currentPosts = _feedUiState.value.posts.map { post ->
            if (post.id == postId) {
                val newKudos = if (post.isKudosGiven) post.kudosCount - 1 else post.kudosCount + 1
                post.copy(kudosCount = newKudos, isKudosGiven = !post.isKudosGiven)
            } else post
        }
        _feedUiState.value = _feedUiState.value.copy(posts = currentPosts)
    }

    fun toggleLeaderboardTab(isGlobal: Boolean) {
        _leaderboardUiState.value = _leaderboardUiState.value.copy(isGlobal = isGlobal)
    }

    init {
        if (_session.value != null) {
            _screen.value = Screen.DASHBOARD
            SyncWorker.schedule(getApplication())
        }

        viewModelScope.launch {
            repository.observeFirestoreFeed().collect { firestorePosts ->
                if (firestorePosts.isNotEmpty()) {
                    _feedUiState.value = _feedUiState.value.copy(posts = firestorePosts)
                }
            }
        }
    }

    fun setRegistering(value: Boolean) {
        _isRegistering.value = value
        _message.value = null
    }

    fun clearMessage() {
        _message.value = null
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _busy.value = true
            _message.value = "Initializing Google Authentication..."
            kotlinx.coroutines.delay(800)
            _busy.value = false
        }
    }

    fun loginWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            _busy.value = true
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    _busy.value = false
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        if (firebaseUser != null) {
                            val uid = firebaseUser.uid
                            val name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "Athlete"
                            val mail = firebaseUser.email ?: "athlete@sprinta.app"
                            repository.saveUserSession(uid, name, mail, "firebase_token")
                            _session.value = repository.currentSession()
                            _screen.value = Screen.DASHBOARD
                            _message.value = "Google Authentication Successful."
                            SyncWorker.schedule(getApplication())
                        }
                    } else {
                        val err = task.exception
                        val userMsg = when (err) {
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Google credentials invalid or expired."
                            is com.google.firebase.FirebaseNetworkException -> "Network error during Google Sign-In."
                            else -> err?.localizedMessage ?: "Google Sign-In failed."
                        }
                        _message.value = userMsg
                    }
                }
        }
    }

    fun login(email: String, password: String) {
        if (!Validators.isValidEmail(email)) {
            _message.value = "Enter a valid email address."
            return
        }
        if (password.isBlank()) {
            _message.value = "Password is required."
            return
        }

        viewModelScope.launch {
            _busy.value = true
            com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    _busy.value = false
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        if (firebaseUser != null) {
                            val uid = firebaseUser.uid
                            val name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@') ?: "Athlete"
                            val mail = firebaseUser.email ?: email
                            repository.saveUserSession(uid, name, mail, "firebase_token")
                            _session.value = repository.currentSession()
                            _screen.value = Screen.DASHBOARD
                            _message.value = null
                            SyncWorker.schedule(getApplication())
                        }
                    } else {
                        val err = task.exception
                        val msg = when (err) {
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please check your credentials."
                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No user account found with this email."
                            is com.google.firebase.FirebaseNetworkException -> "Network error. Please check your internet connection."
                            else -> err?.localizedMessage ?: "Login failed. Please check your credentials."
                        }
                        android.util.Log.e("SprintaAuth", "Login error: $msg", err)
                        _message.value = msg
                    }
                }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        when {
            name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                _message.value = "Please complete all required fields."
                return
            }
            !Validators.isValidEmail(email) -> {
                _message.value = "Enter a valid email address."
                return
            }
            !Validators.isValidPassword(password) -> {
                _message.value = "Password must be at least 8 characters and include an uppercase letter, a number and a special character."
                return
            }
            password != confirmPassword -> {
                _message.value = "Passwords do not match."
                return
            }
        }

        viewModelScope.launch {
            _busy.value = true
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        if (firebaseUser != null) {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name.trim())
                                .build()
                            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener {
                                _busy.value = false
                                repository.saveUserSession(firebaseUser.uid, name.trim(), email.trim(), "firebase_token")
                                _session.value = repository.currentSession()
                                _screen.value = Screen.DASHBOARD
                                _isRegistering.value = false
                                _message.value = null
                                SyncWorker.schedule(getApplication())
                            }
                        } else {
                            _busy.value = false
                        }
                    } else {
                        _busy.value = false
                        val msg = task.exception?.localizedMessage ?: "Registration failed."
                        android.util.Log.e("SprintaAuth", "Registration error: $msg", task.exception)
                        _message.value = msg
                    }
                }
        }
    }

    fun logout() {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        repository.clearSession()
        _session.value = null
        _screen.value = Screen.AUTH
        TrackingStore.reset()
    }

    fun resetPassword(email: String) {
        if (!Validators.isValidEmail(email)) {
            _message.value = "Enter a valid email address."
            return
        }
        viewModelScope.launch {
            _busy.value = true
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                _message.value = "Password reset link sent to $email."
            } catch (e: Exception) {
                android.util.Log.e("SprintaAuth", "Reset password error: ${e.localizedMessage}", e)
                _message.value = "Failed to send reset link: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _busy.value = false
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _busy.value = true
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()
            } catch (_: Exception) {}
            repository.clearSession()
            _session.value = null
            _screen.value = Screen.AUTH
            TrackingStore.reset()
            _busy.value = false
            _message.value = "Your account has been deleted."
        }
    }

    fun goTo(screen: Screen) {
        _message.value = null
        _screen.value = screen
    }

    fun saveManualWorkout(
        activityType: String,
        distanceKm: Double,
        durationMinutes: Long,
        dateIso: String,
        notes: String
    ) {
        val userId = _session.value?.get("id") ?: return
        if (distanceKm <= 0.0 && durationMinutes <= 0L) {
            _message.value = "Please enter valid distance or duration."
            return
        }

        viewModelScope.launch {
            _busy.value = true
            val distanceMeters = distanceKm * 1000.0
            val durationSeconds = durationMinutes * 60L
            val pace = GeoUtils.paceSecondsPerKm(distanceMeters, durationSeconds)

            val entity = WorkoutEntity(
                workoutId = UUID.randomUUID().toString(),
                userId = userId,
                distanceMeters = distanceMeters,
                durationSeconds = durationSeconds,
                averagePaceSecondsPerKm = pace,
                startTimeIso = dateIso,
                totalElevation = 0.0,
                notes = notes.trim(),
                routePointsJson = "[]",
                activityType = activityType
            )
            val records = repository.saveWorkout(entity)
            _newRecords.value = records
            _busy.value = false
            SyncWorker.schedule(getApplication())
            _message.value = "Manual workout saved."
        }
    }

    fun saveCurrentWorkout(notes: String) {
        val userId = _session.value?.get("id") ?: return
        val state = TrackingStore.state.value
        if (state.points.size < 2 || state.distanceMeters <= 0.0) {
            _message.value = "Move a little further before saving your workout."
            return
        }

        viewModelScope.launch {
            _busy.value = true
            val entity = WorkoutEntity(
                workoutId = UUID.randomUUID().toString(),
                userId = userId,
                distanceMeters = state.distanceMeters,
                durationSeconds = state.durationSeconds,
                averagePaceSecondsPerKm = state.paceSecondsPerKm,
                startTimeIso = Instant.ofEpochMilli(state.startTimeMillis).toString(),
                totalElevation = state.totalElevation,
                notes = notes.trim(),
                routePointsJson = gson.toJson(state.points)
            )
            val records = repository.saveWorkout(entity)
            _newRecords.value = records
            _busy.value = false
            SyncWorker.schedule(getApplication())
            _message.value = "Workout saved."
            _screen.value = Screen.DASHBOARD
            TrackingStore.reset()
        }
    }

    fun resetRecordDialog() {
        _newRecords.value = emptyList()
    }

    fun setPremium(value: Boolean) {
        _isPremium.value = value
    }

    fun setDarkMode(value: Boolean) {
        viewModelScope.launch { AppGraph.preferences.setDarkMode(value) }
    }

    fun setUseMiles(value: Boolean) {
        viewModelScope.launch { AppGraph.preferences.setUseMiles(value) }
    }

    fun setAudioInterval(value: Int) {
        viewModelScope.launch { AppGraph.preferences.setAudioInterval(value) }
    }

    fun setLanguage(value: String) {
        viewModelScope.launch { AppGraph.preferences.setLanguage(value) }
    }

    fun startSync() {
        SyncWorker.schedule(getApplication())
        _message.value = "Sync queued."
    }
}
