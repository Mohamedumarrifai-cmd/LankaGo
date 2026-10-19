package com.example.lankago.utils

/**
 * Data class representing a travel destination / place in LankaGo.
 */
data class Place(
    val id: String,
    val name: String,
    val category: String,
    val location: String,
    val imageUri: String,
    val description: String
)

/**
 * Singleton repository holding in-memory Place data locally.
 * Manages the "Admin adds a destination -> User sees it" flow without any external APIs or Firebase.
 */
object LankaGoDataManager {

    val placesList = ArrayList<Place>()

    init {
        // Pre-populated sample places
        placesList.add(
            Place(
                id = "place_1",
                name = "Sigiriya Rock Fortress",
                category = "Heritage & History",
                location = "Matale, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/sigiriya",
                description = "Ancient 5th century rock citadel with world-renowned frescoes, water gardens, and lion paw entrance."
            )
        )
        placesList.add(
            Place(
                id = "place_2",
                name = "Nine Arches Bridge",
                category = "Hill Country & Nature",
                location = "Ella, Badulla",
                imageUri = "android.resource://com.example.lankago/drawable/bridge",
                description = "Iconic colonial-era stone rail viaduct spanning lush green tea mountain valleys."
            )
        )
        placesList.add(
            Place(
                id = "place_3",
                name = "Galle Dutch Fort",
                category = "Heritage & Coastal",
                location = "Galle, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/hills",
                description = "17th-century coastal fortress built by the Portuguese and extensively fortified by the Dutch."
            )
        )
    }
}
