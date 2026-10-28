package com.example.lankago.data

//Domain model representing an in-app or push notification.
data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timeAgo: String = "",
    val type: NotificationType = NotificationType.RENTAL_BOOKING,
    val targetId: String = "",
    val isRead: Boolean = false,
    val iconRes: Int = 0
)

enum class NotificationType {
    RENTAL_BOOKING,
    ACTIVITY_BOOKING,
    TRIP_UPDATE,
    FESTIVAL_ALERT,
    LOCAL_PRODUCT
}
