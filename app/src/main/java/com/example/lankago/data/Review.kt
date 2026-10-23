package com.example.lankago.data

/**
 * Data model for customer ratings and community reviews.
 */
data class Review(
    val id: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val rating: Float = 5.0f,
    val date: String = "",
    val comment: String = ""
)
