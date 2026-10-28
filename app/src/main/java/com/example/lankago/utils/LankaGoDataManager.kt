package com.example.lankago.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


  //Data class representing a travel destination / place in LankaGo.

data class Place(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val location: String = "",
    val imageUri: String = "",
    val description: String = "",
    val rating: String = "4.8",
    val entranceFee: String = "Free",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

/**
 * Singleton repository holding Place data locally.
 * Synchronizes with Firebase Realtime Database node `/destinations` and caches via SharedPreferences & Gson.
 */
object LankaGoDataManager {

    private const val TAG = "PlacesSync"
    private const val PREFS_NAME = "lankago_places_pref"
    private const val KEY_PLACES_JSON = "saved_places_json"
    private const val DATA_VERSION = 3 // Updated with exact user drawable images for all places
    private const val KEY_DATA_VERSION = "saved_places_version"
    private val gson = Gson()
    private var firebaseListener: ValueEventListener? = null

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
        val currentVersion = prefs.getInt(KEY_DATA_VERSION, 0)
        val json = prefs.getString(KEY_PLACES_JSON, null)

        if (!json.isNullOrEmpty() && currentVersion >= DATA_VERSION) {
            val type = object : TypeToken<ArrayList<Place>>() {}.type
            val savedList: ArrayList<Place>? = gson.fromJson(json, type)
            if (!savedList.isNullOrEmpty() && savedList.any { it.category.equals("Lakes", ignoreCase = true) }) {
                placesList.clear()
                placesList.addAll(savedList)
                return savedList
            }
        }

        // Initialize with comprehensive defaults and save
        val defaults = getDefaultPlaces()
        savePlacesToPrefs(context, defaults)
        prefs.edit().putInt(KEY_DATA_VERSION, DATA_VERSION).apply()
        placesList.clear()
        placesList.addAll(defaults)
        return defaults
    }

    /**
     * Adds a newly submitted destination from Admin panel and persists to disk and Firebase.
     */
    fun addPlace(context: Context, place: Place) {
        val current = getPlaces(context).toMutableList()
        current.removeAll { it.id == place.id }
        current.add(0, place)
        savePlacesToPrefs(context, current)
        placesList.clear()
        placesList.addAll(current)

        try {
            val db = FirebaseDatabase.getInstance().reference.child("destinations")
            db.child(place.id).setValue(place)
                .addOnSuccessListener {
                    Log.d(TAG, "Place successfully pushed to Firebase /destinations: ${place.name}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to push place to Firebase: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase unavailable for addPlace: ${e.message}")
        }
    }

    /**
     * Updates an existing destination and persists changes to disk and Firebase.
     */
    fun updatePlace(context: Context, place: Place) {
        val current = getPlaces(context).toMutableList()
        val index = current.indexOfFirst { it.id == place.id }
        if (index != -1) {
            current[index] = place
            savePlacesToPrefs(context, current)
            placesList.clear()
            placesList.addAll(current)
        }

        try {
            val db = FirebaseDatabase.getInstance().reference.child("destinations")
            db.child(place.id).setValue(place)
                .addOnSuccessListener {
                    Log.d(TAG, "Place successfully updated in Firebase: ${place.name}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update place in Firebase: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase unavailable for updatePlace: ${e.message}")
        }
    }

    /**
     * Permanently deletes a destination by ID and persists changes to disk and Firebase.
     */
    fun deletePlace(context: Context, placeId: String) {
        val current = getPlaces(context).toMutableList()
        val removed = current.removeAll { it.id == placeId }
        if (removed) {
            savePlacesToPrefs(context, current)
            placesList.clear()
            placesList.addAll(current)
        }

        try {
            val db = FirebaseDatabase.getInstance().reference.child("destinations")
            db.child(placeId).removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "Place deleted from Firebase: $placeId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to delete place from Firebase: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase unavailable for deletePlace: ${e.message}")
        }
    }

    /**
     * Real-time bidirectional synchronization with Firebase Realtime Database `/destinations`.
     * Fetches places published by Admin, merges with local defaults, and refreshes the user UI.
     */
    fun syncWithFirebase(context: Context, onSynced: (() -> Unit)? = null) {
        try {
            val ref = FirebaseDatabase.getInstance().reference.child("destinations")
            if (firebaseListener != null) {
                ref.removeEventListener(firebaseListener!!)
            }

            firebaseListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firebasePlaces = mutableListOf<Place>()
                    for (child in snapshot.children) {
                        try {
                            val id = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                            val name = child.child("name").getValue(String::class.java) ?: ""
                            val category = child.child("category").getValue(String::class.java) ?: "Heritage"
                            val location = child.child("location").getValue(String::class.java) ?: "Sri Lanka"
                            val imageUri = child.child("imageUri").getValue(String::class.java) ?: ""
                            val description = child.child("description").getValue(String::class.java) ?: ""
                            val rating = child.child("rating").getValue(String::class.java) ?: "4.8"
                            val entranceFee = child.child("entranceFee").getValue(String::class.java) ?: "Free"
                            val lat = (child.child("lat").value as? Number)?.toDouble() ?: 0.0
                            val lng = (child.child("lng").value as? Number)?.toDouble() ?: 0.0

                            if (name.isNotBlank()) {
                                firebasePlaces.add(
                                    Place(
                                        id = id,
                                        name = name,
                                        category = category,
                                        location = location,
                                        imageUri = imageUri,
                                        description = description,
                                        rating = rating,
                                        entranceFee = entranceFee,
                                        lat = lat,
                                        lng = lng
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing place snapshot: ${e.message}")
                        }
                    }

                    Log.d(TAG, "Synced ${firebasePlaces.size} destinations from Firebase /destinations")

                    if (firebasePlaces.isNotEmpty()) {
                        val defaults = getDefaultPlaces()
                        val firebaseIds = firebasePlaces.map { it.id }.toSet()
                        val merged = mutableListOf<Place>()
                        merged.addAll(firebasePlaces)
                        for (def in defaults) {
                            if (!firebaseIds.contains(def.id)) {
                                merged.add(def)
                            }
                        }
                        savePlacesToPrefs(context, merged)
                        placesList.clear()
                        placesList.addAll(merged)
                    }
                    onSynced?.invoke()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase /destinations listener cancelled: ${error.message}")
                }
            }

            ref.addValueEventListener(firebaseListener!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Firebase sync: ${e.message}")
        }
    }

    /**
     * Searches places matching a query in title, location, or description.
     */
    fun searchPlaces(context: Context, query: String): List<Place> {
        val all = getPlaces(context)
        if (query.isBlank()) return all

        val q = query.trim().lowercase()
        return all.filter {
            it.name.lowercase().contains(q) ||
            it.location.lowercase().contains(q) ||
            it.category.lowercase().contains(q) ||
            it.description.lowercase().contains(q)
        }
    }

    /**
     * Filters places by specific category ("Lakes", "Hills", "Heritage", "Beach", "Wildlife").
     */
    fun filterByCategory(context: Context, category: String): List<Place> {
        val all = getPlaces(context)
        if (category.equals("All", ignoreCase = true) || category.isBlank()) {
            return all
        }
        return all.filter { it.category.equals(category, ignoreCase = true) }
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
            // ================= 1. LAKES OF SRI LANKA =================
            Place(
                id = "lake_gregory",
                name = "Gregory Lake",
                category = "Lakes",
                location = "Nuwara Eliya, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/gregoru_lake_nuwaraeliya",
                description = "Scenic colonial reservoir nestled among mist-covered hills in Little England. Popular for swan paddle boating, jet skiing, pony rides, and waterfront walking trails.",
                rating = "4.8",
                entranceFee = "LKR 500",
                lat = 6.9535,
                lng = 80.7816
            ),
            Place(
                id = "lake_kandy",
                name = "Kandy Lake (Kiri Muhuda)",
                category = "Lakes",
                location = "Kandy, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/lake_kandy",
                description = "Historic royal heart of Kandy built in 1807 beside Sri Dalada Maligawa. Enjoy tranquil shaded lakeside strolls, watching pelicans, cormorants, and scenic hill views.",
                rating = "4.7",
                entranceFee = "Free",
                lat = 7.2936,
                lng = 80.6413
            ),
            Place(
                id = "lake_sembuwatta",
                name = "Sembuwatta Lake",
                category = "Lakes",
                location = "Elkaduwa, Matale District",
                imageUri = "android.resource://com.example.lankago/drawable/lake_sembuwatta",
                description = "Breathtaking man-made lake fed by natural springs, framed by towering pine forests and emerald tea hills. A paradise for photography, picnics, and mountain breezes.",
                rating = "4.9",
                entranceFee = "LKR 400",
                lat = 7.4338,
                lng = 80.6974
            ),
            Place(
                id = "lake_bolgoda",
                name = "Bolgoda Lake",
                category = "Lakes",
                location = "Moratuwa & Panadura, Western Province",
                imageUri = "android.resource://com.example.lankago/drawable/lake_bolgoda",
                description = "The largest natural freshwater lake in Sri Lanka. Renowned for boat safaris, bird watching, mangrove ecosystems, and thrilling jet-ski watersports.",
                rating = "4.6",
                entranceFee = "Free",
                lat = 6.7417,
                lng = 79.9392
            ),
            Place(
                id = "lake_tissa",
                name = "Tissa Wewa Reservoir",
                category = "Lakes",
                location = "Tissamaharama, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/lake_tissa",
                description = "Magnificent 3rd-century BC ancient irrigation lake constructed by King Yatala Tissa. Shaded by ancient giant trees, with glorious sunsets and aquatic birdlife.",
                rating = "4.7",
                entranceFee = "Free",
                lat = 6.2797,
                lng = 81.2882
            ),
            Place(
                id = "lake_castlereagh",
                name = "Castlereagh Reservoir",
                category = "Lakes",
                location = "Hatton, Central Highlands",
                imageUri = "android.resource://com.example.lankago/drawable/lake_castlereagh",
                description = "Dramatic emerald mountain lake surrounded by rolling tea plantations and colonial bungalows. Famous for seaplane landings and peaceful valley scenery.",
                rating = "4.9",
                entranceFee = "Free",
                lat = 6.8667,
                lng = 80.5667
            ),

            // ================= 2. HILLS & MOUNTAINS =================
            Place(
                id = "hill_nine_arches",
                name = "Nine Arches Bridge",
                category = "Hills",
                location = "Ella, Badulla District",
                imageUri = "android.resource://com.example.lankago/drawable/hill_nine_arch",
                description = "Iconic colonial-era stone rail viaduct nestled amidst dense jungle and lush green tea mountain valleys in Ella. Watch the blue train cross into the misty hills.",
                rating = "4.9",
                entranceFee = "Free",
                lat = 6.8768,
                lng = 81.0608
            ),
            Place(
                id = "hill_little_adams",
                name = "Little Adam's Peak & Ella Rock",
                category = "Hills",
                location = "Ella, Uva Province",
                imageUri = "android.resource://com.example.lankago/drawable/hills_adams",
                description = "Invigorating mountain hike offering 360-degree panoramic vistas of Ella Gap, Ravana Falls, and rolling green tea plantations.",
                rating = "4.8",
                entranceFee = "Free",
                lat = 6.8617,
                lng = 81.0544
            ),
            Place(
                id = "hill_horton_plains",
                name = "Horton Plains & World's End",
                category = "Hills",
                location = "Nuwara Eliya, Central Highlands",
                imageUri = "android.resource://com.example.lankago/drawable/hill_hortainplains",
                description = "UNESCO World Heritage high-altitude cloud plateau at 2,100 meters. Features a dramatic sheer vertical cliff drop of 870 meters overlooking the southern plains.",
                rating = "4.9",
                entranceFee = "LKR 3,500",
                lat = 6.8028,
                lng = 80.8039
            ),
            Place(
                id = "hill_knuckles",
                name = "Knuckles Mountain Range",
                category = "Hills",
                location = "Matale & Kandy Districts",
                imageUri = "android.resource://com.example.lankago/drawable/hills_knuckles",
                description = "Rugged UNESCO biosphere reserve shaped like the knuckles of a clenched fist. Home to cloud forests, hidden waterfalls, and premier trekking expeditions.",
                rating = "4.9",
                entranceFee = "LKR 1,500",
                lat = 7.4667,
                lng = 80.7833
            ),
            Place(
                id = "hill_pidurutalagala",
                name = "Pidurutalagala (Mount Pedro)",
                category = "Hills",
                location = "Nuwara Eliya, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/hill_pidurutalagala",
                description = "Sri Lanka's highest peak rising to 2,524 meters (8,281 feet). Offers cool temperatures, pine forests, and sweeping views of the central massif.",
                rating = "4.7",
                entranceFee = "Free",
                lat = 7.0000,
                lng = 80.7667
            ),
            Place(
                id = "hill_ambuluwawa",
                name = "Ambuluwawa Biodiversity Peak",
                category = "Hills",
                location = "Gampola, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/hill_ambuluwawa",
                description = "A striking cone-shaped mountain spire crowned with a spiraling narrow white tower extending high into the clouds.",
                rating = "4.8",
                entranceFee = "LKR 1,000",
                lat = 7.1719,
                lng = 80.5489
            ),

            // ================= 3. HERITAGE & FORTS =================
            Place(
                id = "heritage_sigiriya",
                name = "Sigiriya Rock Fortress",
                category = "Heritage",
                location = "Matale, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/heritage_sigiriya",
                description = "Ancient 5th-century rock fortress and palace of King Kashyapa. World-renowned for its colorful frescoes, mirrored wall, water gardens, and lion paw entrance.",
                rating = "4.9",
                entranceFee = "LKR 1,500",
                lat = 7.9570,
                lng = 80.7603
            ),
            Place(
                id = "heritage_galle_fort",
                name = "Galle Dutch Fort",
                category = "Heritage",
                location = "Galle, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/heritage_galle_fort",
                description = "Living 17th-century coastal UNESCO fortress overlooking the Indian Ocean. Stroll along cobblestone streets, ramparts, lighthouse, and colonial mansions.",
                rating = "4.8",
                entranceFee = "Free",
                lat = 6.0268,
                lng = 80.2170
            ),
            Place(
                id = "heritage_tooth_temple",
                name = "Temple of the Sacred Tooth Relic",
                category = "Heritage",
                location = "Kandy, Central Province",
                imageUri = "android.resource://com.example.lankago/drawable/heritage_tooth_temple",
                description = "Sacred Buddhist shrine in the Royal Palace complex of Kandy housing the holy tooth relic of Gautama Buddha. A centerpiece of world heritage.",
                rating = "4.9",
                entranceFee = "LKR 1,000",
                lat = 7.2936,
                lng = 80.6413
            ),
            Place(
                id = "heritage_dambulla",
                name = "Dambulla Golden Cave Temple",
                category = "Heritage",
                location = "Dambulla, Matale District",
                imageUri = "android.resource://com.example.lankago/drawable/heritage_dambulla",
                description = "The largest and best-preserved rock cave temple monastery in Sri Lanka, boasting 153 Buddha statues and ancient murals dating to the 1st century BC.",
                rating = "4.8",
                entranceFee = "LKR 1,500",
                lat = 7.8564,
                lng = 80.6483
            ),
            Place(
                id = "heritage_polonnaruwa",
                name = "Polonnaruwa Ancient City",
                category = "Heritage",
                location = "Polonnaruwa, North Central",
                imageUri = "android.resource://com.example.lankago/drawable/heritage_polonnaruwa",
                description = "Medieval royal capital of Sri Lanka featuring magnificent stone ruins, royal palace audience halls, and the majestic Gal Vihara rock statues.",
                rating = "4.8",
                entranceFee = "LKR 2,000",
                lat = 7.9403,
                lng = 81.0188
            ),

            // ================= 4. BEACHES =================
            Place(
                id = "beach_mirissa",
                name = "Mirissa Beach & Coconut Tree Hill",
                category = "Beach",
                location = "Mirissa, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/beach_mirissa",
                description = "Golden crescent bay celebrated for surfing, blue whale watching excursions, beachfront seafood, and Instagram-famous palm promontories.",
                rating = "4.8",
                entranceFee = "Free",
                lat = 5.9483,
                lng = 80.4578
            ),
            Place(
                id = "beach_bentota",
                name = "Bentota Golden Beach",
                category = "Beach",
                location = "Bentota, Galle District",
                imageUri = "android.resource://com.example.lankago/drawable/beach_bentota",
                description = "Premier tropical coastal destination with water sports, coral reefs, and tranquil Madu river lagoon cruises.",
                rating = "4.6",
                entranceFee = "Free",
                lat = 6.4258,
                lng = 79.9961
            ),

            // ================= 5. WILDLIFE =================
            Place(
                id = "wildlife_yala",
                name = "Yala National Park Safari",
                category = "Wildlife",
                location = "Hambantota, Southern Province",
                imageUri = "android.resource://com.example.lankago/drawable/wildlife_yala",
                description = "World-famous sanctuary boasting the highest density of leopards on earth, herds of wild elephants, sloth bears, and crocodiles.",
                rating = "4.9",
                entranceFee = "LKR 3,000",
                lat = 6.3683,
                lng = 81.5208
            ),
            Place(
                id = "wildlife_udawalawe",
                name = "Udawalawe Elephant Sanctuary",
                category = "Wildlife",
                location = "Udawalawe, Sabaragamuwa",
                imageUri = "android.resource://com.example.lankago/drawable/wildlife_udawalawe",
                description = "Sprawling national park centered around a massive reservoir, famous for guaranteed wild elephant sightings and conservation orphanage.",
                rating = "4.8",
                entranceFee = "LKR 2,500",
                lat = 6.4744,
                lng = 80.8986
            )
        )
    }
}
