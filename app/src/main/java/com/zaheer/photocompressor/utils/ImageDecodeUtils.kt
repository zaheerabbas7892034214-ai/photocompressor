package com.zaheer.photocompressor.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Utilities for safe image decoding
 */
object ImageDecodeUtils {
    
    /**
     * Calculate the size of image in KB from Uri
     */
    fun calculateImageSizeKb(context: android.content.Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            (bytes?.size ?: 0) / 1024
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Read bytes from Uri
     */
    fun readBytesFromUri(context: android.content.Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decode bitmap from bytes with safe downsampling
     */
    fun decodeBitmapSafe(bytes: ByteArray, maxWidth: Int = 2048, maxHeight: Int = 2048): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert bitmap to ImageBitmap for Compose
     */
    fun bitmapToImageBitmap(bitmap: Bitmap): ImageBitmap {
        return bitmap.asImageBitmap()
    }

    /**
     * Calculate appropriate sample size for downsampling
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
}
