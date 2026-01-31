package com.zaheer.photocompressor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zaheer.photocompressor.billing.BillingManager
import com.zaheer.photocompressor.data.PreferencesRepository
import com.zaheer.photocompressor.presentation.home.HomeScreen
import com.zaheer.photocompressor.presentation.pro.ProScreen
import com.zaheer.photocompressor.presentation.pro.ProViewModel
import com.zaheer.photocompressor.ui.theme.PhotoCompressorTheme

class MainActivity : ComponentActivity() {

    lateinit var billingManager: BillingManager
    private lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize repositories and billing
        preferencesRepository = PreferencesRepository(this)
        billingManager = BillingManager(this, preferencesRepository)

        setContent {
            PhotoCompressorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhotoCompressorApp()
                }
            }
        }
    }

    @Composable
    fun PhotoCompressorApp() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onNavigateToPro = {
                        navController.navigate("pro")
                    }
                )
            }
            composable("pro") {
                val proViewModel: ProViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return ProViewModel(billingManager) as T
                        }
                    }
                )
                ProScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    viewModel = proViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}
