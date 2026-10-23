package com.example.lankago

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

import com.example.lankago.utils.GlobalCrashHandler

/**
 * Custom Application class for LankaGo.
 * Applies the user's saved Dark Mode preference instantly upon process startup
 * and installs the Global Crash Handler for Play Store pre-release stability.
 */
class LankaGoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        GlobalCrashHandler.install(this)
        applySavedTheme()
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    companion object {
        const val PREFS_SETTINGS = "lankago_settings"
        const val KEY_DARK_MODE = "dark_mode_enabled"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
