package za.co.sprinta.app.ui.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaTextField
import za.co.sprinta.app.ui.SprintaWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onSave: (activityType: String, distanceKm: Double, durationMins: Long, dateIso: String, notes: String) -> Unit
) {
    var selectedActivity by remember { mutableStateOf("RUN") }
    var expandedDropdown by remember { mutableStateOf(false) }

    var distanceText by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("") }
    var minsText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val activityOptions = listOf(
        "RUN" to "🏃‍♂️ Run",
        "WALK" to "🚶‍♂️ Walk",
        "WORKOUT" to "🏋️‍♂️ Workout"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Manual Workout Entry",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "ACTIVITY TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDropdown = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                activityOptions.firstOrNull { it.first == selectedActivity }?.second ?: "🏃‍♂️ Run",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SprintaWhite
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select", tint = SprintaGold)
                        }
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        activityOptions.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedActivity = type
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                SprintaTextField(
                    value = distanceText,
                    onValueChange = { distanceText = it },
                    label = "Distance (km)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SprintaTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        label = "Hours",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    SprintaTextField(
                        value = minsText,
                        onValueChange = { minsText = it },
                        label = "Minutes",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                SprintaTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = "Notes",
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dist = distanceText.toDoubleOrNull() ?: 0.0
                    val hours = hoursText.toLongOrNull() ?: 0L
                    val mins = minsText.toLongOrNull() ?: 0L
                    val totalMins = (hours * 60L) + mins
                    val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

                    onSave(selectedActivity, dist, totalMins, nowIso, notesText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SprintaGold, contentColor = SprintaWhite)
            ) {
                Text("Save Workout", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}
