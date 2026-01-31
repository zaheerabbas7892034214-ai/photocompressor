package com.zaheer.photocompressor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.zaheer.photocompressor.billing.BillingManager
import com.zaheer.photocompressor.ui.navigation.AppNav
import com.zaheer.photocompressor.ui.theme.PhotoCompressorTheme
import com.zaheer.photocompressor.ui.viewmodel.CompressorViewModel
import com.zaheer.photocompressor.utils.ShareUtils

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: CompressorViewModel
    private lateinit var billingManager: BillingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CompressorViewModel::class.java]
        
        // Initialize Billing Manager
        billingManager = BillingManager(applicationContext)
        billingManager.initialize()
        
        // Clean up old cached files
        ShareUtils.cleanupCache(applicationContext)
        
        setContent {
            PhotoCompressorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(
                        viewModel = viewModel,
                        billingManager = billingManager,
                        onUnlockPro = {
                            billingManager.launchPurchaseFlow(this@MainActivity)
                        }
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        billingManager.cleanup()
    }
}
