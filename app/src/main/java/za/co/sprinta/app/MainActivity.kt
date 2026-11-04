package za.co.sprinta.app

// 1. ALL IMPORTS MUST BE AT THE VERY TOP
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import za.co.sprinta.app.billing.SprintaBillingViewModel
import za.co.sprinta.app.data.local.WorkoutEntity
import za.co.sprinta.app.navigation.Screen as NavScreen
import za.co.sprinta.app.service.TrackingService
import za.co.sprinta.app.tracking.TrackingStore
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.PrimaryButton
import za.co.sprinta.app.ui.SecondaryButton
import za.co.sprinta.app.ui.SprintaBlack
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaTheme
import za.co.sprinta.app.ui.SprintaWhite
import za.co.sprinta.app.ui.SprintaTopAppBar
import za.co.sprinta.app.ui.SprintaTextField
import za.co.sprinta.app.ui.splash.VideoSplashScreen
import za.co.sprinta.app.util.GeoUtils
import za.co.sprinta.app.util.LocationUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// 2. MAIN ACTIVITY CLASS
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        za.co.sprinta.app.ads.AdManager.gatherConsentAndInitialize(this)
        super.onCreate(savedInstanceState)

        setContent {
            val prefs by viewModel.preferences.collectAsState()
            SprintaTheme(darkMode = prefs.darkMode) {
                SprintaApp(viewModel)
            }
        }
    }
}

