package com.example.lankago.data

/**
 * Data model for bookable activities, excursions, and guided tours in Sri Lanka.
 */
data class ActivityItem(
    val id: String,
    val title: String,
    val category: String, // Adventure, Heritage, Wildlife Safari, Cultural
    val location: String,
    val address: String,
    val durationHours: Double,
    val basePriceLkr: Double,
    val serviceFeeLkr: Double,
    val rating: Float,
    val reviewCount: Int,
    val isAvailable: Boolean,
    val imageUrl: String,
    val description: String,
    val includedItems: List<String>,
    val reviews: List<Review>
)
