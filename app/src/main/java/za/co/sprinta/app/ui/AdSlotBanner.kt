package za.co.sprinta.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import za.co.sprinta.app.ads.AdManager

@Composable
fun AdSlotBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.BANNER_AD_UNIT_ID
) {
    AdSlot(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    setAdUnitId(adUnitId)
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {}
                        override fun onAdFailedToLoad(error: LoadAdError) {}
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
