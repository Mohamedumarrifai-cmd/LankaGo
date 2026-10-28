package com.example.lankago.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import com.example.lankago.MainActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

//  Pre-Release Production QA Global Crash Handler.
//  Intercepts uncaught thread exceptions and coroutine failures across the application.
//  Prevents abrupt force-closes, records diagnostic crash telemetry, and ensures Play Store readiness.

class GlobalCrashHandler private constructor(
    private val application: Application,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // 1. Generate comprehensive diagnostic crash report
            val crashReport = buildCrashReport(thread, throwable)
            Log.e(TAG, "CRITICAL: Global uncaught exception intercepted!\n$crashReport", throwable)

            // 2. Persist diagnostic crash report to local storage
            saveCrashLogToDisk(crashReport)

            // 3. Graceful recovery: Restart application from a clean slate
            restartAppCleanly()

        } catch (e: Exception) {
            Log.e(TAG, "Error handling uncaught exception in GlobalCrashHandler", e)
            defaultHandler?.uncaughtException(thread, throwable)
        } finally {
            // Terminate faulty process
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    private fun buildCrashReport(thread: Thread, throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())

        return buildString {
            append("====================================================\n")
            append("             LankaGo CRASH TELEMETRY REPORT         \n")
            append("====================================================\n")
            append("Timestamp   : $timestamp\n")
            append("Thread      : ${thread.name}\n")
            append("Exception   : ${throwable.javaClass.name}\n")
            append("Message     : ${throwable.localizedMessage ?: "No message"}\n")
            append("App Version : 1.0.0 (Release Candidate)\n")
            append("Device      : ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})\n")
            append("Android OS  : API ${Build.VERSION.SDK_INT} (Android ${Build.VERSION.RELEASE})\n")
            append("----------------------------------------------------\n")
            append("STACK TRACE:\n")
            append(stackTrace)
            append("====================================================\n")
        }
    }

    private fun saveCrashLogToDisk(report: String) {
        try {
            val logsDir = File(application.filesDir, "crash_logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            val logFile = File(logsDir, "latest_crash.txt")
            logFile.writeText(report)

            // Also save timestamp in SharedPreferences for fast startup checks
            val prefs = application.getSharedPreferences(PREFS_CRASH, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_LAST_CRASH_TIME, System.currentTimeMillis())
                .putString(KEY_LAST_CRASH_REPORT, report.take(500))
                .apply()
        } catch (ioEx: Exception) {
            Log.w(TAG, "Unable to write crash log to disk: ${ioEx.message}")
        }
    }

    private fun restartAppCleanly() {
        try {
            val intent = Intent(application, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("EXTRA_RESTARTED_FROM_CRASH", true)
            }
            application.startActivity(intent)
        } catch (restartEx: Exception) {
            Log.e(TAG, "Could not restart MainActivity cleanly", restartEx)
        }
    }

    companion object {
        private const val TAG = "GlobalCrashHandler"
        private const val PREFS_CRASH = "lankago_crash_prefs"
        private const val KEY_LAST_CRASH_TIME = "last_crash_timestamp"
        private const val KEY_LAST_CRASH_REPORT = "last_crash_report"

        /**
         * Installs the GlobalCrashHandler as the default uncaught exception handler.
         */
        fun install(application: Application) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (defaultHandler !is GlobalCrashHandler) {
                val handler = GlobalCrashHandler(application, defaultHandler)
                Thread.setDefaultUncaughtExceptionHandler(handler)
                Log.i(TAG, "✓ LankaGo GlobalCrashHandler successfully installed.")
            }
        }

        /**
         * Standard CoroutineExceptionHandler that logs and prevents unhandled Coroutine crashes.
         */
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Caught Coroutine uncaught exception: ${throwable.message}", throwable)
        }
    }
}
