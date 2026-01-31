package com.zaheer.photocompressor.presentation.pro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.photocompressor.billing.BillingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProUiState(
    val isPro: Boolean = false,
    val productPrice: String? = null,
    val error: String? = null
)

class ProViewModel(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProUiState())
    val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            billingManager.proStatus.collect { isPro ->
                _uiState.value = _uiState.value.copy(isPro = isPro)
            }
        }

        viewModelScope.launch {
            billingManager.productDetails.collect { details ->
                val price = details?.oneTimePurchaseOfferDetails?.formattedPrice
                _uiState.value = _uiState.value.copy(productPrice = price)
            }
        }

        viewModelScope.launch {
            billingManager.purchaseError.collect { error ->
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }

    fun clearError() {
        billingManager.clearError()
    }
}
