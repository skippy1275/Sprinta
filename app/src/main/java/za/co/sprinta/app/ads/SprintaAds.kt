package za.co.sprinta.app.ads

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import za.co.sprinta.app.ui.BorderGray
import za.co.sprinta.app.ui.SprintaDarkSurface
import za.co.sprinta.app.ui.SprintaGold
import za.co.sprinta.app.ui.SprintaWhite

object AdUnitIds {
    const val APP_OPEN = "ca-app-pub-5833611172742289/8627849847"
    const val BANNER = "ca-app-pub-5833611172742289/3073358578"
    const val INTERSTITIAL = "ca-app-pub-5833611172742289/4286365295"
    const val NATIVE_ADVANCED = "ca-app-pub-5833611172742289/5140030975"
    const val REWARDED_INTERSTITIAL = "ca-app-pub-5833611172742289/6464720939"
}

@Composable
fun SprintaBannerAd(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdUnitIds.BANNER
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun SprintaNativeAdCard(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = SprintaDarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPONSORED ATHLETE CONTENT",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SprintaGold
                )
                Text(
                    text = "Ad",
                    style = MaterialTheme.typography.labelSmall,
                    color = SprintaWhite.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "Fuel Your Run With Sprinta Pro Nutrition",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SprintaWhite
            )
            Text(
                text = "Advanced electrolyte formulas engineered for endurance athletes.",
                style = MaterialTheme.typography.bodySmall,
                color = SprintaWhite.copy(alpha = 0.7f)
            )
        }
    }
}

fun showInterstitialAd(
    context: Context,
    isPremium: Boolean,
    onAdDismissed: () -> Unit = {}
) {
    if (isPremium) {
        onAdDismissed()
        return
    }

    val activity = context as? android.app.Activity
    if (activity == null) {
        onAdDismissed()
        return
    }

    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        AdUnitIds.INTERSTITIAL,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                interstitialAd.show(activity)
                onAdDismissed()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                onAdDismissed()
            }
        }
    )
}

fun showRewardedAd(
    context: Context,
    isPremium: Boolean,
    onRewardEarned: () -> Unit
) {
    if (isPremium) {
        onRewardEarned()
        return
    }

    val activity = context as? android.app.Activity
    if (activity == null) {
        onRewardEarned()
        return
    }

    val adRequest = AdRequest.Builder().build()
    RewardedInterstitialAd.load(
        context,
        AdUnitIds.REWARDED_INTERSTITIAL,
        adRequest,
        object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                rewardedAd.show(activity) {
                    onRewardEarned()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                onRewardEarned()
            }
        }
    )
}
