package za.co.sprinta.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.UserMessagingPlatform

object AdManager {
    const val BANNER_AD_UNIT_ID = "ca-app-pub-5833611172742289/3073358578"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5833611172742289/4286365295"

    private var interstitialAd: InterstitialAd? = null
    private var isInitializing = false

    fun gatherConsentAndInitialize(activity: Activity) {
        if (isInitializing) return
        isInitializing = true

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = com.google.android.ump.ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    initMobileAds(activity.applicationContext)
                }
            },
            { _ ->
                initMobileAds(activity.applicationContext)
            }
        )
    }

    private fun initMobileAds(context: Context) {
        MobileAds.initialize(context) {
            preloadInterstitial(context)
        }
    }

    fun preloadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitialIfReady(activity: Activity, onAdDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preloadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            onAdDismissed()
        }
    }
}
