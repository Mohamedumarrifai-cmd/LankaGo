package com.example.lankago.data

/**
 * Data model for traditional cultural activities, artisan craft workshops, and masterclasses.
 */
data class TraditionalActivity(
    val id: String,
    val title: String,
    val category: String,
    val location: String,
    val address: String,
    val duration: String,
    val durationHours: Double,
    val hostName: String,
    val hostTitle: String,
    val hostAvatarRes: Int,
    val priceLkr: Double,
    val rating: Float,
    val reviewCount: Int,
    val imageRes: Int,
    val description: String,
    val highlights: List<String>,
    val timeSlots: List<String>
)
