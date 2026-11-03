package za.co.sprinta.app.ui.social

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.sprinta.app.MainViewModel
import za.co.sprinta.app.model.LeaderboardEntry
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaBlack
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun LeaderboardScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by vm.leaderboardUiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "WEEKLY DISTANCE LEADERBOARD",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.isGlobal,
                    onClick = { vm.toggleLeaderboardTab(true) },
                    label = { Text("🌍 Global") }
                )
                FilterChip(
                    selected = !uiState.isGlobal,
                    onClick = { vm.toggleLeaderboardTab(false) },
                    label = { Text("👥 Following") }
                )
            }
        }

        items(
            items = uiState.entries,
            key = { it.userId },
            contentType = { "LeaderboardEntry" }
        ) { entry ->
            LeaderboardEntryCard(entry = entry)
        }
    }
}

@Composable
fun LeaderboardEntryCard(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val rankBadgeColor = when (entry.rank) {
        1 -> SprintaGold
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> BorderGray
    }

    val cardBorder = if (entry.isCurrentUser) {
        BorderStroke(2.dp, SprintaGold)
    } else {
        BorderStroke(1.dp, BorderGray)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) SprintaDarkSurface.copy(alpha = 0.95f) else SprintaDarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(rankBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                if (entry.rank <= 3) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Rank",
                        tint = SprintaBlack,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "#${entry.rank}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SprintaWhite
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.userName + if (entry.isCurrentUser) " (You)" else "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (entry.isCurrentUser) SprintaGold else SprintaWhite
                )
                Text(
                    text = "${entry.totalWorkouts} activities this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = SprintaWhite.copy(alpha = 0.6f)
                )
            }

            Text(
                text = "%.1f km".format(entry.weeklyDistanceKm),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SprintaGold
            )
        }
    }
}
