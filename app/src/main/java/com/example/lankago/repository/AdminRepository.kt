package com.example.lankago.repository

import android.net.Uri
import com.example.lankago.data.RentalShop
import com.example.lankago.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Production-ready Repository handling Firebase operations:
 * - Authentication & User Nodes (`/users/{uid}`)
 * - Realtime Database CRUD (`/rentals`, `/products`, `/bookings`)
 * - Firebase Storage Image Uploads
 */
class AdminRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val storage: StorageReference = FirebaseStorage.getInstance().reference
) {

    // =========================================================================
    // 1. FIREBASE STORAGE: IMAGE UPLOADS
    // =========================================================================

    /**
     * Upload an image file from the device to Firebase Storage.
     * @param imageUri Content Uri obtained from the photo picker or camera.
     * @param folder The storage bucket subdirectory (e.g. "rentals", "products").
     * @return Download URL string accessible via HTTPS.
     */
    suspend fun uploadImage(imageUri: Uri, folder: String = "rentals"): String {
        val fileName = "${folder}/${UUID.randomUUID()}.jpg"
        val fileRef = storage.child(fileName)

        // Upload file asynchronously using Kotlin Coroutines Tasks.await()
        fileRef.putFile(imageUri).await()

        // Retrieve and return the publicly accessible download URL
        return fileRef.downloadUrl.await().toString()
    }

    /**
     * Delete an existing image from Firebase Storage using its full URL.
     */
    suspend fun deleteImageByUrl(imageUrl: String) {
        if (imageUrl.isNotBlank() && imageUrl.contains("firebasestorage.googleapis.com")) {
            try {
                FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete().await()
            } catch (_: Exception) {
                // Ignore if already deleted or invalid
            }
        }
    }

    // =========================================================================
    // 2. REALTIME DATABASE: RENTAL SHOPS CRUD (/rentals)
    // =========================================================================

    /**
     * Create and push a new rental shop to the `/rentals` node.
     * 1. Uploads the image to Firebase Storage if an [imageUri] is provided.
     * 2. Generates a unique key using `push().key`.
     * 3. Sets the complete object in the database using `setValue()`.
     */
    suspend fun addRentalShop(
        name: String,
        description: String,
        operatingHours: String,
        contactNumber: String,
        whatsappNumber: String,
        priceStartingFrom: String = "LKR 3,500/day",
        city: String = "Sri Lanka",
        address: String = "Sri Lanka",
        lat: Double = 6.9271,
        lng: Double = 79.8612,
        imageUri: Uri? = null,
        existingImageUrl: String = ""
    ): RentalShop {
        // Step 1: Upload image to Firebase Storage if provided
        val finalImageUrl = if (imageUri != null) {
            uploadImage(imageUri, folder = "rentals")
        } else {
            existingImageUrl
        }

        // Step 2: Generate unique push key
        val rentalsRef = database.child("rentals")
        val shopKey = rentalsRef.push().key ?: UUID.randomUUID().toString()

        // Step 3: Instantiate domain model
        val newShop = RentalShop(
            id = shopKey,
            name = name.trim(),
            description = description.trim(),
            lat = lat,
            lng = lng,
            contactNumber = contactNumber.trim(),
            operatingHours = operatingHours.trim(),
            imageUrl = finalImageUrl.trim(),
            rating = 5.0f,
            reviewCount = 1,
            address = address.trim(),
            city = city.trim(),
            whatsappNumber = whatsappNumber.trim(),
            fleetAvailable = listOf("Automatic Scooters", "Helmets Included", "Roadside Assistance"),
            priceStartingFrom = priceStartingFrom.trim()
        )

        // Step 4: Write to Realtime Database
        rentalsRef.child(shopKey).setValue(newShop).await()

        return newShop
    }

    /**
     * Update an existing rental shop under `/rentals/{shopId}` using `updateChildren`.
     */
    suspend fun updateRentalShop(
        shopId: String,
        updates: Map<String, Any?>,
        newImageUri: Uri? = null
    ) {
        val mutableUpdates = updates.toMutableMap()

        // Upload new image if provided and update the imageUrl field
        if (newImageUri != null) {
            val newDownloadUrl = uploadImage(newImageUri, folder = "rentals")
            mutableUpdates["imageUrl"] = newDownloadUrl
        }

        database.child("rentals").child(shopId).updateChildren(mutableUpdates).await()
    }

    /**
     * Delete a rental shop from `/rentals/{shopId}` using `removeValue()`.
     */
    suspend fun deleteRentalShop(shopId: String, imageUrl: String? = null) {
        database.child("rentals").child(shopId).removeValue().await()
        if (!imageUrl.isNullOrBlank()) {
            deleteImageByUrl(imageUrl)
        }
    }

    /**
     * Fetch all rental shops once from `/rentals`.
     */
    suspend fun getAllRentalsOnce(): List<RentalShop> {
        val snapshot = database.child("rentals").get().await()
        val list = mutableListOf<RentalShop>()
        for (child in snapshot.children) {
            child.getValue(RentalShop::class.java)?.let { list.add(it) }
        }
        return list
    }

    /**
     * Real-time stream of all rental shops from `/rentals`.
     */
    fun observeRentals(): Flow<List<RentalShop>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<RentalShop>()
                for (child in snapshot.children) {
                    child.getValue(RentalShop::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val ref = database.child("rentals")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // =========================================================================
    // 3. REALTIME DATABASE: LOCAL PRODUCTS CRUD (/products)
    // =========================================================================

    suspend fun addProduct(
        name: String,
        category: String,
        description: String,
        priceLkr: Double,
        originRegion: String = "Sri Lanka",
        stockQuantity: Int = 50,
        imageUri: Uri? = null,
        existingImageUrl: String = ""
    ): com.example.lankago.data.LocalProduct {
        val finalImageUrl = if (imageUri != null) {
            uploadImage(imageUri, folder = "products")
        } else {
            existingImageUrl
        }

        val productsRef = database.child("products")
        val prodKey = productsRef.push().key ?: UUID.randomUUID().toString()

        val product = com.example.lankago.data.LocalProduct(
            id = prodKey,
            name = name.trim(),
            category = category.trim(),
            description = description.trim(),
            priceLkr = priceLkr,
            originRegion = originRegion.trim(),
            inStock = stockQuantity > 0,
            stockQuantity = stockQuantity,
            imageUrl = finalImageUrl.trim(),
            rating = 5.0f,
            reviewCount = 1
        )

        productsRef.child(prodKey).setValue(product).await()
        return product
    }

    suspend fun updateProduct(
        productId: String,
        updates: Map<String, Any?>,
        newImageUri: Uri? = null
    ) {
        val mutableUpdates = updates.toMutableMap()
        if (newImageUri != null) {
            val downloadUrl = uploadImage(newImageUri, folder = "products")
            mutableUpdates["imageUrl"] = downloadUrl
        }
        database.child("products").child(productId).updateChildren(mutableUpdates).await()
    }

    suspend fun deleteProduct(productId: String, imageUrl: String? = null) {
        database.child("products").child(productId).removeValue().await()
        if (!imageUrl.isNullOrBlank()) {
            deleteImageByUrl(imageUrl)
        }
    }

    suspend fun getAllProductsOnce(): List<com.example.lankago.data.LocalProduct> {
        val snapshot = database.child("products").get().await()
        val list = mutableListOf<com.example.lankago.data.LocalProduct>()
        for (child in snapshot.children) {
            child.getValue(com.example.lankago.data.LocalProduct::class.java)?.let { list.add(it) }
        }
        return list
    }

    fun observeProducts(): Flow<List<com.example.lankago.data.LocalProduct>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.lankago.data.LocalProduct>()
                for (child in snapshot.children) {
                    child.getValue(com.example.lankago.data.LocalProduct::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child("products")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // =========================================================================
    // 3.5. REALTIME DATABASE: TRADITIONAL ACTIVITIES & TOURS CRUD (/activities)
    // =========================================================================

    suspend fun addActivity(
        title: String,
        category: String,
        location: String,
        address: String,
        duration: String,
        priceLkr: Double,
        hostName: String,
        description: String,
        highlights: List<String> = emptyList(),
        timeSlots: List<String> = emptyList(),
        imageUri: Uri? = null,
        existingImageUrl: String = ""
    ): com.example.lankago.data.TraditionalActivity {
        val finalImageUrl = if (imageUri != null) {
            uploadImage(imageUri, folder = "activities")
        } else {
            existingImageUrl
        }

        val activitiesRef = database.child("activities")
        val actKey = activitiesRef.push().key ?: UUID.randomUUID().toString()

        val activity = com.example.lankago.data.TraditionalActivity(
            id = actKey,
            title = title.trim(),
            category = category.trim(),
            location = location.trim(),
            address = address.trim(),
            duration = duration.trim(),
            durationHours = 2.0,
            hostName = hostName.trim(),
            hostTitle = "Master Artisan & Guide",
            hostAvatarRes = 0,
            priceLkr = priceLkr,
            rating = 5.0f,
            reviewCount = 1,
            imageRes = 0,
            description = description.trim(),
            highlights = highlights,
            timeSlots = if (timeSlots.isEmpty()) listOf("09:00 AM", "02:00 PM") else timeSlots,
            imageUrl = finalImageUrl.trim()
        )

        activitiesRef.child(actKey).setValue(activity).await()
        return activity
    }

    suspend fun updateActivity(
        activityId: String,
        updates: Map<String, Any?>,
        newImageUri: Uri? = null
    ) {
        val mutableUpdates = updates.toMutableMap()
        if (newImageUri != null) {
            val downloadUrl = uploadImage(newImageUri, folder = "activities")
            mutableUpdates["imageUrl"] = downloadUrl
        }
        database.child("activities").child(activityId).updateChildren(mutableUpdates).await()
    }

    suspend fun deleteActivity(activityId: String, imageUrl: String? = null) {
        database.child("activities").child(activityId).removeValue().await()
        if (!imageUrl.isNullOrBlank()) {
            deleteImageByUrl(imageUrl)
        }
    }

    suspend fun getAllActivitiesOnce(): List<com.example.lankago.data.TraditionalActivity> {
        val snapshot = database.child("activities").get().await()
        val list = mutableListOf<com.example.lankago.data.TraditionalActivity>()
        for (child in snapshot.children) {
            child.getValue(com.example.lankago.data.TraditionalActivity::class.java)?.let { list.add(it) }
        }
        return list
    }

    fun observeActivities(): Flow<List<com.example.lankago.data.TraditionalActivity>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.lankago.data.TraditionalActivity>()
                for (child in snapshot.children) {
                    child.getValue(com.example.lankago.data.TraditionalActivity::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child("activities")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // =========================================================================
    // 4. REALTIME DATABASE: USER BOOKINGS (/bookings)
    // =========================================================================

    suspend fun addBooking(booking: com.example.lankago.data.Booking): com.example.lankago.data.Booking {
        val bookingsRef = database.child("bookings")
        val key = booking.id.ifBlank { bookingsRef.push().key ?: UUID.randomUUID().toString() }
        val finalBooking = booking.copy(id = key)
        bookingsRef.child(key).setValue(finalBooking).await()
        return finalBooking
    }

    fun observeBookings(): Flow<List<com.example.lankago.data.Booking>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.lankago.data.Booking>()
                for (child in snapshot.children) {
                    child.getValue(com.example.lankago.data.Booking::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child("bookings")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getAllBookingsOnce(): List<com.example.lankago.data.Booking> {
        val snapshot = database.child("bookings").get().await()
        val list = mutableListOf<com.example.lankago.data.Booking>()
        for (child in snapshot.children) {
            child.getValue(com.example.lankago.data.Booking::class.java)?.let { list.add(it) }
        }
        return list
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String) {
        database.child("bookings").child(bookingId).child("status").setValue(newStatus).await()
    }

    // =========================================================================
    // 5. AUTHENTICATION & USER NODE MANAGEMENT (/users/{uid})
    // =========================================================================

    /**
     * Save user profile details to `/users/{uid}` with role.
     */
    suspend fun saveUserNode(uid: String, name: String, email: String, role: String = "user") {
        val user = User(
            uid = uid,
            name = name.trim(),
            email = email.trim(),
            role = role.trim(),
            createdAt = System.currentTimeMillis()
        )
        database.child("users").child(uid).setValue(user).await()
    }

    /**
     * Retrieve the user role from `/users/{uid}/role`.
     */
    suspend fun getUserRole(uid: String): String {
        val snapshot = database.child("users").child(uid).child("role").get().await()
        return snapshot.getValue(String::class.java) ?: "user"
    }

    /**
     * Verify if currently logged-in user has "admin" role.
     */
    suspend fun isCurrentUserAdmin(): Boolean {
        val currentUid = auth.currentUser?.uid ?: return false
        return getUserRole(currentUid) == "admin"
    }
}
