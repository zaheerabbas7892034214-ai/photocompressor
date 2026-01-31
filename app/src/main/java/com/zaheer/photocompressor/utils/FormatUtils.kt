package com.zaheer.photocompressor.utils

import java.text.DecimalFormat

/**
 * Utilities for formatting display values
 */
object FormatUtils {
    
    /**
     * Format file size in KB to readable string
     */
    fun formatFileSize(sizeKb: Int): String {
        return when {
            sizeKb < 1024 -> "$sizeKb KB"
            else -> {
                val sizeMb = sizeKb / 1024.0
                "${DecimalFormat("#.##").format(sizeMb)} MB"
            }
        }
    }

    /**
     * Format quality percentage
     */
    fun formatQuality(quality: Int): String {
        return "$quality%"
    }

    /**
     * Format scale factor
     */
    fun formatScale(scale: Float): String {
        val percentage = (scale * 100).toInt()
        return "$percentage%"
    }

    /**
     * Format compression ratio
     */
    fun formatCompressionRatio(originalKb: Int, compressedKb: Int): String {
        if (originalKb == 0) return "N/A"
        
        val ratio = (compressedKb.toFloat() / originalKb.toFloat()) * 100
        return "${DecimalFormat("#.#").format(ratio)}%"
    }

    /**
     * Format size reduction
     */
    fun formatSizeReduction(originalKb: Int, compressedKb: Int): String {
        if (originalKb == 0) return "N/A"
        
        val reduction = originalKb - compressedKb
        val reductionPercent = ((reduction.toFloat() / originalKb.toFloat()) * 100).toInt()
        return "$reduction KB saved ($reductionPercent%)"
    }
}
