package com.example.lankago.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Diagnostic utility to verify that Firebase Authentication, Realtime Database,
 * and Firebase Storage are fully operational and connected in LankaGo.
 */
object FirebaseHealthCheck {

    private const val TAG = "FirebaseHealthCheck"

    /**
     * Executes asynchronous health checks across Firebase Auth, Realtime Database, and Storage.
     * Can be invoked directly from any Fragment, Activity, or View callback.
     *
     * @param context Application or Activity context used to display user-facing Toast messages.
     * @param onResult Optional callback delivering (isSuccess, diagnosticMessage).
     */
    fun runFullDatabaseDiagnostics(
        context: Context,
        onResult: ((isSuccess: Boolean, message: String) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "==================================================")
                Log.i(TAG, "  STARTING FIREBASE COMPREHENSIVE HEALTH CHECK    ")
                Log.i(TAG, "==================================================")

                // -----------------------------------------------------------------
                // STEP 1: AUTHENTICATION CHECK
                // -----------------------------------------------------------------
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                val authSummary = if (currentUser != null) {
                    "ACTIVE SESSION | UID: ${currentUser.uid} | Email: ${currentUser.email ?: "None"} | Anonymous: ${currentUser.isAnonymous}"
                } else {
                    "NO ACTIVE USER (Guest Mode / Logged Out). Testing public/rule-permitted nodes."
                }
                Log.i(TAG, "[1/3 AUTH CHECK]: SUCCESS -> $authSummary")

                // -----------------------------------------------------------------
                // STEP 2: REALTIME DATABASE WRITE & READ CHECK
                // -----------------------------------------------------------------
                val rtdbRef = FirebaseDatabase.getInstance().reference
                val testNodeRef = rtdbRef.child("system_tests").child("last_check")

                val currentTime = System.currentTimeMillis()
                val testPayload = mapOf(
                    "status" to "HEALTH_CHECK_OK",
                    "timestamp" to currentTime,
                    "device_time_iso" to java.util.Date(currentTime).toString(),
                    "tester_uid" to (currentUser?.uid ?: "unauthenticated_guest"),
                    "app_id" to context.packageName
                )

                // 2a. Write test node
                testNodeRef.setValue(testPayload).await()
                Log.i(TAG, "[2/3 RTDB WRITE]: SUCCESS -> Successfully wrote payload to /system_tests/last_check")

                // 2b. Read back test node
                val snapshot = testNodeRef.get().await()
                if (snapshot.exists()) {
                    val status = snapshot.child("status").getValue(String::class.java)
                    val readTime = snapshot.child("timestamp").getValue(Long::class.java)
                    Log.i(TAG, "[2/3 RTDB READ]: SUCCESS -> Verified read back (Status: $status, Timestamp: $readTime)")
                    Log.i(TAG, "Database R/W: SUCCESS")
                } else {
                    throw IllegalStateException("RTDB snapshot at /system_tests/last_check was empty after successful write operation.")
                }

                // -----------------------------------------------------------------
                // STEP 3: FIREBASE STORAGE WRITE & DOWNLOAD URL CHECK
                // -----------------------------------------------------------------
                val storageRef = FirebaseStorage.getInstance().reference
                val testFileRef = storageRef.child("tests").child("check.txt")

                // Upload a tiny text byte array
                val testContent = "LankaGo Firebase Storage Diagnostic OK - Timestamp: $currentTime"
                val testBytes = testContent.toByteArray(Charsets.UTF_8)

                testFileRef.putBytes(testBytes).await()
                val downloadUrl = testFileRef.downloadUrl.await().toString()
                Log.i(TAG, "[3/3 STORAGE]: SUCCESS -> Uploaded check.txt to Firebase Storage. URL: $downloadUrl")
                Log.i(TAG, "Storage: SUCCESS")

                // -----------------------------------------------------------------
                // ALL TESTS PASSED
                // -----------------------------------------------------------------
                Log.i(TAG, "==================================================")
                Log.i(TAG, "  FIREBASE HEALTH CHECK COMPLETED: ALL SYSTEMS GO ")
                Log.i(TAG, "==================================================")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context.applicationContext,
                        "Firebase All Systems Go!",
                        Toast.LENGTH_LONG
                    ).show()
                    onResult?.invoke(true, "Firebase All Systems Go!")
                }

            } catch (e: Exception) {
                val errorDetails = e.localizedMessage ?: e.message ?: "Unknown Firebase exception occurred"
                Log.e(TAG, "==================================================")
                Log.e(TAG, "  FIREBASE HEALTH CHECK FAILED: $errorDetails", e)
                Log.e(TAG, "==================================================")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context.applicationContext,
                        "Firebase Error: $errorDetails",
                        Toast.LENGTH_LONG
                    ).show()
                    onResult?.invoke(false, errorDetails)
                }
            }
        }
    }
}