// 3. MAIN NAVIGATION SCAFFOLD WITH NAVCONTROLLER
@Composable
private fun SprintaApp(vm: MainViewModel) {
    val navController = rememberNavController()
    val session by vm.session.collectAsState()
    val message by vm.message.collectAsState()
    val records by vm.newRecords.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        NavScreen.Dashboard.route,
        NavScreen.History.route,
        NavScreen.Profile.route,
        NavScreen.Settings.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar(
                    containerColor = SprintaBlack,
                    contentColor = SprintaWhite
                ) {
                    NavigationBarItem(
                        selected = currentRoute == NavScreen.Dashboard.route,
                        onClick = {
                            navController.navigate(NavScreen.Dashboard.route) {
                                popUpTo(NavScreen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.dashboard)) },
                        label = { Text(stringResource(R.string.dashboard)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintaBlack,
                            selectedTextColor = SprintaGold,
                            indicatorColor = SprintaGold,
                            unselectedIconColor = SprintaWhite,
                            unselectedTextColor = SprintaWhite
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == NavScreen.History.route,
                        onClick = {
                            navController.navigate(NavScreen.History.route) {
                                popUpTo(NavScreen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.history)) },
                        label = { Text(stringResource(R.string.history)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintaBlack,
                            selectedTextColor = SprintaGold,
                            indicatorColor = SprintaGold,
                            unselectedIconColor = SprintaWhite,
                            unselectedTextColor = SprintaWhite
                        )
                    )
                    // Center Prominent Record Item
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate(NavScreen.Tracking.route)
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SprintaGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Record", tint = SprintaWhite)
                            }
                        },
                        label = { Text("Record", color = SprintaGold, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == NavScreen.Profile.route,
                        onClick = {
                            navController.navigate(NavScreen.Profile.route) {
                                popUpTo(NavScreen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintaBlack,
                            selectedTextColor = SprintaGold,
                            indicatorColor = SprintaGold,
                            unselectedIconColor = SprintaWhite,
                            unselectedTextColor = SprintaWhite
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == NavScreen.Settings.route,
                        onClick = {
                            navController.navigate(NavScreen.Settings.route) {
                                popUpTo(NavScreen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) },
                        label = { Text("More") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SprintaBlack,
                            selectedTextColor = SprintaGold,
                            indicatorColor = SprintaGold,
                            unselectedIconColor = SprintaWhite,
                            unselectedTextColor = SprintaWhite
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavScreen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavScreen.Splash.route) {
                VideoSplashScreen(
                    onSplashComplete = {
                        val destination = if (session != null) NavScreen.Dashboard.route else NavScreen.Auth.route
                        navController.navigate(destination) {
                            popUpTo(NavScreen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavScreen.Auth.route) {
                AuthScreen(
                    vm = vm,
                    onAuthSuccess = {
                        navController.navigate(NavScreen.Dashboard.route) {
                            popUpTo(NavScreen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavScreen.Dashboard.route) {
                DashboardScreen(
                    vm = vm,
                    onStartRunClick = { navController.navigate(NavScreen.Tracking.route) }
                )
            }
            composable(NavScreen.History.route) {
                HistoryScreen(vm = vm)
            }
            composable(NavScreen.Profile.route) {
                za.co.sprinta.app.ui.profile.ProfileScreen(vm = vm)
            }
            composable(NavScreen.EditProfile.route) {
                za.co.sprinta.app.ui.profile.EditProfileScreen(
                    vm = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(NavScreen.Performance.route) {
                za.co.sprinta.app.ui.performance.PerformanceScreen(vm = vm)
            }
            composable(NavScreen.Feed.route) {
                za.co.sprinta.app.ui.social.FeedScreen(vm = vm)
            }
            composable(NavScreen.Leaderboard.route) {
                za.co.sprinta.app.ui.social.LeaderboardScreen(vm = vm)
            }
            composable(NavScreen.About.route) {
                za.co.sprinta.app.ui.about.AboutScreen()
            }
            composable(NavScreen.Settings.route) {
                val prefs by vm.preferences.collectAsState()
                za.co.sprinta.app.ui.settings.SettingsScreen(
                    vm = vm,
                    prefs = prefs,
                    onEditProfileClick = { navController.navigate(NavScreen.EditProfile.route) },
                    onUpgradeClick = { navController.navigate(NavScreen.ProUpgrade.route) },
                    onGearClick = { navController.navigate(NavScreen.Gear.route) },
                    onSmartWatchClick = { navController.navigate(NavScreen.SmartWatch.route) },
                    onAboutClick = { navController.navigate(NavScreen.About.route) },
                    onLogoutClick = {
                        vm.logout()
                        navController.navigate(NavScreen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavScreen.Gear.route) {
                za.co.sprinta.app.ui.gear.GearManagementScreen(
                    vm = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(NavScreen.SmartWatch.route) {
                za.co.sprinta.app.ui.watch.SmartWatchConnectScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(NavScreen.Tracking.route) {
                TrackingScreen(
                    vm = vm,
                    navController = navController,
                    onFinishRun = {
                        navController.navigate(NavScreen.Summary.route) {
                            popUpTo(NavScreen.Tracking.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavScreen.Summary.route) {
                SummaryScreen(
                    vm = vm,
                    onSaved = {
                        navController.navigate(NavScreen.Dashboard.route) {
                            popUpTo(NavScreen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavScreen.ProUpgrade.route) {
                val billingViewModel: SprintaBillingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                ProUpgradeScreen(
                    billingViewModel = billingViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    if (records.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = vm::resetRecordDialog,
            title = { Text("New Personal Record!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SPRINTA detected a new personal best:")
                    records.forEach { record ->
                        AssistChip(onClick = {}, label = { Text("🏆 $record") })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = vm::resetRecordDialog) { Text("OK") }
            }
        )
    }
}

// 4. SCREENS
@Composable
private fun AuthScreen(
    vm: MainViewModel,
    onAuthSuccess: () -> Unit = {}
) {
    val registering by vm.isRegistering.collectAsState()
    val busy by vm.busy.collectAsState()
    val session by vm.session.collectAsState()

    LaunchedEffect(session) {
        if (session != null) {
            onAuthSuccess()
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    "Reset Password",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter your email address and we'll send you a password reset link.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    SprintaTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = stringResource(R.string.email)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotPasswordDialog = false
                        vm.resetPassword(resetEmail)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SprintaGold, contentColor = SprintaWhite)
                ) {
                    Text("Send Reset Link", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showForgotPasswordDialog = false },
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sprinta_flat_logo_transparent),
            contentDescription = "SPRINTA Logo",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )
        Text(
            stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 28.dp)
        )

        if (registering) {
            SprintaTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.name)
            )
            Spacer(Modifier.height(10.dp))
        }

        SprintaTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.email)
        )
        Spacer(Modifier.height(10.dp))
        SprintaTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.password),
            visualTransformation = PasswordVisualTransformation()
        )

        if (registering) {
            Spacer(Modifier.height(10.dp))
            SprintaTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = stringResource(R.string.confirm_password),
                visualTransformation = PasswordVisualTransformation()
            )
        } else {
            TextButton(
                onClick = {
                    resetEmail = email
                    showForgotPasswordDialog = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "Forgot Password?",
                    style = MaterialTheme.typography.labelLarge,
                    color = SprintaGold
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = if (registering) stringResource(R.string.create_account) else stringResource(R.string.login),
            onClick = {
                if (registering) vm.register(name, email, password, confirm)
                else vm.login(email, password)
            },
            enabled = !busy
        )

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = { vm.signInWithGoogle() },
            enabled = !busy,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, BorderGray),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SprintaDarkSurface,
                contentColor = SprintaWhite
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google SSO",
                    tint = SprintaGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SprintaWhite
                )
            }
        }

        if (busy) {
            Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = SprintaGold)
            }
        }

        TextButton(
            onClick = { vm.setRegistering(!registering) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (registering) "Already have an account? ${stringResource(R.string.login)}" else stringResource(R.string.create_account),
                color = SprintaGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DashboardScreen(
    vm: MainViewModel,
    onStartRunClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by vm.session.collectAsState()
    val workouts by vm.workouts.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val name = session?.get("name") ?: "Runner"

    val totalDistance = workouts.sumOf { it.distanceMeters }
    val totalDuration = workouts.sumOf { it.durationSeconds }
    val avgPace = GeoUtils.paceSecondsPerKm(totalDistance, totalDuration)
    val record5k = workouts
        .filter { it.distanceMeters >= 5_000 }
        .minByOrNull { it.averagePaceSecondsPerKm }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Sprinta",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SprintaGold
                    )
                    Text(
                        "Welcome, $name",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.sprinta_logo),
                    contentDescription = "Sprinta Logo",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Weekly Snapshot Section
        item {
            Text(
                "WEEKLY SNAPSHOT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "DISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SprintaWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            formatDistance(totalDistance, prefs.useMiles),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SprintaGold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(BorderGray)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "AVG PACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SprintaWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            GeoUtils.formatPace(avgPace),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SprintaWhite
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(BorderGray)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = SprintaWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            GeoUtils.formatDuration(totalDuration),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SprintaWhite
                        )
                    }
                }
            }
        }

        // Weekly Goal Progress Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val currentKm = (totalDistance / 1000.0).toFloat()
                    val goalKm = 20.0f
                    val progress = (currentKm / goalKm).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WEEKLY GOAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SprintaGold
                        )
                        Text(
                            text = "%.1f / %.0f km".format(currentKm, goalKm),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SprintaWhite
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SprintaGold,
                        trackColor = BorderGray
                    )
                }
            }
        }

        // Gear Tracker Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = "Gear", tint = SprintaGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEAR TRACKER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SprintaGold
                            )
                        }
                        Text(
                            text = "184 / 500 km",
                            style = MaterialTheme.typography.bodySmall,
                            color = SprintaWhite.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = "Nike Air Zoom Pegasus 40",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SprintaWhite
                    )

                    LinearProgressIndicator(
                        progress = { 184f / 500f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SprintaGold,
                        trackColor = BorderGray
                    )
                }
            }
        }

        item {
            PrimaryButton(
                text = stringResource(R.string.start_run),
                onClick = onStartRunClick
            )
        }

        item {
            Text(
                stringResource(R.string.recent_workouts),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (workouts.isEmpty()) {
            item { Text(stringResource(R.string.no_workouts), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)) }
        } else {
            items(workouts.take(6), key = { it.workoutId }) { workout ->
                WorkoutCard(workout, prefs.useMiles)
            }
        }

        // Relocated Personal Records Section at the bottom of the scroll view
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SprintaGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PERSONAL RECORDS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SprintaGold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1K Distance", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SprintaWhite)
                        Text(bestRecord(workouts, 1_000.0)?.let(GeoUtils::formatPace) ?: "No record yet", color = SprintaWhite)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5K Distance", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SprintaWhite)
                        Text(record5k?.let { GeoUtils.formatPace(it.averagePaceSecondsPerKm) } ?: "No record yet", color = SprintaWhite)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10K Distance", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SprintaWhite)
                        Text(bestRecord(workouts, 10_000.0)?.let(GeoUtils::formatPace) ?: "No record yet", color = SprintaWhite)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
    }
}

@Composable
private fun HistoryScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val workouts by vm.workouts.collectAsState()
    val prefs by vm.preferences.collectAsState()
    var showManualDialog by remember { mutableStateOf(false) }

    if (showManualDialog) {
        za.co.sprinta.app.ui.manual.ManualEntryDialog(
            onDismiss = { showManualDialog = false },
            onSave = { activityType, distanceKm, durationMins, dateIso, notes ->
                vm.saveManualWorkout(activityType, distanceKm, durationMins, dateIso, notes)
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Workout History", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                }
                item {
                    SecondaryButton(text = "Synchronise Pending Workouts", onClick = { vm.startSync() })
                }
                items(workouts, key = { it.workoutId }) { workout ->
                    WorkoutCard(workout, prefs.useMiles)
                }
            }

            za.co.sprinta.app.ui.AdSlotBanner(modifier = Modifier.padding(bottom = 16.dp))
        }

        FloatingActionButton(
            onClick = { showManualDialog = true },
            containerColor = SprintaGold,
            contentColor = SprintaWhite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 70.dp, end = 20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Manual Workout", tint = SprintaWhite)
        }
    }
}

@Composable
private fun SettingsScreen(
    vm: MainViewModel,
    prefs: za.co.sprinta.app.prefs.AppPreferences,
    onUpgradeClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Account?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete your SPRINTA account and saved workout data? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        vm.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = SprintaWhite)
                ) {
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaWhite)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SprintaBlack)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.language == "en",
                            onClick = {
                                vm.setLanguage("en")
                                AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("en"))
                            },
                            label = { Text("English") },
                            leadingIcon = { Icon(Icons.Default.Language, null) }
                        )
                        FilterChip(
                            selected = prefs.language == "af",
                            onClick = {
                                vm.setLanguage("af")
                                AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("af"))
                            },
                            label = { Text("Afrikaans") }
                        )
                        FilterChip(
                            selected = prefs.language == "zu",
                            onClick = {
                                vm.setLanguage("zu")
                                AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("zu"))
                            },
                            label = { Text("isiZulu") }
                        )
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaWhite)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.units), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SprintaBlack)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !prefs.useMiles, onClick = { vm.setUseMiles(false) }, label = { Text(stringResource(R.string.km)) })
                        FilterChip(selected = prefs.useMiles, onClick = { vm.setUseMiles(true) }, label = { Text(stringResource(R.string.miles)) })
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaWhite)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.audio_updates), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SprintaBlack)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = prefs.audioIntervalKm == 1, onClick = { vm.setAudioInterval(1) }, label = { Text(stringResource(R.string.every_1km)) })
                        FilterChip(selected = prefs.audioIntervalKm == 2, onClick = { vm.setAudioInterval(2) }, label = { Text(stringResource(R.string.every_2km)) })
                        FilterChip(selected = prefs.audioIntervalKm == 0, onClick = { vm.setAudioInterval(0) }, label = { Text(stringResource(R.string.off)) })
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaWhite)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SprintaBlack)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !prefs.darkMode, onClick = { vm.setDarkMode(false) }, label = { Text(stringResource(R.string.light)) }, leadingIcon = { Icon(Icons.Default.LightMode, null) })
                        FilterChip(selected = prefs.darkMode, onClick = { vm.setDarkMode(true) }, label = { Text(stringResource(R.string.dark)) }, leadingIcon = { Icon(Icons.Default.DarkMode, null) })
                    }
                }
            }
        }

        item {
            PrimaryButton(text = "⭐ Upgrade to SPRINTA Pro", onClick = onUpgradeClick)
        }

        item {
            SecondaryButton(text = stringResource(R.string.logout), onClick = onLogoutClick)
        }

        item {
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Account")
                Spacer(Modifier.width(8.dp))
                Text("Delete Account & Data", fontWeight = FontWeight.Bold)
            }
        }

        item { Text("SPRINTA • Open-Source Coding OPSC 6312", style = MaterialTheme.typography.labelMedium) }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackingScreen(
    vm: MainViewModel,
    navController: NavHostController,
    onFinishRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    val serviceState by TrackingService.trackingState.collectAsState()
    val storeState by vm.trackingState.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedActivityType by remember { mutableStateOf("RUN") }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    var locationPermissionDenied by remember { mutableStateOf(false) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            startLocationService(context)
        } else {
            locationPermissionDenied = true
        }
    }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coords = if (serviceState.routeCoordinates.isNotEmpty()) {
        serviceState.routeCoordinates.map { LatLng(it.first, it.second) }
    } else {
        storeState.points.map { LatLng(it.latitude, it.longitude) }
    }

    val distanceMeters = if (serviceState.isTracking) serviceState.distanceMeters else storeState.distanceMeters
    val durationSeconds = if (serviceState.isTracking) serviceState.durationSeconds else storeState.durationSeconds
    val paceSecondsPerKm = if (serviceState.isTracking) serviceState.currentPaceSecondsPerKm else storeState.paceSecondsPerKm
    val isTrackingActive = serviceState.isTracking || storeState.active

    BackHandler(enabled = isTrackingActive) {
        showBackConfirmDialog = true
    }

    if (showBackConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBackConfirmDialog = false },
            title = {
                Text(
                    "Active Activity in Progress",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    "Your activity is currently being recorded. Do you want to keep recording in the background or finish?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackConfirmDialog = false
                        navController.popBackStack(NavScreen.Dashboard.route, inclusive = false)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SprintaGold,
                        contentColor = SprintaWhite
                    )
                ) {
                    Text("Keep Recording", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showBackConfirmDialog = false
                        stopLocationService(context)
                        onFinishRun()
                    },
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Finish Activity", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 220.dp,
        sheetContainerColor = SprintaDarkSurface.copy(alpha = 0.95f),
        sheetContentColor = SprintaWhite,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(BorderGray, CircleShape)
            )
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Stats Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            LocationUtils.formatDuration(durationSeconds),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = SprintaWhite
                        )
                        Text("TIME", style = MaterialTheme.typography.labelSmall, color = SprintaWhite.copy(alpha = 0.6f))
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(BorderGray)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            formatDistance(distanceMeters, prefs.useMiles),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = SprintaGold
                        )
                        Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = SprintaWhite.copy(alpha = 0.6f))
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(BorderGray)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            LocationUtils.formatPace(paceSecondsPerKm),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = SprintaWhite
                        )
                        Text("PACE", style = MaterialTheme.typography.labelSmall, color = SprintaWhite.copy(alpha = 0.6f))
                    }
                }

                // GPS Indicator Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (coords.isNotEmpty()) Color(0xFF4CAF50) else SprintaGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (coords.isNotEmpty()) "GPS ACQUIRED" else "SEARCHING GPS...",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SprintaWhite.copy(alpha = 0.8f)
                    )
                }

                if (locationPermissionDenied) {
                    Text(
                        stringResource(R.string.location_permission_required),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!isTrackingActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = selectedActivityType == "RUN",
                            onClick = { selectedActivityType = "RUN" },
                            label = { Text("🏃‍♂️ Run") }
                        )
                        FilterChip(
                            selected = selectedActivityType == "WALK",
                            onClick = { selectedActivityType = "WALK" },
                            label = { Text("🚶‍♂️ Walk") }
                        )
                        FilterChip(
                            selected = selectedActivityType == "WORKOUT",
                            onClick = { selectedActivityType = "WORKOUT" },
                            label = { Text("🏋️‍♂️ Workout") }
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            if (hasLocationPermission) {
                                startLocationService(context)
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        *(if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS) else emptyArray())
                                    )
                                )
                            }
                        },
                        containerColor = SprintaGold,
                        contentColor = SprintaWhite,
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Activity", modifier = Modifier.size(42.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            stopLocationService(context)
                            onFinishRun()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SprintaGold,
                            contentColor = SprintaWhite
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = SprintaWhite)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Stop Activity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SprintaWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val initial = coords.lastOrNull() ?: LatLng(-33.9249, 18.4241)

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(initial, 17f)
            }

            LaunchedEffect(coords.lastOrNull()) {
                coords.lastOrNull()?.let { lastLocation ->
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLng(lastLocation),
                        durationMs = 1000
                    )
                }
            }

            // Full Screen Map Background with Brand-Strict Speed-Gradient Polyline
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isBuildingEnabled = false,
                    mapType = mapType,
                    mapStyleOptions = if (mapType == MapType.NORMAL) MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) else null
                )
            ) {
                if (coords.size >= 2) {
                    for (i in 0 until coords.size - 1) {
                        val p1 = coords[i]
                        val p2 = coords[i + 1]
                        val dist = LocationUtils.haversineDistanceMeters(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
                        val isFast = dist > 2.0
                        Polyline(
                            points = listOf(p1, p2),
                            color = if (isFast) SprintaGold else SprintaGold.copy(alpha = 0.5f),
                            width = if (isFast) 16f else 10f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )
                    }
                    val lastPoint = coords.last()
                    val markerState = rememberMarkerState(key = lastPoint.toString(), position = lastPoint)
                    Marker(state = markerState, title = "Current Position")
                } else if (coords.isNotEmpty()) {
                    val lastPoint = coords.last()
                    val markerState = rememberMarkerState(key = lastPoint.toString(), position = lastPoint)
                    Marker(state = markerState, title = "Current Position")
                }
            }

            // Floating Action Buttons on Right
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                    },
                    containerColor = SprintaDarkSurface,
                    contentColor = SprintaGold,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Map Layers")
                }

                FloatingActionButton(
                    onClick = {
                        coords.lastOrNull()?.let { lastLoc ->
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(lastLoc))
                            }
                        }
                    },
                    containerColor = SprintaDarkSurface,
                    contentColor = SprintaGold,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
                }
            }
        }
    }
}

