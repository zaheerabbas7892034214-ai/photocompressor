package com.zaheer.photocompressor.data

import android.content.Context
import android.content.SharedPreferences

/**
 * PreferencesRepository - Manages SharedPreferences for app settings
 */
class PreferencesRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "photo_compressor_prefs"
        private const val KEY_IS_PRO = "is_pro"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isProUser(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PRO, false)
    }

    fun setProStatus(isPro: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PRO, isPro).apply()
    }
}
