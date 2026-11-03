package za.co.sprinta.app.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SprintaBillingViewModel(application: Application) : AndroidViewModel(application), PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(application)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser

    private val _proProductDetails = MutableStateFlow<ProductDetails?>(null)
    val proProductDetails: StateFlow<ProductDetails?> = _proProductDetails

    init {
        connectToPlayBilling()
    }

    private fun connectToPlayBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySubscriptionProducts()
                    checkExistingPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Retry connection logic here in production
            }
        })
    }

    private fun querySubscriptionProducts() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("sprinta_pro_monthly") // This must match your Google Play Console ID
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, productDetailsList ->
            if (productDetailsList.isNotEmpty()) {
                _proProductDetails.value = productDetailsList[0]
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        val productDetails = _proProductDetails.value ?: return
        val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgePurchase(purchase)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _isProUser.value = true
                    }
                }
            } else {
                _isProUser.value = true
            }
        }
    }

    private fun checkExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { _, purchases ->
            val hasActiveSub = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            _isProUser.value = hasActiveSub
        }
    }
}
