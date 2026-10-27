package com.example.lankago

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

/**
 * Production Application class for LankaGo.
 * Configures the Global Uncaught Exception Handler to capture fatal crashes,
 * log telemetry to Firebase Crashlytics, and present a recovery error activity.
 */
class LankaGoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupGlobalExceptionHandler()
        applySavedTheme()
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // 1. Record the exception to Firebase Crashlytics
                try {
                    FirebaseCrashlytics.getInstance().recordException(throwable)
                } catch (crashlyticsEx: Throwable) {
                    Log.e(TAG, "Failed logging to Crashlytics", crashlyticsEx)
                }

                Log.e(TAG, "FATAL: Intercepted uncaught exception on thread ${thread.name}", throwable)

                // 2. Launch CrashDisplayActivity in a new task with error message
                val intent = Intent(this, CrashDisplayActivity::class.java).apply {
                    putExtra(
                        CrashDisplayActivity.EXTRA_ERROR_MESSAGE,
                        throwable.localizedMessage ?: "Unexpected internal application error."
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

            } catch (fallbackEx: Throwable) {
                Log.e(TAG, "Error executing custom crash handler fallback", fallbackEx)
                defaultHandler?.uncaughtException(thread, throwable)
            } finally {
                // 3. Safely kill the process
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
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
        private const val TAG = "LankaGoApp"
        const val PREFS_SETTINGS = "lankago_settings"
        const val KEY_DARK_MODE = "dark_mode_enabled"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

        lateinit var instance: LankaGoApp
            private set
    }
}
