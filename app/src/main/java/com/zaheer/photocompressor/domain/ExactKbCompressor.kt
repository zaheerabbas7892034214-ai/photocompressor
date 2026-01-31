package com.zaheer.photocompressor.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Result of compression operation
 */
data class CompressionResult(
    val data: ByteArray,
    val achievedSizeKb: Int,
    val quality: Int,
    val scale: Float,
    val isApproximate: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompressionResult
        if (!data.contentEquals(other.data)) return false
        if (achievedSizeKb != other.achievedSizeKb) return false
        if (quality != other.quality) return false
        if (scale != other.scale) return false
        if (isApproximate != other.isApproximate) return false
        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + achievedSizeKb
        result = 31 * result + quality
        result = 31 * result + scale.hashCode()
        result = 31 * result + isApproximate.hashCode()
        return result
    }
}

/**
 * Compresses images to target KB size with best effort approach
 */
class ExactKbCompressor {
    companion object {
        private const val MAX_QUALITY = 92
        private const val MIN_QUALITY = 30
        private const val SCALE_FACTOR = 0.9f
        private const val MIN_SCALE = 0.1f
        private const val QUALITY_STEP = 5
    }

    /**
     * Compress image to target size in KB
     * @param inputBytes Input image bytes
     * @param targetKb Target size in KB
     * @return CompressionResult with compressed data and metadata
     */
    fun compress(inputBytes: ByteArray, targetKb: Int): CompressionResult {
        if (targetKb <= 0) {
            throw IllegalArgumentException("Target KB must be positive")
        }

        // Decode bitmap with safe downsampling
        var bitmap = safeDecodeBitmap(inputBytes) ?: throw IllegalStateException("Failed to decode bitmap")
        
        var currentScale = 1.0f
        var quality = MAX_QUALITY
        var compressedData: ByteArray
        var achievedSizeKb: Int
        var isApproximate = false

        // First, try quality reduction loop
        while (quality >= MIN_QUALITY) {
            compressedData = compressBitmap(bitmap, quality)
            achievedSizeKb = compressedData.size / 1024
            
            if (achievedSizeKb <= targetKb) {
                // Success! We hit or are below target
                return CompressionResult(
                    data = compressedData,
                    achievedSizeKb = achievedSizeKb,
                    quality = quality,
                    scale = currentScale,
                    isApproximate = false
                )
            }
            
            quality -= QUALITY_STEP
        }

        // If still too large at minimum quality, start downscaling
        quality = MIN_QUALITY
        
        while (currentScale > MIN_SCALE) {
            currentScale *= SCALE_FACTOR
            
            // Downscale the bitmap
            val scaledBitmap = scaleBitmap(bitmap, currentScale)
            if (scaledBitmap == null) {
                break
            }
            
            // Try compression at minimum quality
            compressedData = compressBitmap(scaledBitmap, quality)
            achievedSizeKb = compressedData.size / 1024
            
            // Clean up scaled bitmap if it's not the original
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            
            if (achievedSizeKb <= targetKb) {
                // We got it with downscaling
                isApproximate = false
                return CompressionResult(
                    data = compressedData,
                    achievedSizeKb = achievedSizeKb,
                    quality = quality,
                    scale = currentScale,
                    isApproximate = isApproximate
                )
            }
        }

        // If we reach here, we couldn't achieve target even at minimum scale
        // Return the best we could do
        isApproximate = true
        val finalBitmap = scaleBitmap(bitmap, max(currentScale, MIN_SCALE))
        compressedData = compressBitmap(finalBitmap ?: bitmap, quality)
        achievedSizeKb = compressedData.size / 1024
        
        if (finalBitmap != null && finalBitmap != bitmap) {
            finalBitmap.recycle()
        }
        
        return CompressionResult(
            data = compressedData,
            achievedSizeKb = achievedSizeKb,
            quality = quality,
            scale = max(currentScale, MIN_SCALE),
            isApproximate = isApproximate
        )
    }

    /**
     * Safely decode bitmap with downsampling if needed
     */
    private fun safeDecodeBitmap(inputBytes: ByteArray): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size, options)
            
            // Calculate inSampleSize if image is too large
            options.inSampleSize = calculateInSampleSize(options, 4096, 4096)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            
            BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calculate sample size for downsampling
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Compress bitmap to JPEG with specified quality
     */
    private fun compressBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Scale bitmap by factor
     */
    private fun scaleBitmap(bitmap: Bitmap, scale: Float): Bitmap? {
        return try {
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            
            if (newWidth <= 0 || newHeight <= 0) {
                return null
            }
            
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
