package com.example.lankago.data

/**
 * Data model for vehicle and bike rental items in Sri Lanka.
 */
data class RentalVehicle(
    val id: String,
    val name: String,
    val type: String, // Scooter, Tuk-Tuk, SUV / 4x4, Adventure Bike
    val location: String,
    val transmission: String, // Automatic, Manual
    val seatingCapacity: Int,
    val fuelType: String, // Petrol, Diesel, Electric
    val dailyRateLkr: Double,
    val depositLkr: Double,
    val rating: Float,
    val reviewCount: Int,
    val isAvailable: Boolean,
    val vendorName: String,
    val vendorPhone: String,
    val vendorWhatsapp: String,
    val images: List<String>,
    val reviews: List<Review>
)
