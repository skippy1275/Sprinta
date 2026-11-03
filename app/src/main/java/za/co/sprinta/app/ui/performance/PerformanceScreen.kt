package za.co.sprinta.app.ui.performance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.sprinta.app.MainViewModel
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun PerformanceScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by vm.performanceUiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "PERFORMANCE ANALYTICS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RACE PREDICTOR",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SprintaGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PredictorBlock("5K", state.predicted5k, Modifier.weight(1f))
                        PredictorBlock("10K", state.predicted10k, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PredictorBlock("HALF MARATHON", state.predictedHalf, Modifier.weight(1f))
                        PredictorBlock("MARATHON", state.predictedMarathon, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESTIMATED VO₂ MAX",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SprintaWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Superior Fitness",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SprintaGold
                        )
                    }
                    Text(
                        text = "${state.estimatedVo2Max}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                        color = SprintaGold
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HR ZONE DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SprintaGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val zoneColors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF4CAF50),
                        Color(0xFFFFEB3B),
                        Color(0xFFFF9800),
                        Color(0xFFF44336)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        state.hrZonePercentages.forEachIndexed { idx, pct ->
                            Box(
                                modifier = Modifier
                                    .weight(pct.coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(zoneColors[idx])
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Z1 15%", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2196F3))
                        Text("Z2 25%", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                        Text("Z3 35%", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFEB3B))
                        Text("Z4 15%", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF9800))
                        Text("Z5 10%", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF44336))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "12-WEEK TRAINING PROGRESSION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SprintaGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val points = state.weeklyVolumeKm
                    val maxVal = remember(points) { (points.maxOrNull() ?: 1f).coerceAtLeast(1f) }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val stepX = width / (points.size - 1)

                        val path = Path()
                        val fillPath = Path()

                        points.forEachIndexed { i, valKm ->
                            val x = i * stepX
                            val y = height - (valKm / maxVal * height * 0.8f)

                            if (i == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, height)
                                fillPath.lineTo(x, y)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevY = height - (points[i - 1] / maxVal * height * 0.8f)
                                val controlX = (prevX + x) / 2f
                                path.cubicTo(controlX, prevY, controlX, y, x, y)
                                fillPath.cubicTo(controlX, prevY, controlX, y, x, y)
                            }
                        }

                        fillPath.lineTo(width, height)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(SprintaGold.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )

                        drawPath(
                            path = path,
                            color = SprintaGold,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictorBlock(
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SprintaWhite.copy(alpha = 0.6f)
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = SprintaWhite
        )
    }
}
