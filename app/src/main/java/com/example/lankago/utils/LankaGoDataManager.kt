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
    val description: String,
    val rating: String = "4.8",
    val entranceFee: String = "Free"
)

/**
 * Singleton repository holding in-memory Place data locally.
 * Manages the "Admin adds a destination -> User sees it" flow without any external APIs or Firebase.
 */
object LankaGoDataManager {

    val placesList = ArrayList<Place>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        placesList.add(
            Place(
                id = "place_1",
                name = "Sigiriya Rock Fortress",
                category = "Heritage",
                location = "Matale, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/sigiriya",
                description = "Ancient 5th century rock citadel with world-renowned frescoes, water gardens, and lion paw entrance.",
                rating = "4.9",
                entranceFee = "LKR 1,500"
            )
        )
        placesList.add(
            Place(
                id = "place_2",
                name = "Nine Arches Bridge",
                category = "Heritage",
                location = "Ella, Badulla",
                imageUri = "android.resource://com.example.lankago/drawable/bridge",
                description = "Iconic colonial-era stone rail viaduct spanning lush green tea mountain valleys.",
                rating = "4.8",
                entranceFee = "Free"
            )
        )
        placesList.add(
            Place(
                id = "place_3",
                name = "Galle Dutch Fort",
                category = "Heritage",
                location = "Galle, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/hills",
                description = "17th-century coastal fortress built by the Portuguese and extensively fortified by the Dutch.",
                rating = "4.7",
                entranceFee = "Free"
            )
        )
        placesList.add(
            Place(
                id = "place_4",
                name = "Mirissa Beach & Coconut Tree Hill",
                category = "Beach",
                location = "Mirissa, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/lake",
                description = "Crescent beach famous for surfing, whale watching, and picturesque palm-tree cliffs over the ocean.",
                rating = "4.8",
                entranceFee = "Free"
            )
        )
        placesList.add(
            Place(
                id = "place_5",
                name = "Bentota Golden Beach",
                category = "Beach",
                location = "Bentota, Galle District",
                imageUri = "android.resource://com.example.lankago/drawable/hills",
                description = "Premier tropical coastal destination with water sports, coral reefs, and tranquil lagoon cruises.",
                rating = "4.6",
                entranceFee = "Free"
            )
        )
        placesList.add(
            Place(
                id = "place_6",
                name = "Yala National Park Safari",
                category = "Wildlife",
                location = "Hambantota, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/elephent",
                description = "Renowned sanctuary hosting one of the world's highest leopard densities, wild elephants, and rich birdlife.",
                rating = "4.9",
                entranceFee = "LKR 3,000"
            )
        )
        placesList.add(
            Place(
                id = "place_7",
                name = "Udawalawe Elephant Sanctuary",
                category = "Wildlife",
                location = "Udawalawe, Sabaragamuwa",
                imageUri = "android.resource://com.example.lankago/drawable/elephent",
                description = "Sprawling national park centered around a massive reservoir, famous for guaranteed wild elephant sightings.",
                rating = "4.7",
                entranceFee = "LKR 2,500"
            )
        )
        placesList.add(
            Place(
                id = "place_8",
                name = "Temple of the Sacred Tooth Relic",
                category = "Heritage",
                location = "Kandy, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/sigiriya1",
                description = "Sacred Buddhist shrine in the Royal Palace complex housing the relic of the tooth of the Buddha.",
                rating = "4.9",
                entranceFee = "LKR 1,000"
            )
        )
    }
}
