package com.example.lankago.data

import java.io.Serializable

/**
 * Domain model representing a Shopping Mall in Sri Lanka.
 * Supports Firebase Realtime Database serialization and Serializable for navigation/intent transfer.
 */
data class Mall(
    val id: String = "",
    val name: String = "",
    val city: String = "Colombo",
    val address: String = "",
    val description: String = "",
    val operatingHours: String = "10:00 AM - 10:00 PM",
    val imageUrl: String = "",
    val imageRes: Int = 0,
    val rating: Double = 4.8,
    val storeCount: Int = 120,
    val contactNumber: String = "+94 11 200 0000",
    val category: String = "Shopping & Leisure",
    val features: List<String> = listOf("Cinema", "Food Court", "Supermarket", "Parking", "Shopping")
) : Serializable
