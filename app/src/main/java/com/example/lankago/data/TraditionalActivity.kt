package com.example.lankago.data


 //Data model for traditional cultural activities, artisan craft workshops, and masterclasses.
  //Every property has a default value so that Firebase Realtime Database deserialization works seamlessly.

data class TraditionalActivity(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val location: String = "",
    val address: String = "",
    val duration: String = "",
    val durationHours: Double = 0.0,
    val hostName: String = "",
    val hostTitle: String = "",
    val hostAvatarRes: Int = 0,
    val priceLkr: Double = 0.0,
    val rating: Float = 5.0f,
    val reviewCount: Int = 1,
    val imageRes: Int = 0,
    val description: String = "",
    val highlights: List<String> = emptyList(),
    val timeSlots: List<String> = emptyList(),
    val imageUrl: String = ""
)
