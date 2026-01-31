package com.zaheer.photocompressor.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * BillingManager - Handles Play Billing integration for Pro version
 */
class BillingManager(
    private val context: Context,
    private val preferencesRepository: com.zaheer.photocompressor.data.PreferencesRepository
) : PurchasesUpdatedListener {

    companion object {
        private const val PRO_PRODUCT_ID = "photo_compressor_pro"
    }

    private var billingClient: BillingClient? = null
    private val _proStatus = MutableStateFlow(false)
    val proStatus: StateFlow<Boolean> = _proStatus.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError: StateFlow<String?> = _purchaseError.asStateFlow()

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryPurchases()
                } else {
                    _purchaseError.value = "Billing setup failed: ${billingResult.debugMessage}"
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection if needed
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRO_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = productDetailsList.firstOrNull()
            } else {
                _purchaseError.value = "Failed to query product details: ${billingResult.debugMessage}"
            }
        }
    }

    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPro = purchases.any { it.products.contains(PRO_PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED }
                _proStatus.value = hasPro
                preferencesRepository.setProStatus(hasPro)
                
                // Acknowledge purchases if needed
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _productDetails.value
        if (productDetails == null) {
            _purchaseError.value = "Product details not available"
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

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                    _proStatus.value = true
                    preferencesRepository.setProStatus(true)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseError.value = null // User canceled, not an error
        } else {
            _purchaseError.value = "Purchase failed: ${billingResult.debugMessage}"
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _purchaseError.value = "Failed to acknowledge purchase: ${billingResult.debugMessage}"
            }
        }
    }

    fun restorePurchases() {
        queryPurchases()
    }

    fun clearError() {
        _purchaseError.value = null
    }

    fun endConnection() {
        billingClient?.endConnection()
    }
}
