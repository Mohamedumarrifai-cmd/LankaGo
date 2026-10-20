package com.example.lankago.data

/**
 * Data model representing a verified rental agency or vehicle vendor in Sri Lanka.
 */
data class RentalProvider(
    val id: String,
    val name: String,
    val phone: String,
    val whatsapp: String,
    val websiteUrl: String,
    val location: String,
    val rating: Float,
    val reviewCount: Int,
    val isVerified: Boolean = true
)

/**
 * Data model for a vehicle or bike rental listing.
 */
data class Vehicle(
    val id: String,
    val name: String,
    val type: String, // Bike / Scooter, Tuk-Tuk, Car / Sedan, SUV / 4x4
    val imageUrl: String,
    val images: List<String>,
    val pricePerDayLkr: Double,
    val depositLkr: Double,
    val rating: Float,
    val reviewCount: Int,
    val isAvailable: Boolean,
    val seats: Int,
    val transmission: String, // Automatic, Manual
    val fuelType: String, // Petrol, Diesel, Hybrid
    val provider: RentalProvider,
    val reviews: List<Review>
)
