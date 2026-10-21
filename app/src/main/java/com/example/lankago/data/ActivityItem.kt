package com.example.lankago.data

/**
 * Data model for bookable activities, excursions, and guided tours in Sri Lanka.
 */
data class ActivityItem(
    val id: String,
    val title: String,
    val category: String, // Adventure, Heritage, Wildlife Safari, Cultural
    val location: String = "Sri Lanka",
    val address: String = "Sri Lanka",
    val durationHours: Double = 3.0,
    val basePriceLkr: Double = 4500.0,
    val serviceFeeLkr: Double = 500.0,
    val rating: Float = 4.8f,
    val reviewCount: Int = 12,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val description: String = "",
    val includedItems: List<String> = emptyList(),
    val reviews: List<Review> = emptyList()
)