// 5. SUMMARY SCREEN WITH CONFETTI ANIMATION
@Composable
private fun SummaryScreen(
    vm: MainViewModel,
    onSaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by vm.trackingState.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val busy by vm.busy.collectAsState()
    var notes by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as android.app.Activity

    val distanceStr = formatDistance(state.distanceMeters, prefs.useMiles)
    val durationStr = GeoUtils.formatDuration(state.durationSeconds)
    val paceStr = GeoUtils.formatPace(state.paceSecondsPerKm)

    // Celebration Confetti Party with Gold, Black, and White
    val party = remember {
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xFFD8B154.toInt(), 0xFF111111.toInt(), 0xFFFFFFFF.toInt()),
            position = Position.Relative(0.5, 0.25),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(300)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.run_summary),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = SprintaBlack.copy(alpha = 0.6f))
                        Text(distanceStr, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SprintaBlack)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TIME", style = MaterialTheme.typography.labelSmall, color = SprintaBlack.copy(alpha = 0.6f))
                        Text(durationStr, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SprintaBlack)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PACE", style = MaterialTheme.typography.labelSmall, color = SprintaBlack.copy(alpha = 0.6f))
                        Text(paceStr, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SprintaBlack)
                    }
                }
            }

            SprintaTextField(
                value = notes,
                onValueChange = { notes = it },
                label = stringResource(R.string.notes),
                singleLine = false
            )

            ShareRunButton(
                distance = distanceStr,
                duration = durationStr,
                pace = paceStr,
                context = context
            )

            PrimaryButton(
                text = stringResource(R.string.save_sync),
                onClick = {
                    vm.saveCurrentWorkout(notes)
                    za.co.sprinta.app.ads.AdManager.showInterstitialIfReady(activity) {
                        onSaved()
                    }
                },
                enabled = !busy
            )
        }

        KonfettiView(
            parties = listOf(party),
            modifier = Modifier.fillMaxSize()
        )
    }
}

