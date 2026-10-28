package com.example.lankago.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.lankago.MainActivity
import com.example.lankago.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


 // Production-ready Firebase Messaging Service for LankaGo.

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "LankaGoFCM"
        const val CHANNEL_ID = "lankago_booking_channel"
        const val CHANNEL_NAME = "Booking Updates & Alerts"
        const val CHANNEL_DESC = "Notifications for vehicle rentals and cultural activity reservations"

        /**
         * Explicit helper to fetch and synchronize the current device FCM token
         * with the authenticated user's node in Firebase Realtime Database.
         */
        fun syncCurrentUserToken() {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val userTokenRef = FirebaseDatabase.getInstance()
                        .reference
                        .child("users")
                        .child(uid)
                        .child("fcmToken")

                    userTokenRef.setValue(token)
                        .addOnSuccessListener {
                            Log.d(TAG, "Device FCM Token synced successfully for user: $uid")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to sync device FCM Token", e)
                        }
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                }
            }
        }
    }


//      Called when a new token for the default Firebase project is generated.

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Device Token received: $token")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (!uid.isNullOrBlank()) {
            FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(uid)
                .child("fcmToken")
                .setValue(token)
        }
    }


//      Called when a message is received from Firebase Cloud Messaging.

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 1. Extract Title & Body from Notification or Data Payload
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body

        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: $data")
            if (title.isNullOrBlank()) {
                title = data["title"] ?: "LankaGo Alert"
            }
            if (body.isNullOrBlank()) {
                body = data["body"] ?: data["message"] ?: "You have a new update."
            }
        }

        if (!title.isNullOrBlank() && !body.isNullOrBlank()) {
            showHeadsUpNotification(title, body, data)
        }
    }

    /**
     * Displays a Heads-Up high-priority Android notification with custom sounds, lights, and vibration.
     */
    private fun showHeadsUpNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Android 8.0+ Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.parseColor("#5D5FEF")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Target Intent opening MainActivity with booking payload
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("bookingId", data["bookingId"])
            putExtra("bookingType", data["bookingType"])
            putExtra("status", data["status"])
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(ContextCompat.getColor(this, R.color.admin_accent_indigo))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
