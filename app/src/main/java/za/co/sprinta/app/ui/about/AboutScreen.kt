package za.co.sprinta.app.ui.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import za.co.sprinta.app.R
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sprinta_flat_logo_transparent),
            contentDescription = "SPRINTA Logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SPRINTA",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = SprintaGold
        )
        Text(
            text = "Version 1.0.0 (Build 101)",
            style = MaterialTheme.typography.bodyMedium,
            color = SprintaWhite.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray),
            colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ABOUT THE APPLICATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SprintaGold
                )
                Text(
                    text = "SPRINTA is a modern, high-performance athletic activity tracking platform designed for multi-sport GPS logging, offline telemetry sync, and personal record benchmarking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SprintaWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DEVELOPER INFO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SprintaGold
                )
                Text(
                    text = "Open-Source Coding • OPSC 6312 POE Part 2\nBuilt with Kotlin, Jetpack Compose, Room & Firebase.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SprintaWhite
                )
            }
        }
    }
}
