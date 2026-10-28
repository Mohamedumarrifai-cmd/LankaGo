package com.example.lankago.data

import com.example.lankago.utils.Place


 //Data model representing a user-created travel itinerary in LankaGo.
 //Includes traveler verification details and booked rentals / activities.

data class Trip(
    val id: String = "",
    val tripName: String = "",
    val date: String = "",
    val travelerType: String = "Local", // "Local" or "Foreign"
    val idImageUri: String? = null,      // Local FileProvider URI string
    val places: MutableList<Place> = mutableListOf(),
    val bookedRentals: MutableList<RentalVehicle> = mutableListOf(),
    val bookedActivities: MutableList<ActivityItem> = mutableListOf(),
    val destination: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Upcoming"
)
