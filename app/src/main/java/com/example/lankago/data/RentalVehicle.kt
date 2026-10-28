package com.example.lankago.data


 // Data model for vehicle and bike rental items in Sri Lanka.

data class RentalVehicle(
    val id: String = "",
    val name: String = "",
    val type: String = "", // Scooter, Tuk-Tuk, SUV / 4x4, Adventure Bike
    val location: String = "",
    val transmission: String = "Automatic", // Automatic, Manual
    val seatingCapacity: Int = 2,
    val fuelType: String = "Petrol", // Petrol, Diesel, Electric, Hybrid
    val dailyRateLkr: Double = 0.0,
    val depositLkr: Double = 0.0,
    val rating: Float = 5.0f,
    val reviewCount: Int = 0,
    val isAvailable: Boolean = true,
    val vendorName: String = "",
    val vendorPhone: String = "",
    val vendorWhatsapp: String = "",
    val images: List<String> = emptyList(),
    val reviews: List<Review> = emptyList()
)
