package com.example.lankago.data


 // Data model representing a travel destination in LankaGo.

data class Destination(
    val id: String,
    val name: String,
    val category: String,
    val location: String,
    val entranceFee: String,
    val description: String,
    val imageUri: String?, // Content Uri string from camera or resource path
    val isApproved: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Singleton repository managing local in-memory travel data across fragments.
 * Enables the "Admin adds here, User sees there" architecture locally without Firebase.
 */
object LankaGoDataManager {

    private val destinationsList = mutableListOf<Destination>(
        Destination(
            id = "dest_sigiriya",
            name = "Sigiriya Rock Fortress",
            category = "Heritage & History",
            location = "Matale, Central Province",
            entranceFee = "LKR 1,500 ($36)",
            description = "Ancient 5th century rock citadel with world-renowned frescoes.",
            imageUri = "android.resource://com.example.lankago/drawable/heritage_sigiriya",
            isApproved = true
        ),
        Destination(
            id = "dest_bridge",
            name = "Nine Arches Bridge",
            category = "Hill Country & Nature",
            location = "Ella, Badulla",
            entranceFee = "Free Admission",
            description = "Iconic colonial-era stone rail bridge spanning lush misty tea valleys.",
            imageUri = "android.resource://com.example.lankago/drawable/hill_nine_arch",
            isApproved = true
        )
    )

    /**
     * Admin publishes a new destination here
     */
    fun addDestination(destination: Destination) {
        destinationsList.add(0, destination) // Prepend so new items appear at the top
    }

    /**
     * User app consumes approved destinations here
     */
    fun getApprovedDestinations(): List<Destination> {
        return destinationsList.filter { it.isApproved }
    }

    fun getAllDestinations(): List<Destination> {
        return destinationsList.toList()
    }
}
