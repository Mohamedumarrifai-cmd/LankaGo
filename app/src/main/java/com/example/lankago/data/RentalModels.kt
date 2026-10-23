package com.example.lankago.data

/**
 * Data model representing a verified rental agency or vehicle vendor in Sri Lanka.
 */
data class RentalProvider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val websiteUrl: String = "",
    val location: String = "",
    val rating: Float = 5.0f,
    val reviewCount: Int = 0,
    val isVerified: Boolean = true
)

/**
 * Data model for a vehicle or bike rental listing.
 */
data class Vehicle(
    val id: String = "",
    val name: String = "",
    val type: String = "Bike / Scooter", // Bike / Scooter, Adventure Bike, Tuk-Tuk, Car / Sedan, SUV / 4x4, Van
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val pricePerDayLkr: Double = 0.0,
    val depositLkr: Double = 0.0,
    val rating: Float = 5.0f,
    val reviewCount: Int = 0,
    val isAvailable: Boolean = true,
    val seats: Int = 2,
    val transmission: String = "Automatic", // Automatic, Manual
    val fuelType: String = "Petrol", // Petrol, Diesel, Hybrid
    val provider: RentalProvider = RentalProvider(),
    val reviews: List<Review> = emptyList()
)
