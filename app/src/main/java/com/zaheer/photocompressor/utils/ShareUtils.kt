package com.zaheer.photocompressor.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utilities for sharing images via FileProvider
 */
object ShareUtils {
    
    /**
     * Share image using FileProvider
     * @param context Application context
     * @param imageBytes Compressed image bytes
     * @return true if share intent was launched successfully
     */
    fun shareImage(context: Context, imageBytes: ByteArray): Boolean {
        return try {
            // Create cache directory for sharing
            val cacheDir = File(context.cacheDir, "images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // Create temporary file
            val fileName = "compressed_${System.currentTimeMillis()}.jpg"
            val imageFile = File(cacheDir, fileName)
            imageFile.writeBytes(imageBytes)

            // Get Uri using FileProvider
            val authority = "${context.packageName}.fileprovider"
            val imageUri = FileProvider.getUriForFile(context, authority, imageFile)

            // Create share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Launch share chooser
            val chooser = Intent.createChooser(shareIntent, "Share compressed image")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Clean up old cached images
     */
    fun cleanupCache(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, "images")
            if (cacheDir.exists() && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { file ->
                    // Delete files older than 1 hour
                    if (System.currentTimeMillis() - file.lastModified() > 3600000) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
