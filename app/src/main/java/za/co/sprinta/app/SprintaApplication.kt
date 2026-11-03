package za.co.sprinta.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import za.co.sprinta.app.ads.AdUnitIds

class SprintaApplication : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isAdLoading = false

    override fun onCreate() {
        super<Application>.onCreate()
        AppGraph.init(this)
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        showAppOpenAdIfAvailable()
    }

    private fun showAppOpenAdIfAvailable() {
        val activity = currentActivity ?: return

        if (appOpenAd != null) {
            appOpenAd?.show(activity)
            appOpenAd = null
            fetchAppOpenAd()
        } else {
            fetchAppOpenAd()
        }
    }

    private fun fetchAppOpenAd() {
        if (isAdLoading || appOpenAd != null) return

        isAdLoading = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            AdUnitIds.APP_OPEN,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                    isAdLoading = false
                }
            }
        )
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
