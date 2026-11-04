package za.co.sprinta.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import za.co.sprinta.app.AppGraph
import za.co.sprinta.app.tracking.TrackingStore
import za.co.sprinta.app.util.LocationUtils
import java.util.Locale

data class TrackingState(
    val isTracking: Boolean = false,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val currentPaceSecondsPerKm: Double = 0.0,
    val routeCoordinates: List<Pair<Double, Double>> = emptyList()
)

class TrackingService : Service(), TextToSpeech.OnInitListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private var tts: TextToSpeech? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var audioIntervalKm: Int = 1
    private var lastAnnouncedKm: Int = 0

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { location ->
                if (LocationUtils.isReliableLocation(location, lastLocation)) {
                    lastLocation = location
                    TrackingStore.addLocation(location)
                    updateTrackingState()
                    checkAndAnnounceSplit()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tts = TextToSpeech(this, this)

        serviceScope.launch {
            AppGraph.preferences.preferences.collect { prefs ->
                audioIntervalKm = prefs.audioIntervalKm
            }
        }

        createNotificationChannel()
    }

    private fun checkAndAnnounceSplit() {
        if (audioIntervalKm == 0) return

        val currentKm = (TrackingStore.state.value.distanceMeters / 1000).toInt()
        if (currentKm >= lastAnnouncedKm + audioIntervalKm && currentKm > 0) {
            lastAnnouncedKm = currentKm
            val pace = LocationUtils.formatPace(TrackingStore.state.value.paceSecondsPerKm)
            val message = "$currentKm kilometer${if (currentKm > 1) "s" else ""} completed. Average pace: $pace."
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        startForegroundServiceNotification()
        TrackingStore.start()
        lastAnnouncedKm = 0

        // Immediate Initial Position Seeding
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && LocationUtils.isReliableLocation(loc, lastLocation)) {
                    lastLocation = loc
                    TrackingStore.addLocation(loc)
                    updateTrackingState()
                }
            }
        } catch (_: SecurityException) {}

        updateTrackingState()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(0.5f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(callback)
        TrackingStore.stop()
        updateTrackingState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateTrackingState() {
        val storeState = TrackingStore.state.value
        val coords = storeState.points.map { Pair(it.latitude, it.longitude) }
        _trackingState.value = TrackingState(
            isTracking = storeState.active,
            distanceMeters = storeState.distanceMeters,
            durationSeconds = storeState.durationSeconds,
            currentPaceSecondsPerKm = storeState.paceSecondsPerKm,
            routeCoordinates = coords
        )
    }

    private fun startForegroundServiceNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sprinta Tracking Active")
            .setContentText("Recording your GPS route and live metrics...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Sprinta Tracking Engine", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        tts?.stop()
        tts?.shutdown()
        fusedLocationClient.removeLocationUpdates(callback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "za.co.sprinta.app.action.START_TRACKING"
        const val ACTION_STOP = "za.co.sprinta.app.action.STOP_TRACKING"
        private const val CHANNEL_ID = "sprinta_tracking_engine"
        private const val NOTIFICATION_ID = 5102

        private val _trackingState = MutableStateFlow(TrackingState())
        val trackingState: StateFlow<TrackingState> = _trackingState
    }
}
