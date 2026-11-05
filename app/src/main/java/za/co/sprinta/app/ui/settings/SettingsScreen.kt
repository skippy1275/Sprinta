package za.co.sprinta.app.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.sprinta.app.MainViewModel
import za.co.sprinta.app.prefs.AppPreferences
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaBlack
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaError
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun SettingsScreen(
    vm: MainViewModel,
    prefs: AppPreferences,
    onEditProfileClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onGearClick: () -> Unit = {},
    onSmartWatchClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by vm.session.collectAsState()
    val email = session?.get("email") ?: "athlete@sprinta.app"
    var showDeleteDialog by remember { mutableStateOf(false) }

    var isAutoPauseEnabled by remember { mutableStateOf(true) }
    var isAudioCuesEnabled by remember { mutableStateOf(prefs.audioIntervalKm > 0) }
    var isDarkTheme by remember { mutableStateOf(prefs.darkMode) }
    var isPrivateProfile by remember { mutableStateOf(false) }
    var useMetric by remember { mutableStateOf(!prefs.useMiles) }

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
                    "Are you sure you want to permanently delete your Sprinta account and saved workout data? This action cannot be undone.",
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
                    colors = ButtonDefaults.buttonColors(containerColor = SprintaError, contentColor = SprintaWhite)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SprintaBlack)
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = SprintaWhite
        )

        SettingsSectionHeader(title = "ACCOUNT & SUBSCRIPTION")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
        ) {
            Column {
                SettingsClickableItem(
                    title = "Edit Profile",
                    subtitle = "$email • Edit details & photo",
                    icon = Icons.Default.Person,
                    onClick = onEditProfileClick
                )
                SettingsClickableItem(
                    title = "Sprinta Pro Subscription",
                    subtitle = "Manage premium features",
                    icon = Icons.Default.Star,
                    iconTint = SprintaGold,
                    onClick = onUpgradeClick
                )
            }
        }

        SettingsSectionHeader(title = "TRACKING & SENSORS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
        ) {
            Column {
                SettingsClickableItem(
                    title = "Gear & Shoe Tracker",
                    subtitle = "Manage running shoe wear & mileage",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    onClick = onGearClick
                )
                SettingsClickableItem(
                    title = "Smart Watch Hardware",
                    subtitle = "Link Garmin & Galaxy Watch telemetry",
                    icon = Icons.Default.Watch,
                    onClick = onSmartWatchClick
                )
                SettingsToggleItem(
                    title = "Auto-Pause",
                    subtitle = "Pause recording when you stop moving",
                    icon = Icons.Default.PauseCircle,
                    isChecked = isAutoPauseEnabled,
                    onCheckedChange = { isAutoPauseEnabled = it }
                )
                SettingsToggleItem(
                    title = "Audio Cues",
                    subtitle = "Pace and distance announcements",
                    icon = Icons.Default.VolumeUp,
                    isChecked = isAudioCuesEnabled,
                    onCheckedChange = {
                        isAudioCuesEnabled = it
                        vm.setAudioInterval(if (it) 1 else 0)
                    }
                )
                SettingsClickableItem(
                    title = "Bluetooth Sensors",
                    subtitle = "Connect HR monitors & foot pods",
                    icon = Icons.Default.Bluetooth,
                    onClick = { }
                )
            }
        }

        SettingsSectionHeader(title = "PREFERENCES")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
        ) {
            Column {
                SettingsClickableItem(
                    title = "Distance Units",
                    subtitle = if (useMetric) "Kilometers (km)" else "Miles (mi)",
                    icon = Icons.Default.Straighten,
                    onClick = {
                        useMetric = !useMetric
                        vm.setUseMiles(!useMetric)
                    }
                )
                SettingsToggleItem(
                    title = "Dark Theme",
                    subtitle = "Force dark appearance",
                    icon = Icons.Default.DarkMode,
                    isChecked = isDarkTheme,
                    onCheckedChange = {
                        isDarkTheme = it
                        vm.setDarkMode(it)
                    }
                )
                SettingsClickableItem(
                    title = "App Information",
                    subtitle = "Version, terms & developer info",
                    icon = Icons.Default.Info,
                    onClick = onAboutClick
                )
            }
        }

        SettingsSectionHeader(title = "PRIVACY & SAFETY")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
        ) {
            Column {
                SettingsToggleItem(
                    title = "Private Profile",
                    subtitle = "Only followers can see your activities",
                    icon = Icons.Default.Lock,
                    isChecked = isPrivateProfile,
                    onCheckedChange = { isPrivateProfile = it }
                )
                SettingsClickableItem(
                    title = "Beacon / Live Track",
                    subtitle = "Share live location with emergency contacts",
                    icon = Icons.Default.LocationOn,
                    onClick = { }
                )
            }
        }

        SettingsSectionHeader(title = "DATA & STORAGE")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
        ) {
            Column {
                SettingsClickableItem(
                    title = "Export Data",
                    subtitle = "Download activity GPX / TCX logs",
                    icon = Icons.Default.Download,
                    onClick = { }
                )
                SettingsClickableItem(
                    title = "Clear App Cache",
                    subtitle = "Free up offline tile storage",
                    icon = Icons.Default.Delete,
                    onClick = { }
                )
            }
        }

        SettingsSectionHeader(title = "ACTIONS")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, BorderGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SprintaWhite)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Log Out")
                Spacer(Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, SprintaError),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SprintaError)
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Delete Account", tint = SprintaError)
                Spacer(Modifier.width(8.dp))
                Text("Delete Account & Data", fontWeight = FontWeight.Bold, color = SprintaError)
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Sprinta • Open-Source Coding OPSC 6312",
            style = MaterialTheme.typography.bodySmall,
            color = SprintaWhite.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = SprintaGold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SprintaGold,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = SprintaWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SprintaWhite.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SprintaBlack,
                checkedTrackColor = SprintaGold,
                uncheckedThumbColor = SprintaWhite.copy(alpha = 0.6f),
                uncheckedTrackColor = BorderGray
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = SprintaGold,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = SprintaWhite
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SprintaWhite.copy(alpha = 0.6f)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SprintaWhite.copy(alpha = 0.4f)
        )
    }
}
