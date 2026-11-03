package za.co.sprinta.app.ui.pro

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import za.co.sprinta.app.billing.SprintaBillingViewModel
import za.co.sprinta.app.ui.SecondaryButton
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite

@Composable
fun SprintaProScreen(
    billingViewModel: SprintaBillingViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProUser by billingViewModel.isProUser.collectAsState()
    val productDetails by billingViewModel.proProductDetails.collectAsState()
    val context = LocalContext.current as? Activity

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProUser) {
            Text(
                text = "You are a Sprinta Pro Member!",
                color = SprintaGold,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Zero ads active. Premium features unlocked.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            SecondaryButton(text = "Back to App", onClick = onBackClick)
        } else {
            Text(
                text = "Sprinta Pro",
                color = SprintaGold,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Go Pro. Remove all ads. Focus on the run.",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "• 100% Ad-Free Experience\n• Advanced Pace Analytics\n• Marathon & 10K Training Plans\n• Live Safety Beacon Track",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (context != null) {
                        billingViewModel.launchBillingFlow(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = productDetails != null,
                colors = ButtonDefaults.buttonColors(containerColor = SprintaGold, contentColor = SprintaWhite),
                shape = CircleShape
            ) {
                val price = productDetails?.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)?.formattedPrice ?: "Loading..."
                Text(
                    text = if (productDetails != null) "Subscribe for $price / month" else "Loading Play Store...",
                    color = SprintaWhite,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(text = "Back", onClick = onBackClick)
        }
    }
}
