package za.co.sprinta.app.ui.gear

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import za.co.sprinta.app.MainViewModel
import za.co.sprinta.app.data.local.GearEntity
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaTextField
import za.co.sprinta.app.ui.SprintaTopAppBar
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun GearManagementScreen(
    vm: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gearList by vm.gearList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var brandInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Custom Running Shoes",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SprintaTextField(
                        value = brandInput,
                        onValueChange = { brandInput = it },
                        label = "Brand (e.g. Hoka, Nike, Adidas)"
                    )
                    SprintaTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        label = "Model (e.g. Clifton 9, Pegasus 40)"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (brandInput.isNotBlank() && modelInput.isNotBlank()) {
                            vm.addCustomGear(brandInput, modelInput)
                            brandInput = ""
                            modelInput = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SprintaGold, contentColor = SprintaWhite)
                ) {
                    Text("Add Shoe", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddDialog = false },
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SprintaTopAppBar(title = "Gear & Shoe Tracker", onBackClick = onBackClick)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SprintaGold,
                contentColor = SprintaWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Shoe")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "ACTIVE RUNNING SHOES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            if (gearList.isEmpty()) {
                item {
                    Text(
                        text = "No custom shoes added yet. Tap + to add your running shoes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(gearList, key = { it.shoeId }) { gear ->
                    GearCard(
                        gear = gear,
                        onSetDefault = { vm.setDefaultGear(gear.shoeId) }
                    )
                }
            }
        }
    }
}

@Composable
fun GearCard(
    gear: GearEntity,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (gear.totalKm / gear.maxKm).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (gear.isDefault) SprintaGold else BorderGray),
        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = SprintaGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = gear.brand.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SprintaGold
                    )
                }
                if (gear.isDefault) {
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text("Active Default", fontWeight = FontWeight.Bold) }
                    )
                } else {
                    OutlinedButton(
                        onClick = onSetDefault,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text("Make Default", color = SprintaWhite, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Text(
                text = gear.model,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SprintaWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "%.1f / %.0f km logged".format(gear.totalKm, gear.maxKm),
                    style = MaterialTheme.typography.bodySmall,
                    color = SprintaWhite.copy(alpha = 0.8f)
                )
                Text(
                    text = "${(progress * 100).toInt()}% Wear",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = SprintaGold
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
