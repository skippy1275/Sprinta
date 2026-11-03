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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import za.co.sprinta.app.MainViewModel
import za.co.sprinta.app.model.SocialPost
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaBlack
import za.co.sprinta.app.ui.SprintaDarkCard
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun FeedScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by vm.feedUiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "COMMUNITY ATHLETE FEED",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }

        items(
            items = uiState.posts,
            key = { it.id },
            contentType = { "SocialPost" }
        ) { post ->
            SocialPostCard(
                post = post,
                onKudosClick = { vm.toggleKudos(post.id) }
            )
        }
    }
}

@Composable
fun SocialPostCard(
    post: SocialPost,
    onKudosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SprintaGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = SprintaBlack,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SprintaWhite
                    )
                    Text(
                        text = "${post.activityType} • ${post.timestampFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SprintaWhite.copy(alpha = 0.6f)
                    )
                }
            }

            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SprintaWhite
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = CardDefaults.cardColors(containerColor = SprintaDarkCard)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map Snapshot",
                            tint = SprintaGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "GPS ROUTE SNAPSHOT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SprintaWhite.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = post.distanceFormatted,
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
                        text = post.paceFormatted,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = SprintaWhite
                    )
                    Text("PACE", style = MaterialTheme.typography.labelSmall, color = SprintaWhite.copy(alpha = 0.6f))
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(BorderGray)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = post.durationFormatted,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = SprintaWhite
                    )
                    Text("TIME", style = MaterialTheme.typography.labelSmall, color = SprintaWhite.copy(alpha = 0.6f))
                }
            }

            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onKudosClick) {
                        Icon(
                            imageVector = if (post.isKudosGiven) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Kudos",
                            tint = if (post.isKudosGiven) SprintaGold else SprintaWhite.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "${post.kudosCount} Kudos",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (post.isKudosGiven) SprintaGold else SprintaWhite.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = SprintaWhite.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "Comment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SprintaWhite.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
