package com.zaheer.photocompressor.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

/**
 * Utilities for saving images to MediaStore (gallery)
 */
object MediaStoreUtils {
    
    /**
     * Save compressed image to gallery
     * @return Uri of saved image, or null if failed
     */
    fun saveToGallery(context: Context, imageBytes: ByteArray, fileName: String): android.net.Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhotoCompressor")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val imageUri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return null

            var outputStream: OutputStream? = null
            try {
                outputStream = resolver.openOutputStream(imageUri)
                outputStream?.write(imageBytes)
                outputStream?.flush()
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                
                imageUri
            } catch (e: Exception) {
                e.printStackTrace()
                resolver.delete(imageUri, null, null)
                null
            } finally {
                outputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate a unique filename for compressed image
     */
    fun generateFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "compressed_$timestamp.jpg"
    }
}
