package com.zaheer.photocompressor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zaheer.photocompressor.ui.viewmodel.CompressorUiState
import com.zaheer.photocompressor.ui.viewmodel.CompressorViewModel
import com.zaheer.photocompressor.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: CompressorViewModel,
    uiState: CompressorUiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compression Result") }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is CompressorUiState.CompressionComplete -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Result Image
                    Card(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = uiState.uri,
                            contentDescription = "Compressed image preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }

                    // Statistics Card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Compression Statistics",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Divider()
                            
                            StatRow("Original Size:", FormatUtils.formatFileSize(uiState.originalSizeKb))
                            StatRow("Compressed Size:", FormatUtils.formatFileSize(uiState.compressedSizeKb))
                            StatRow("Compression Ratio:", FormatUtils.formatCompressionRatio(uiState.originalSizeKb, uiState.compressedSizeKb))
                            StatRow("Size Reduction:", FormatUtils.formatSizeReduction(uiState.originalSizeKb, uiState.compressedSizeKb))
                            StatRow("Quality Used:", FormatUtils.formatQuality(uiState.quality))
                            StatRow("Scale Factor:", FormatUtils.formatScale(uiState.scale))
                            
                            if (uiState.isApproximate) {
                                Divider()
                                Text(
                                    text = "⚠️ Approximate result - exact target size not achievable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Button(
                        onClick = { viewModel.saveToGallery(uiState.compressedData) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save to Gallery")
                    }

                    Button(
                        onClick = { viewModel.shareImage(uiState.compressedData) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share Image")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.backToImageSelected(uiState.uri, uiState.originalSizeKb)
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Compress Again")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.reset()
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pick New Image")
                    }
                }
            }
            
            is CompressorUiState.SavingToGallery -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Saving to gallery...")
                    }
                }
            }
            
            is CompressorUiState.SavedToGallery -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "✅ Saved to Gallery",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Button(onClick = onNavigateBack) {
                                Text("Back")
                            }
                        }
                    }
                }
            }
            
            is CompressorUiState.Sharing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Preparing to share...")
                    }
                }
            }
            
            is CompressorUiState.ShareComplete -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "✅ Shared Successfully",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Button(onClick = onNavigateBack) {
                                Text("Back")
                            }
                        }
                    }
                }
            }
            
            is CompressorUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = onNavigateBack) {
                                Text("Back")
                            }
                        }
                    }
                }
            }
            
            else -> {
                // Fallback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = onNavigateBack) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
