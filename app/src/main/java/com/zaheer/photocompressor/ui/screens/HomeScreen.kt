package com.zaheer.photocompressor.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zaheer.photocompressor.billing.BillingManager
import com.zaheer.photocompressor.billing.BillingState
import com.zaheer.photocompressor.ui.viewmodel.CompressorUiState
import com.zaheer.photocompressor.ui.viewmodel.CompressorViewModel
import com.zaheer.photocompressor.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CompressorViewModel,
    uiState: CompressorUiState,
    billingManager: BillingManager,
    isProActive: Boolean,
    billingState: BillingState,
    onNavigateToResult: () -> Unit,
    onUnlockPro: () -> Unit
) {
    var targetKb by remember { mutableStateOf("") }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Compressor – KB Size") },
                actions = {
                    if (isProActive) {
                        Text(
                            text = "Pro Active ✅",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Billing Section
            if (!isProActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Unlock Pro Features",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Get unlimited compressions and priority support",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Image Selection
            when (uiState) {
                is CompressorUiState.Idle -> {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pick Image")
                    }
                }
                
                is CompressorUiState.ImageSelected -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = uiState.uri,
                                contentDescription = "Selected image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            
                            Text(
                                text = "Original Size: ${FormatUtils.formatFileSize(uiState.originalSizeKb)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            OutlinedTextField(
                                value = targetKb,
                                onValueChange = { targetKb = it },
                                label = { Text("Target Size (KB)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Button(
                                onClick = {
                                    val target = targetKb.toIntOrNull()
                                    if (target != null && target > 0) {
                                        viewModel.compressImage(uiState.uri, target)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = targetKb.toIntOrNull() != null && targetKb.toIntOrNull()!! > 0
                            ) {
                                Text("Compress")
                            }
                            
                            OutlinedButton(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pick Different Image")
                            }
                        }
                    }
                }
                
                is CompressorUiState.Compressing -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Compressing image...")
                            if (uiState.progress > 0) {
                                LinearProgressIndicator(
                                    progress = uiState.progress / 100f,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                is CompressorUiState.CompressionComplete -> {
                    // Navigate to result screen
                    LaunchedEffect(Unit) {
                        onNavigateToResult()
                    }
                }
                
                is CompressorUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
                
                else -> {}
            }

            // Billing Buttons
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isProActive) {
                Button(
                    onClick = onUnlockPro,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock Pro")
                }
            }
            
            OutlinedButton(
                onClick = { billingManager.restorePurchases() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Purchases")
            }
            
            // Billing State Messages
            when (billingState) {
                is BillingState.Connecting -> {
                    Text("Connecting to billing...")
                }
                is BillingState.Error -> {
                    Text(
                        text = billingState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is BillingState.PurchaseSuccess -> {
                    Text(
                        text = "Purchase successful! Pro activated ✅",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is BillingState.RestoreSuccess -> {
                    Text(
                        text = "Purchases restored successfully ✅",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> {}
            }
        }
    }
}
