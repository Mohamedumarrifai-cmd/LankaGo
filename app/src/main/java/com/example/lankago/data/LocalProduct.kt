package com.example.lankago.data

/**
 * Domain model representing a Sri Lankan local artisan product, spice, tea, or handicraft.
 */
data class LocalProduct(
    val id: String,
    val name: String,
    val category: String, // "Ceylon Tea", "Spices", "Handicrafts", "Gems & Jewelry", "Batik"
    val description: String,
    val priceLkr: Double,
    val originRegion: String = "Sri Lanka",
    val inStock: Boolean = true,
    val stockQuantity: Int = 50,
    val imageUrl: String = "",
    val imageRes: Int = 0,
    val rating: Float = 4.8f,
    val reviewCount: Int = 34
)