// 6. COMPONENTS & UPGRADE SCREEN
@Composable
private fun WorkoutCard(workout: WorkoutEntity, useMiles: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatDate(workout.startTimeIso),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SprintaGold,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(onClick = {}, label = { Text(if (workout.isSynced) "Synced" else "Pending") })
            }
            Text(formatDistance(workout.distanceMeters, useMiles), color = SprintaWhite, style = MaterialTheme.typography.bodyMedium)
            Text(GeoUtils.formatDuration(workout.durationSeconds), color = SprintaWhite.copy(alpha = 0.8f))
            Text(GeoUtils.formatPace(workout.averagePaceSecondsPerKm), color = SprintaWhite.copy(alpha = 0.8f))
            if (workout.notes.isNotBlank()) Text(workout.notes, color = SprintaWhite.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun Metric(name: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ShareRunButton(
    distance: String,
    duration: String,
    pace: String,
    context: android.content.Context
) {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.sprinta_logo),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Text("JUST CRUSHED IT ON SPRINTA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Metric("Distance", distance)
                    Metric("Time", duration)
                    Metric("Pace", pace)
                }
            }
        }

        SecondaryButton(text = "Share to Instagram / WhatsApp", onClick = {
            scope.launch {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                shareBitmap(context, bitmap)
            }
        })
    }
}

