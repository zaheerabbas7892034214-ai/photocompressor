package com.zaheer.photocompressor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zaheer.photocompressor.billing.BillingManager
import com.zaheer.photocompressor.ui.screens.HomeScreen
import com.zaheer.photocompressor.ui.screens.ResultScreen
import com.zaheer.photocompressor.ui.viewmodel.CompressorViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Result : Screen("result")
}

@Composable
fun AppNav(
    viewModel: CompressorViewModel,
    billingManager: BillingManager
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val isProActive by billingManager.isProActive.collectAsState()
    val billingState by billingManager.billingState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                uiState = uiState,
                billingManager = billingManager,
                isProActive = isProActive,
                billingState = billingState,
                onNavigateToResult = {
                    navController.navigate(Screen.Result.route)
                }
            )
        }
        
        composable(Screen.Result.route) {
            ResultScreen(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
