package com.zaheer.photocompressor.presentation.home

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.photocompressor.domain.CompressionResult
import com.zaheer.photocompressor.domain.ExactKbCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class HomeUiState(
    val selectedImageUri: Uri? = null,
    val originalSizeKb: Double? = null,
    val targetSizeKb: String = "",
    val compressionResult: CompressionResult? = null,
    val isCompressing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val compressor = ExactKbCompressor()
    private var compressedImageFile: File? = null

    fun onImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val imageData = inputStream?.readBytes()
                inputStream?.close()

                if (imageData != null) {
                    val sizeKb = imageData.size / 1024.0
                    _uiState.value = _uiState.value.copy(
                        selectedImageUri = uri,
                        originalSizeKb = sizeKb,
                        compressionResult = null,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to load image: ${e.message}")
            }
        }
    }

    fun onTargetSizeChanged(size: String) {
        _uiState.value = _uiState.value.copy(targetSizeKb = size)
    }

    fun compressImage(context: Context) {
        val state = _uiState.value
        val uri = state.selectedImageUri
        val targetKb = state.targetSizeKb.toIntOrNull()

        if (uri == null) {
            _uiState.value = state.copy(error = "No image selected")
            return
        }

        if (targetKb == null || targetKb <= 0) {
            _uiState.value = state.copy(error = "Invalid target size")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isCompressing = true, error = null)

            try {
                val result = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val imageData = inputStream?.readBytes()
                    inputStream?.close()

                    if (imageData == null) {
                        throw Exception("Failed to read image data")
                    }

                    compressor.compressToTargetKb(imageData, targetKb)
                }

                // Save compressed image to cache for sharing
                compressedImageFile = withContext(Dispatchers.IO) {
                    val cacheFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                    cacheFile.writeBytes(result.data)
                    cacheFile
                }

                _uiState.value = _uiState.value.copy(
                    compressionResult = result,
                    isCompressing = false,
                    successMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCompressing = false,
                    error = "Compression failed: ${e.message}"
                )
            }
        }
    }

    fun saveToGallery(context: Context) {
        val result = _uiState.value.compressionResult
        if (result == null) {
            _uiState.value = _uiState.value.copy(error = "No compressed image to save")
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "compressed_${System.currentTimeMillis()}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhotoCompressor")
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    
                    uri?.let {
                        resolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(result.data)
                        }
                    } ?: throw Exception("Failed to create media store entry")
                }

                _uiState.value = _uiState.value.copy(
                    successMessage = "Image saved successfully",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save: ${e.message}")
            }
        }
    }

    fun shareImage(context: Context): Uri? {
        return compressedImageFile?.let { file ->
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
