package com.example.lankago.data

/**
 * Domain model representing an authenticated user stored in Firebase Realtime Database
 * under the `/users/{uid}` node.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user", // "user" or "admin"
    val contactNumber: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