@Composable
fun ProUpgradeScreen(
    billingViewModel: SprintaBillingViewModel,
    onBackClick: () -> Unit
) {
    val isProUser by billingViewModel.isProUser.collectAsState()
    val productDetails by billingViewModel.proProductDetails.collectAsState()
    val context = LocalContext.current as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProUser) {
            Text(
                "You are a SPRINTA Pro Member!",
                color = SprintaGold,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(24.dp))
            SecondaryButton(text = "Back", onClick = onBackClick)
        } else {
            Text(
                "Unlock SPRINTA Pro",
                color = SprintaGold,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "• Advanced Pace Analytics\n• Marathon Training Plans\n• Ad-Free Experience",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { billingViewModel.launchBillingFlow(context) },
                enabled = productDetails != null,
                colors = ButtonDefaults.buttonColors(containerColor = SprintaGold, contentColor = SprintaWhite),
                shape = CircleShape
            ) {
                val price = productDetails?.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)?.formattedPrice ?: "Loading..."
                Text("Subscribe for $price / month", color = SprintaWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(text = "Back", onClick = onBackClick)
        }
    }
}

// 7. HELPER FUNCTIONS
private fun bestRecord(workouts: List<WorkoutEntity>, distanceMeters: Double): Double? {
    return workouts
        .filter { it.distanceMeters >= distanceMeters }
        .minByOrNull { it.averagePaceSecondsPerKm }
        ?.averagePaceSecondsPerKm
}

private fun shareBitmap(context: android.content.Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = FileOutputStream("$cachePath/sprinta_run.png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val imagePath = File(context.cacheDir, "images")
        val newFile = File(imagePath, "sprinta_run.png")
        val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", newFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share your run"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun startLocationService(context: android.content.Context) {
    val intent = Intent(context, TrackingService::class.java).apply {
        action = TrackingService.ACTION_START
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
    else context.startService(intent)
}

private fun stopLocationService(context: android.content.Context) {
    val intent = Intent(context, TrackingService::class.java).apply {
        action = TrackingService.ACTION_STOP
    }
    context.startService(intent)
}

private fun formatDate(isoString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = sdf.parse(isoString) ?: Date()
        SimpleDateFormat("EEE, d MMM yyyy", Locale.US).format(date)
    } catch (e: Exception) {
        isoString
    }
}

private fun formatDistance(meters: Double, miles: Boolean): String {
    val value = if (miles) meters / 1609.344 else meters / 1000.0
    return "%.2f %s".format(Locale.US, value, if (miles) "mi" else "km")
}
