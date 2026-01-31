package com.zaheer.photocompressor.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Google Play Billing for in-app purchases
 */
class BillingManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "billing_prefs"
        private const val KEY_IS_PRO = "is_pro"
        const val PRODUCT_ID = "photo_compressor_pro"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _isProActive = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()
    
    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()
    
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()
    
    private var billingClient: BillingClient? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Error("Purchase cancelled")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingState.value = BillingState.Error("Already purchased")
                setProActive(true)
            }
            else -> {
                _billingState.value = BillingState.Error("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Initialize billing client and connect
     */
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        connectToBilling()
    }

    private fun connectToBilling() {
        _billingState.value = BillingState.Connecting
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingState.value = BillingState.Connected
                    queryProductDetails()
                    restorePurchases()
                } else {
                    _billingState.value = BillingState.Error("Billing unavailable: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
                // Retry connection
            }
        })
    }

    /**
     * Query product details from Play Store
     */
    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList.firstOrNull()
                if (productDetailsList.isEmpty()) {
                    _billingState.value = BillingState.Error("Product not found")
                }
            } else {
                _billingState.value = BillingState.Error("Failed to query products: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launch purchase flow
     */
    fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _productDetails.value
        if (productDetails == null) {
            _billingState.value = BillingState.Error("Product not available")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _billingState.value = BillingState.PurchaseInProgress
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Handle purchase after successful transaction
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            
            if (purchase.products.contains(PRODUCT_ID)) {
                setProActive(true)
                _billingState.value = BillingState.PurchaseSuccess
            }
        }
    }

    /**
     * Acknowledge purchase if needed
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Purchase acknowledged
            }
        }
    }

    /**
     * Restore purchases from Play Store
     */
    fun restorePurchases() {
        _billingState.value = BillingState.RestoringPurchases
        
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var foundPurchase = false
                purchases.forEach { purchase ->
                    if (purchase.products.contains(PRODUCT_ID) && 
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                        foundPurchase = true
                    }
                }
                
                if (foundPurchase) {
                    _billingState.value = BillingState.RestoreSuccess
                } else {
                    _billingState.value = BillingState.Error("No purchases found")
                }
            } else {
                _billingState.value = BillingState.Error("Failed to restore: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Set pro status and persist
     */
    private fun setProActive(isActive: Boolean) {
        _isProActive.value = isActive
        prefs.edit().putBoolean(KEY_IS_PRO, isActive).apply()
    }

    /**
     * Clean up billing client
     */
    fun cleanup() {
        billingClient?.endConnection()
        billingClient = null
    }
}

/**
 * Billing state enum
 */
sealed class BillingState {
    object Idle : BillingState()
    object Connecting : BillingState()
    object Connected : BillingState()
    object Disconnected : BillingState()
    object PurchaseInProgress : BillingState()
    object PurchaseSuccess : BillingState()
    object RestoringPurchases : BillingState()
    object RestoreSuccess : BillingState()
    data class Error(val message: String) : BillingState()
}
