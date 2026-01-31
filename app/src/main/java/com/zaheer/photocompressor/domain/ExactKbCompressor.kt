package com.zaheer.photocompressor.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Compression result data class
 */
data class CompressionResult(
    val data: ByteArray,
    val sizeKb: Double,
    val quality: Int,
    val scale: Float,
    val isApproximate: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompressionResult

        if (!data.contentEquals(other.data)) return false
        if (sizeKb != other.sizeKb) return false
        if (quality != other.quality) return false
        if (scale != other.scale) return false
        if (isApproximate != other.isApproximate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + sizeKb.hashCode()
        result = 31 * result + quality
        result = 31 * result + scale.hashCode()
        result = 31 * result + isApproximate.hashCode()
        return result
    }
}

/**
 * ExactKbCompressor - Compression engine with quality reduction and dimension scaling
 */
class ExactKbCompressor {

    companion object {
        private const val INITIAL_QUALITY = 92
        private const val MIN_QUALITY = 30
        private const val SCALE_FACTOR = 0.9f
        private const val MIN_SCALE = 0.3f
        private const val KB_THRESHOLD = 2.0 // KB tolerance for "exact" match
    }

    /**
     * Compress image to target size in KB
     * 
     * @param imageData Original image data
     * @param targetKb Target size in KB
     * @return CompressionResult with compressed data and metadata
     */
    fun compressToTargetKb(imageData: ByteArray, targetKb: Int): CompressionResult {
        // Safe Bitmap decoding with downsampling strategy
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
        
        // Calculate appropriate sample size
        options.inSampleSize = calculateInSampleSize(options, targetKb)
        options.inJustDecodeBounds = false
        
        val originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
            ?: throw IllegalArgumentException("Failed to decode image")

        try {
            // Phase 1: Quality reduction loop (92 down to 30)
            var currentQuality = INITIAL_QUALITY
            var compressedData: ByteArray? = null
            var compressedSizeKb = 0.0

            while (currentQuality >= MIN_QUALITY) {
                compressedData = compressBitmap(originalBitmap, currentQuality)
                compressedSizeKb = compressedData.size / 1024.0

                if (compressedSizeKb <= targetKb + KB_THRESHOLD) {
                    // Success with quality reduction only
                    return CompressionResult(
                        data = compressedData,
                        sizeKb = compressedSizeKb,
                        quality = currentQuality,
                        scale = 1.0f,
                        isApproximate = compressedSizeKb > targetKb
                    )
                }
                currentQuality -= 5
            }

            // Phase 2: Dimension downscaling (by 0.9 factor iteratively)
            var currentScale = 1.0f
            var scaledBitmap = originalBitmap
            
            while (currentScale >= MIN_SCALE) {
                currentScale *= SCALE_FACTOR
                
                // Ensure minimum dimensions
                val newWidth = max(1, (originalBitmap.width * currentScale).toInt())
                val newHeight = max(1, (originalBitmap.height * currentScale).toInt())
                
                scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    newWidth,
                    newHeight,
                    true
                )

                // Try quality loop again with scaled bitmap
                currentQuality = INITIAL_QUALITY
                while (currentQuality >= MIN_QUALITY) {
                    compressedData = compressBitmap(scaledBitmap, currentQuality)
                    compressedSizeKb = compressedData.size / 1024.0

                    if (compressedSizeKb <= targetKb + KB_THRESHOLD) {
                        // Recycle scaled bitmap if different from original
                        if (scaledBitmap != originalBitmap) {
                            scaledBitmap.recycle()
                        }
                        
                        return CompressionResult(
                            data = compressedData,
                            sizeKb = compressedSizeKb,
                            quality = currentQuality,
                            scale = currentScale / SCALE_FACTOR,
                            isApproximate = compressedSizeKb > targetKb
                        )
                    }
                    currentQuality -= 5
                }
                
                // Recycle intermediate bitmap
                if (scaledBitmap != originalBitmap) {
                    scaledBitmap.recycle()
                }
            }

            // Best effort: return the smallest compressed version
            val finalScale = currentScale / SCALE_FACTOR
            val finalWidth = max(1, (originalBitmap.width * finalScale).toInt())
            val finalHeight = max(1, (originalBitmap.height * finalScale).toInt())
            
            scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                finalWidth,
                finalHeight,
                true
            )
            
            compressedData = compressBitmap(scaledBitmap, MIN_QUALITY)
            compressedSizeKb = compressedData!!.size / 1024.0
            
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }

            return CompressionResult(
                data = compressedData,
                sizeKb = compressedSizeKb,
                quality = MIN_QUALITY,
                scale = finalScale,
                isApproximate = true
            )
        } finally {
            originalBitmap.recycle()
        }
    }

    /**
     * Calculate appropriate sample size for initial decoding
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, targetKb: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        // Estimate if we need downsampling based on dimensions
        val estimatedSizeKb = (height * width * 4) / 1024 // Rough estimate
        
        if (estimatedSizeKb > targetKb * 10) {
            val scale = kotlin.math.sqrt((estimatedSizeKb.toDouble() / (targetKb * 10)))
            inSampleSize = scale.toInt()
            
            // Ensure power of 2
            var powerOf2 = 1
            while (powerOf2 * 2 < inSampleSize) {
                powerOf2 *= 2
            }
            inSampleSize = powerOf2
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
}
