package com.example.lankago.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.lankago.MainActivity
import com.example.lankago.R
import com.example.lankago.data.AppNotification
import com.example.lankago.data.NotificationType
import com.example.lankago.data.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Universal Notification Dispatcher for LankaGo.
 * Responsibilities:
 * 1. Creates and registers high-priority Android Notification Channels.
 * 2. Emits immediate Heads-up Status Bar notifications with sound and vibration upon trip creation / booking.
 * 3. Persists in-app notification records to Firebase RTDB (/users/{uid}/notifications) and local SharedPreferences.
 */
object LankaGoNotificationHelper {

    private const val TAG = "LankaGoNotification"
    private const val CHANNEL_TRIPS_ID = "lankago_trips_channel"
    private const val CHANNEL_TRIPS_NAME = "LankaGo Trips & Bookings"
    private const val CHANNEL_TRIPS_DESC = "Realtime notifications for trip creation, confirmations, and bookings."

    private const val PREFS_NOTIFICATIONS = "lankago_in_app_notifications"
    private const val KEY_SAVED_NOTIFICATIONS = "saved_notifications_list"
    private val gson = Gson()

    /**
     * Ensures the Android Notification Channel exists (API 26+).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val channel = NotificationChannel(
                CHANNEL_TRIPS_ID,
                CHANNEL_TRIPS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_TRIPS_DESC
                enableLights(true)
                lightColor = Color.parseColor("#1A56DB")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Emits an immediate heads-up status bar notification on the user's phone when a trip is created.
     */
    fun postTripCreatedNotification(context: Context, trip: Trip) {
        try {
            createNotificationChannels(context)

            // 1. Android 13+ (API 33) Runtime Permission Check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Saving in-app notification only.")
                }
            }

            val destinationName = if (trip.destination.isNotBlank()) trip.destination else trip.tripName
            val title = "🌴 Trip Confirmed: $destinationName"
            val body = "Your itinerary for ${trip.date} is saved! Tap to view your schedule and travel details."

            // 2. Intent to open LankaGo and navigate to Trip History
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("open_destination", "tripHistoryFragment")
                putExtra("tripId", trip.id)
            }

            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                trip.id.hashCode(),
                intent,
                pendingFlags
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // 3. Build Rich Heads-Up Notification
            val notification = NotificationCompat.Builder(context, CHANNEL_TRIPS_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Your journey to $destinationName (${trip.date}) has been successfully created and linked to your Trip Planner. View destinations, rentals, and directions in the LankaGo App.")
                )
                .setColor(Color.parseColor("#1A56DB"))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(pendingIntent)
                .build()

            // 4. Dispatch to System Status Bar
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            try {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
                Log.i(TAG, "✓ Status bar notification successfully posted for trip: ${trip.id}")
            } catch (secEx: SecurityException) {
                Log.w(TAG, "System notification blocked by security permissions: ${secEx.message}")
            }

            // 5. Save in-app notification
            val inAppNotification = AppNotification(
                id = "notif_${System.currentTimeMillis()}",
                title = "Trip Confirmed: $destinationName",
                message = "Your itinerary to $destinationName for ${trip.date} has been confirmed.",
                timeAgo = "Just now",
                type = NotificationType.TRIP_UPDATE,
                targetId = trip.id,
                iconRes = R.drawable.ic_home_trip_plan
            )
            saveInAppNotification(context, inAppNotification)

        } catch (e: Exception) {
            Log.e(TAG, "Error posting trip created notification", e)
        }
    }

    /**
     * Saves an in-app notification locally in SharedPreferences and syncs to Firebase Realtime Database.
     */
    fun saveInAppNotification(context: Context, notification: AppNotification) {
        // 1. Local Persistence
        try {
            val prefs = context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
            val currentList = getAllInAppNotifications(context).toMutableList()
            currentList.add(0, notification)
            prefs.edit().putString(KEY_SAVED_NOTIFICATIONS, gson.toJson(currentList)).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Local in-app notification store error: ${e.message}")
        }

        // 2. Firebase Cloud Sync
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_traveler"
                val rtdb = FirebaseDatabase.getInstance().reference
                rtdb.child("users")
                    .child(uid)
                    .child("notifications")
                    .child(notification.id)
                    .setValue(notification)
                    .await()
                Log.d(TAG, "Synced in-app notification to Firebase /users/$uid/notifications")
            } catch (firebaseEx: Exception) {
                Log.w(TAG, "Firebase notification sync warning: ${firebaseEx.message}")
            }
        }
    }

    /**
     * Retrieves all saved in-app notifications.
     */
    fun getAllInAppNotifications(context: Context): List<AppNotification> {
        val prefs = context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SAVED_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AppNotification>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clears all saved in-app notifications locally and in Firebase RTDB.
     */
    fun clearAllInAppNotifications(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_SAVED_NOTIFICATIONS).apply()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(uid)
                    .child("notifications")
                    .removeValue()
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Clear notifications cloud sync warning: ${e.message}")
            }
        }
    }

    /**
     * Emits a payment success notification to the phone's notification bar and logs an in-app notification.
     */
    fun postPaymentSuccessNotification(
        context: Context,
        bookingRef: String,
        vehicleName: String,
        amountLkr: Double
    ) {
        try {
            createNotificationChannels(context)

            val title = "💳 Payment Confirmed: Rs. ${String.format(Locale.US, "%,.0f", amountLkr)}"
            val body = "Your payment for $vehicleName has been processed. Booking Ref: $bookingRef."

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("open_destination", "tripHistoryFragment")
            }

            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                bookingRef.hashCode(),
                intent,
                pendingFlags
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notification = NotificationCompat.Builder(context, CHANNEL_TRIPS_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Payment Success! Rs. ${String.format(Locale.US, "%,.0f", amountLkr)} received for $vehicleName. Booking Reference: $bookingRef. Your vehicle reservation is confirmed and synced to your Trip History.")
                )
                .setColor(Color.parseColor("#10B981"))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(pendingIntent)
                .build()

            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            try {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            } catch (secEx: SecurityException) {
                Log.w(TAG, "Notification permission missing: ${secEx.message}")
            }

            val inAppNotification = AppNotification(
                id = "notif_pay_${System.currentTimeMillis()}",
                title = "Payment Success: $vehicleName",
                message = "Rs. ${String.format(Locale.US, "%,.0f", amountLkr)} paid. Ref: $bookingRef",
                timeAgo = "Just now",
                type = NotificationType.BOOKING_CONFIRMATION,
                targetId = bookingRef,
                iconRes = R.drawable.ic_bell
            )
            saveInAppNotification(context, inAppNotification)
        } catch (e: Exception) {
            Log.e(TAG, "Error posting payment notification", e)
        }
    }
}
