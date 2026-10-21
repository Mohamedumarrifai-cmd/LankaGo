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
    // 3. AUTHENTICATION & USER NODE MANAGEMENT (/users/{uid})
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
