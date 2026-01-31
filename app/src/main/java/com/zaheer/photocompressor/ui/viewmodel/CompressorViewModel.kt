package com.zaheer.photocompressor.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.photocompressor.domain.ExactKbCompressor
import com.zaheer.photocompressor.utils.ImageDecodeUtils
import com.zaheer.photocompressor.utils.MediaStoreUtils
import com.zaheer.photocompressor.utils.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for compression operations
 */
class CompressorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val compressor = ExactKbCompressor()
    
    private val _uiState = MutableStateFlow<CompressorUiState>(CompressorUiState.Idle)
    val uiState: StateFlow<CompressorUiState> = _uiState.asStateFlow()
    
    /**
     * Handle selected image
     */
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = CompressorUiState.ImagePicking
                
                val context = getApplication<Application>().applicationContext
                val originalSizeKb = ImageDecodeUtils.calculateImageSizeKb(context, uri)
                
                if (originalSizeKb == 0) {
                    _uiState.value = CompressorUiState.Error("Failed to read image")
                    return@launch
                }
                
                _uiState.value = CompressorUiState.ImageSelected(uri, originalSizeKb)
            } catch (e: Exception) {
                _uiState.value = CompressorUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Compress image to target size
     */
    fun compressImage(uri: Uri, targetKb: Int) {
        if (targetKb <= 0) {
            _uiState.value = CompressorUiState.Error("Target size must be positive")
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = CompressorUiState.Compressing(0)
                
                val context = getApplication<Application>().applicationContext
                val originalSizeKb = ImageDecodeUtils.calculateImageSizeKb(context, uri)
                
                // Read image bytes
                val inputBytes = ImageDecodeUtils.readBytesFromUri(context, uri)
                if (inputBytes == null) {
                    _uiState.value = CompressorUiState.Error("Failed to read image")
                    return@launch
                }
                
                _uiState.value = CompressorUiState.Compressing(50)
                
                // Compress
                val result = compressor.compress(inputBytes, targetKb)
                
                _uiState.value = CompressorUiState.CompressionComplete(
                    uri = uri,
                    originalSizeKb = originalSizeKb,
                    compressedSizeKb = result.achievedSizeKb,
                    compressedData = result.data,
                    quality = result.quality,
                    scale = result.scale,
                    isApproximate = result.isApproximate
                )
            } catch (e: Exception) {
                _uiState.value = CompressorUiState.Error("Compression failed: ${e.message}")
            }
        }
    }
    
    /**
     * Save compressed image to gallery
     */
    fun saveToGallery(compressedData: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = CompressorUiState.SavingToGallery(0)
                
                val context = getApplication<Application>().applicationContext
                val fileName = MediaStoreUtils.generateFileName()
                
                _uiState.value = CompressorUiState.SavingToGallery(50)
                
                val savedUri = MediaStoreUtils.saveToGallery(context, compressedData, fileName)
                
                if (savedUri != null) {
                    _uiState.value = CompressorUiState.SavedToGallery(savedUri)
                } else {
                    _uiState.value = CompressorUiState.Error("Failed to save to gallery")
                }
            } catch (e: Exception) {
                _uiState.value = CompressorUiState.Error("Save failed: ${e.message}")
            }
        }
    }
    
    /**
     * Share compressed image
     */
    fun shareImage(compressedData: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = CompressorUiState.Sharing(0)
                
                val context = getApplication<Application>().applicationContext
                
                _uiState.value = CompressorUiState.Sharing(50)
                
                val success = ShareUtils.shareImage(context, compressedData)
                
                if (success) {
                    _uiState.value = CompressorUiState.ShareComplete
                } else {
                    _uiState.value = CompressorUiState.Error("Failed to share image")
                }
            } catch (e: Exception) {
                _uiState.value = CompressorUiState.Error("Share failed: ${e.message}")
            }
        }
    }
    
    /**
     * Reset to idle state
     */
    fun reset() {
        _uiState.value = CompressorUiState.Idle
    }
    
    /**
     * Go back from result to image selected
     */
    fun backToImageSelected(uri: Uri, originalSizeKb: Int) {
        _uiState.value = CompressorUiState.ImageSelected(uri, originalSizeKb)
    }
}
