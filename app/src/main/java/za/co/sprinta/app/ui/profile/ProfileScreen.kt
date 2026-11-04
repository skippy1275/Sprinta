package za.co.sprinta.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.sprinta.app.MainViewModel
import za.co.sprinta.app.data.local.WorkoutEntity
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaBlack
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite
import za.co.sprinta.app.util.GeoUtils

@Composable
fun ProfileScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val session by vm.session.collectAsState()
    val workouts by vm.workouts.collectAsState()
    val prefs by vm.preferences.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Progress", "Activities")

    val userName = session?.get("name") ?: "Athlete"
    val email = session?.get("email") ?: "athlete@sprinta.app"

    val totalRuns = workouts.size
    val totalMeters = workouts.sumOf { it.distanceMeters }
    val totalSeconds = workouts.sumOf { it.durationSeconds }
    val avgPace = GeoUtils.paceSecondsPerKm(totalMeters, totalSeconds)

    val distanceKm = if (prefs.useMiles) totalMeters / 1609.344 else totalMeters / 1000.0
    val unitLabel = if (prefs.useMiles) "mi" else "km"

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SprintaDarkSurface,
            contentColor = SprintaGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = SprintaGold
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTabIndex == index) SprintaGold else SprintaWhite.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTabIndex == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SprintaGold)
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Avatar",
                                    tint = SprintaBlack,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = SprintaWhite
                                )
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SprintaWhite.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚡ 3-WEEK STREAK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SprintaGold
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "ALL-TIME STATS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Activities", color = SprintaWhite.copy(alpha = 0.7f))
                                Text("$totalRuns", fontWeight = FontWeight.Bold, color = SprintaWhite)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Distance", color = SprintaWhite.copy(alpha = 0.7f))
                                Text("%.2f %s".format(distanceKm, unitLabel), fontWeight = FontWeight.Bold, color = SprintaGold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Time", color = SprintaWhite.copy(alpha = 0.7f))
                                Text(GeoUtils.formatDuration(totalSeconds), fontWeight = FontWeight.Bold, color = SprintaWhite)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Average Pace", color = SprintaWhite.copy(alpha = 0.7f))
                                Text(GeoUtils.formatPace(avgPace), fontWeight = FontWeight.Bold, color = SprintaWhite)
                            }
                        }
                    }
                }

                // Dynamic Trophy Cabinet
                item {
                    val badges by vm.badges.collectAsState()

                    Text(
                        text = "TROPHY CABINET",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(badges, key = { it.id }) { badge ->
                            Card(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(130.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (badge.isUnlocked) SprintaGold else BorderGray
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (badge.isUnlocked) SprintaDarkSurface else SprintaBlack
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = badge.emoji,
                                        fontSize = 32.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (badge.isUnlocked) SprintaGold else SprintaWhite.copy(alpha = 0.4f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "12-WEEK TRAINING VOLUME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val volumes = remember { listOf(12f, 18f, 25f, 30f, 22f, 15f, 35f, 28f, 40f, 32f, 45f, 50f) }
                            val maxVol = remember(volumes) { (volumes.maxOrNull() ?: 1f).coerceAtLeast(1f) }

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                val width = size.width
                                val height = size.height
                                val stepX = width / (volumes.size - 1)

                                val strokePath = Path()
                                val fillPath = Path()
                                val nodePositions = mutableListOf<Offset>()

                                volumes.forEachIndexed { i, vol ->
                                    val x = i * stepX
                                    val y = height - (vol / maxVol * height * 0.8f) - 10f
                                    nodePositions.add(Offset(x, y))

                                    if (i == 0) {
                                        strokePath.moveTo(x, y)
                                        fillPath.moveTo(x, height)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        val prevX = (i - 1) * stepX
                                        val prevY = height - (volumes[i - 1] / maxVol * height * 0.8f) - 10f
                                        val controlX = (prevX + x) / 2f
                                        strokePath.cubicTo(controlX, prevY, controlX, y, x, y)
                                        fillPath.cubicTo(controlX, prevY, controlX, y, x, y)
                                    }
                                }

                                fillPath.lineTo(width, height)
                                fillPath.close()

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            SprintaGold.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )

                                drawPath(
                                    path = strokePath,
                                    color = SprintaGold,
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                nodePositions.forEach { pos ->
                                    drawCircle(
                                        color = SprintaGold,
                                        radius = 5.dp.toPx(),
                                        center = pos
                                    )
                                    drawCircle(
                                        color = SprintaDarkSurface,
                                        radius = 2.dp.toPx(),
                                        center = pos
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Weekly Volume Trend ($unitLabel)",
                                style = MaterialTheme.typography.bodySmall,
                                color = SprintaWhite.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "RECENT ACTIVITIES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                }

                if (workouts.isEmpty()) {
                    item {
                        Text(
                            text = "No activities logged yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SprintaWhite.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    items(workouts, key = { it.workoutId }) { workout ->
                        WorkoutActivityRow(workout = workout, useMiles = prefs.useMiles)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutActivityRow(workout: WorkoutEntity, useMiles: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${workout.activityType} • ${GeoUtils.formatDuration(workout.durationSeconds)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SprintaGold
            )
            Text(
                text = "Distance: ${if (useMiles) "%.2f mi".format(workout.distanceMeters / 1609.344) else "%.2f km".format(workout.distanceMeters / 1000.0)}",
                style = MaterialTheme.typography.bodyMedium,
                color = SprintaWhite
            )
            Text(
                text = "Pace: ${GeoUtils.formatPace(workout.averagePaceSecondsPerKm)}",
                style = MaterialTheme.typography.bodySmall,
                color = SprintaWhite.copy(alpha = 0.7f)
            )
        }
    }
}
