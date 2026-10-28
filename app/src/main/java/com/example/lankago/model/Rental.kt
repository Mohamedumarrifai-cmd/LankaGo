package com.example.lankago.model

data class Rental(
    val id: String = "",
    val shopName: String = "",
    val category: String = "",
    val location: String = "",
    val contactNumber: String = "",
    val pricePerDay: Double = 0.0,
    val imageUrl: String = "",
    val description: String = ""
)
