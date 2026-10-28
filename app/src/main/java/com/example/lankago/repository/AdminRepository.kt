package com.example.lankago.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.lankago.LankaGoApp
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
import java.io.ByteArrayOutputStream
import java.util.UUID


// Production-ready Repository handling Firebase operations:
// Authentication & User Nodes (`/users/{uid}`)
// Realtime Database CRUD (`/rentals`, `/products`, `/bookings`)
// Firebase Storage Image Uploads with Base64 fallback for Spark free tier

class AdminRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val storage: StorageReference = FirebaseStorage.getInstance().reference
) {


    // 1. FIREBASE STORAGE: IMAGE UPLOADS (WITH BASE64 FALLBACK)

    /**
      Upload an image file from the device to Firebase Storage.
      If Cloud Storage is not enabled or provisioned (e.g. Spark free plan without billing),
      it automatically compresses and encodes the image into a Base64 data URI to store
      directly in the Realtime Database for 100% free hosting.
     */
    suspend fun uploadImage(imageUri: Uri, folder: String = "rentals"): String {
        // 1. Attempt Cloud Storage upload if provisioned
        try {
            val fileName = "${folder}/${UUID.randomUUID()}.jpg"
            val fileRef = storage.child(fileName)
            fileRef.putFile(imageUri).await()
            return fileRef.downloadUrl.await().toString()
        } catch (storageEx: Throwable) {
            Log.w("AdminRepository", "Cloud Storage unavailable or not provisioned: ${storageEx.message}. Using compressed Base64 Realtime DB storage fallback.")
        }

        // 2. Safe Fallback: Compress and convert image to Base64
        return try {
            encodeUriToBase64(imageUri, folder)
        } catch (e: Exception) {
            Log.e("AdminRepository", "Base64 fallback failed: ${e.message}", e)
            getDefaultPlaceholder(folder)
        }
    }

    private fun encodeUriToBase64(imageUri: Uri, folder: String): String {
        return try {
            val context = LankaGoApp.instance
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return getDefaultPlaceholder(folder)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return getDefaultPlaceholder(folder)

            val maxDim = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = width.toFloat() / height.toFloat()

            val newWidth: Int
            val newHeight: Int
            if (width > height) {
                newWidth = minOf(width, maxDim)
                newHeight = (newWidth / ratio).toInt().coerceAtLeast(1)
            } else {
                newHeight = minOf(height, maxDim)
                newWidth = (newHeight * ratio).toInt().coerceAtLeast(1)
            }

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("AdminRepository", "Error in encodeUriToBase64", e)
            getDefaultPlaceholder(folder)
        }
    }

    private fun getDefaultPlaceholder(folder: String): String {
        return when (folder.lowercase()) {
            "rentals" -> "https://images.unsplash.com/photo-1558981403-c5f9899a28bc"
            "products" -> "https://images.unsplash.com/photo-1607344645866-009c320c5ab8"
            "activities" -> "https://images.unsplash.com/photo-1544735716-392fe2489ffa"
            else -> "https://images.unsplash.com/photo-1558981403-c5f9899a28bc"
        }
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

    // 2. REALTIME DATABASE: RENTAL SHOPS CRUD (/rentals)

    /**
     Create and push a new rental shop to the `/rentals` node.
     1. Uploads the image to Firebase Storage if an [imageUri] is provided.
     2. Generates a unique key using `push().key`.
     3. Sets the complete object in the database using `setValue()`.
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

    // 3. REALTIME DATABASE: LOCAL PRODUCTS CRUD (/products)
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

    // 3.5. REALTIME DATABASE: TRADITIONAL ACTIVITIES & TOURS CRUD (/activities)
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

    // 4. REALTIME DATABASE: USER BOOKINGS (/bookings)
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


    // 5. AUTHENTICATION & USER NODE MANAGEMENT (/users/{uid})

//      Save user profile details to `/users/{uid}` with role.

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


// Retrieve the user role from `/users/{uid}/role`.

    suspend fun getUserRole(uid: String): String {
        val snapshot = database.child("users").child(uid).child("role").get().await()
        return snapshot.getValue(String::class.java) ?: "user"
    }

//     Verify if currently logged-in user has "admin" role.

    suspend fun isCurrentUserAdmin(): Boolean {
        val currentUid = auth.currentUser?.uid ?: return false
        return getUserRole(currentUid) == "admin"
    }

    // 6. REALTIME DATABASE: SHOPPING MALLS CRUD (/malls)
    suspend fun addMall(
        name: String,
        city: String = "Colombo",
        address: String,
        description: String,
        operatingHours: String = "10:00 AM - 10:00 PM",
        contactNumber: String = "+94 11 200 0000",
        storeCount: Int = 100,
        rating: Double = 4.8,
        category: String = "Shopping & Leisure",
        features: List<String> = listOf("Cinema", "Food Court", "Supermarket", "Parking", "Shopping"),
        imageUri: Uri? = null,
        existingImageUrl: String = ""
    ): com.example.lankago.data.Mall {
        val finalImageUrl = if (imageUri != null) {
            uploadImage(imageUri, folder = "malls")
        } else {
            existingImageUrl
        }

        val mallsRef = database.child("malls")
        val mallKey = mallsRef.push().key ?: UUID.randomUUID().toString()

        val mall = com.example.lankago.data.Mall(
            id = mallKey,
            name = name.trim(),
            city = city.trim(),
            address = address.trim(),
            description = description.trim(),
            operatingHours = operatingHours.trim(),
            imageUrl = finalImageUrl.trim(),
            imageRes = 0,
            rating = rating,
            storeCount = storeCount,
            contactNumber = contactNumber.trim(),
            category = category.trim(),
            features = features
        )

        mallsRef.child(mallKey).setValue(mall).await()
        return mall
    }

    suspend fun updateMall(
        mallId: String,
        updates: Map<String, Any?>,
        newImageUri: Uri? = null
    ) {
        val mutableUpdates = updates.toMutableMap()
        if (newImageUri != null) {
            val downloadUrl = uploadImage(newImageUri, folder = "malls")
            mutableUpdates["imageUrl"] = downloadUrl
        }
        database.child("malls").child(mallId).updateChildren(mutableUpdates).await()
    }

    suspend fun deleteMall(mallId: String, imageUrl: String? = null) {
        database.child("malls").child(mallId).removeValue().await()
        if (!imageUrl.isNullOrBlank()) {
            deleteImageByUrl(imageUrl)
        }
    }

    suspend fun getAllMallsOnce(): List<com.example.lankago.data.Mall> {
        val snapshot = database.child("malls").get().await()
        val list = mutableListOf<com.example.lankago.data.Mall>()
        for (child in snapshot.children) {
            child.getValue(com.example.lankago.data.Mall::class.java)?.let { list.add(it) }
        }
        return list
    }

    fun observeMalls(): Flow<List<com.example.lankago.data.Mall>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.lankago.data.Mall>()
                for (child in snapshot.children) {
                    child.getValue(com.example.lankago.data.Mall::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.child("malls")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
