package com.example.lankago.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
 * Singleton repository holding Place data locally.
 * Uses SharedPreferences and Gson to ensure newly published places persist across app restarts.
 */
object LankaGoDataManager {

    private const val PREFS_NAME = "lankago_places_pref"
    private const val KEY_PLACES_JSON = "saved_places_json"
    private val gson = Gson()

    // In-memory cache for fast access
    val placesList = ArrayList<Place>()

    init {
        // Fallback default list
        placesList.addAll(getDefaultPlaces())
    }

    /**
     * Retrieves all destinations.
     * Loads from SharedPreferences if available, otherwise initializes with default seed data.
     */
    fun getPlaces(context: Context): List<Place> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PLACES_JSON, null)

        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<ArrayList<Place>>() {}.type
            val savedList: ArrayList<Place>? = gson.fromJson(json, type)
            if (!savedList.isNullOrEmpty()) {
                placesList.clear()
                placesList.addAll(savedList)
                return savedList
            }
        }

        // First launch: initialize with defaults and save
        val defaults = getDefaultPlaces()
        savePlacesToPrefs(context, defaults)
        placesList.clear()
        placesList.addAll(defaults)
        return defaults
    }

    /**
     * Adds a new destination (e.g. from Admin panel) and persists it permanently to SharedPreferences.
     */
    fun addPlace(context: Context, place: Place) {
        val currentList = ArrayList(getPlaces(context))
        // Add new place at the top of the list
        currentList.add(0, place)
        savePlacesToPrefs(context, currentList)

        // Update in-memory cache
        placesList.clear()
        placesList.addAll(currentList)
    }

    /**
     * Finds a place by its unique ID.
     */
    fun getPlaceById(context: Context, id: String): Place? {
        val all = getPlaces(context)
        return all.find { it.id == id }
    }

    private fun savePlacesToPrefs(context: Context, list: List<Place>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_PLACES_JSON, json).apply()
    }

    private fun getDefaultPlaces(): List<Place> {
        return listOf(
            Place(
                id = "place_1",
                name = "Sigiriya Rock Fortress",
                category = "Heritage",
                location = "Matale, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/sigiriya",
                description = "Ancient 5th century rock citadel with world-renowned frescoes, water gardens, and lion paw entrance.",
                rating = "4.9",
                entranceFee = "LKR 1,500"
            ),
            Place(
                id = "place_2",
                name = "Nine Arches Bridge",
                category = "Heritage",
                location = "Ella, Badulla",
                imageUri = "android.resource://com.example.lankago/drawable/bridge",
                description = "Iconic colonial-era stone rail viaduct spanning lush green tea mountain valleys.",
                rating = "4.8",
                entranceFee = "Free"
            ),
            Place(
                id = "place_3",
                name = "Galle Dutch Fort",
                category = "Heritage",
                location = "Galle, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/hills",
                description = "17th-century coastal fortress built by the Portuguese and extensively fortified by the Dutch.",
                rating = "4.7",
                entranceFee = "Free"
            ),
            Place(
                id = "place_4",
                name = "Mirissa Beach & Coconut Tree Hill",
                category = "Beach",
                location = "Mirissa, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/lake",
                description = "Crescent beach famous for surfing, whale watching, and picturesque palm-tree cliffs over the ocean.",
                rating = "4.8",
                entranceFee = "Free"
            ),
            Place(
                id = "place_5",
                name = "Bentota Golden Beach",
                category = "Beach",
                location = "Bentota, Galle District",
                imageUri = "android.resource://com.example.lankago/drawable/hills",
                description = "Premier tropical coastal destination with water sports, coral reefs, and tranquil lagoon cruises.",
                rating = "4.6",
                entranceFee = "Free"
            ),
            Place(
                id = "place_6",
                name = "Yala National Park Safari",
                category = "Wildlife",
                location = "Hambantota, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/elephent",
                description = "Renowned sanctuary hosting one of the world's highest leopard densities, wild elephants, and rich birdlife.",
                rating = "4.9",
                entranceFee = "LKR 3,000"
            ),
            Place(
                id = "place_7",
                name = "Udawalawe Elephant Sanctuary",
                category = "Wildlife",
                location = "Udawalawe, Sabaragamuwa",
                imageUri = "android.resource://com.example.lankago/drawable/elephent",
                description = "Sprawling national park centered around a massive reservoir, famous for guaranteed wild elephant sightings.",
                rating = "4.7",
                entranceFee = "LKR 2,500"
            ),
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
