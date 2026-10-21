package com.example.lankago.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Utility for dispatching high-priority FCM Push Notification payloads to specific target devices
 * (e.g. notifying a traveler when Admin updates booking status, or notifying Admin of new bookings).
 */
object FcmNotificationSender {

    private const val TAG = "FcmNotificationSender"
    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Dispatches an FCM payload targeting [recipientToken].
     */
    suspend fun sendBookingPushNotification(
        recipientToken: String,
        title: String,
        body: String,
        bookingId: String = "",
        bookingType: String = "",
        status: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        if (recipientToken.isBlank()) {
            Log.w(TAG, "Cannot send push notification: recipientToken is blank")
            return@withContext false
        }

        try {
            val root = JSONObject().apply {
                put("to", recipientToken)
                put("priority", "high")

                val notificationObj = JSONObject().apply {
                    put("title", title)
                    put("body", body)
                    put("sound", "default")
                }
                put("notification", notificationObj)

                val dataObj = JSONObject().apply {
                    put("title", title)
                    put("body", body)
                    put("bookingId", bookingId)
                    put("bookingType", bookingType)
                    put("status", status)
                }
                put("data", dataObj)
            }

            val request = Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(root.toString().toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "FCM payload dispatched. Response code: ${response.code}")
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send FCM payload", e)
            return@withContext false
        }
    }
}
