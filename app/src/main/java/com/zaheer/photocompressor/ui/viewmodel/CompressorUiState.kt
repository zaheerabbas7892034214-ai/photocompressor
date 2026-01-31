package com.zaheer.photocompressor.ui.viewmodel

import android.net.Uri

/**
 * UI State for the compressor app
 */
sealed class CompressorUiState {
    object Idle : CompressorUiState()
    object ImagePicking : CompressorUiState()
    data class ImageSelected(
        val uri: Uri,
        val originalSizeKb: Int
    ) : CompressorUiState()
    
    data class Compressing(
        val progress: Int = 0
    ) : CompressorUiState()
    
    data class CompressionComplete(
        val uri: Uri,
        val originalSizeKb: Int,
        val compressedSizeKb: Int,
        val compressedData: ByteArray,
        val quality: Int,
        val scale: Float,
        val isApproximate: Boolean
    ) : CompressorUiState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as CompressionComplete
            if (uri != other.uri) return false
            if (originalSizeKb != other.originalSizeKb) return false
            if (compressedSizeKb != other.compressedSizeKb) return false
            if (!compressedData.contentEquals(other.compressedData)) return false
            if (quality != other.quality) return false
            if (scale != other.scale) return false
            if (isApproximate != other.isApproximate) return false
            return true
        }

        override fun hashCode(): Int {
            var result = uri.hashCode()
            result = 31 * result + originalSizeKb
            result = 31 * result + compressedSizeKb
            result = 31 * result + compressedData.contentHashCode()
            result = 31 * result + quality
            result = 31 * result + scale.hashCode()
            result = 31 * result + isApproximate.hashCode()
            return result
        }
    }
    
    data class Error(val message: String) : CompressorUiState()
    
    data class SavingToGallery(val progress: Int = 0) : CompressorUiState()
    data class SavedToGallery(val uri: Uri) : CompressorUiState()
    
    data class Sharing(val progress: Int = 0) : CompressorUiState()
    object ShareComplete : CompressorUiState()
}
