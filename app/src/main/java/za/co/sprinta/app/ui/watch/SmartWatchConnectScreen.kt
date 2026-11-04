package za.co.sprinta.app.ui.watch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaBlack
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaTopAppBar
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun SmartWatchConnectScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isGarminConnected by remember { mutableStateOf(false) }
    var isGalaxyWatchConnected by remember { mutableStateOf(true) }

    var syncHeartRate by remember { mutableStateOf(true) }
    var syncCadence by remember { mutableStateOf(true) }
    var syncSteps by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SprintaTopAppBar(title = "Smart Watch Integration", onBackClick = onBackClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CONNECTED HARDWARE DEVICES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isGarminConnected) SprintaGold else BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Watch, contentDescription = "Garmin", tint = SprintaGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GARMIN CONNECT API",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SprintaWhite
                            )
                        }
                        if (isGarminConnected) {
                            FilterChip(
                                selected = true,
                                onClick = { },
                                label = { Text("Connected ⚡ 84%", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Text(
                        text = "Sync heart rate, VO2 max, and GPS route logs from Forerunner / Fenix series.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SprintaWhite.copy(alpha = 0.7f)
                    )

                    if (isGarminConnected) {
                        OutlinedButton(
                            onClick = { isGarminConnected = false },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Text("Disconnect Garmin", color = SprintaWhite)
                        }
                    } else {
                        Button(
                            onClick = { isGarminConnected = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SprintaGold, contentColor = SprintaWhite)
                        ) {
                            Text("Link Garmin Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isGalaxyWatchConnected) SprintaGold else BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Watch, contentDescription = "Galaxy Watch", tint = SprintaGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SAMSUNG GALAXY WATCH",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SprintaWhite
                            )
                        }
                        if (isGalaxyWatchConnected) {
                            FilterChip(
                                selected = true,
                                onClick = { },
                                label = { Text("Linked • Active", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Text(
                        text = "Samsung Health SDK integration for real-time PPG heart rate and cadence sensor streams.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SprintaWhite.copy(alpha = 0.7f)
                    )

                    if (isGalaxyWatchConnected) {
                        OutlinedButton(
                            onClick = { isGalaxyWatchConnected = false },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Text("Disconnect Galaxy Watch", color = SprintaWhite)
                        }
                    } else {
                        Button(
                            onClick = { isGalaxyWatchConnected = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SprintaGold, contentColor = SprintaWhite)
                        ) {
                            Text("Connect Galaxy Watch", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(
                text = "TELEMETRY SYNC PREFERENCES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Live Heart Rate (BPM)", fontWeight = FontWeight.Bold, color = SprintaWhite)
                            Text("Continuous Optical HR sensor tracking", style = MaterialTheme.typography.bodySmall, color = SprintaWhite.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = syncHeartRate,
                            onCheckedChange = { syncHeartRate = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SprintaBlack, checkedTrackColor = SprintaGold)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Running Cadence (SPM)", fontWeight = FontWeight.Bold, color = SprintaWhite)
                            Text("Steps per minute via wrist accelerometer", style = MaterialTheme.typography.bodySmall, color = SprintaWhite.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = syncCadence,
                            onCheckedChange = { syncCadence = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SprintaBlack, checkedTrackColor = SprintaGold)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Daily Background Steps", fontWeight = FontWeight.Bold, color = SprintaWhite)
                            Text("All-day step count synchronization", style = MaterialTheme.typography.bodySmall, color = SprintaWhite.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = syncSteps,
                            onCheckedChange = { syncSteps = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SprintaBlack, checkedTrackColor = SprintaGold)
                        )
                    }
                }
            }
        }
    }
}
