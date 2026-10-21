package com.example.lankago.data

/**
 * Type of reservation made by the traveler.
 */
enum class BookingType {
    RENTAL,
    ACTIVITY
}

/**
 * Lifecycle state of a booking.
 */
enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

/**
 * Domain model representing a customer reservation for rentals or activities.
 */
data class Booking(
    val id: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val itemName: String = "",
    val itemCategory: String = "",
    val bookingType: BookingType = BookingType.RENTAL,
    val bookingDate: String = "",
    val bookingTime: String = "",
    val totalPriceLkr: Double = 0.0,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val specialNotes: String = ""
)
