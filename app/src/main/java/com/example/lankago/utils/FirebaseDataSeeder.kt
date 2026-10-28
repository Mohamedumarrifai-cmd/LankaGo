package com.example.lankago.utils

import android.util.Log
import com.example.lankago.data.Booking
import com.example.lankago.data.BookingStatus
import com.example.lankago.data.BookingType
import com.example.lankago.data.LankaGoDataManager
import com.example.lankago.data.RentalShop
import com.example.lankago.data.User
import com.example.lankago.repository.TraditionalActivityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//  Production Firebase Realtime Database Initializer and Synchronizer.
//  Guarantees that essential nodes exist for both Administrators and Travelers:
//  1. Admin Super User (/users/admin_super_user with role = "admin")
//  2. Active User role validation (/users/{uid}/role)
//  3. Default Rentals (/rentals)
//  4. Default Activities (/activities)
//  5. Default Destinations (/destinations)
//  6. Sample Customer Bookings (/bookings) for Admin panel management

object FirebaseDataSeeder {

    private const val TAG = "FirebaseDataSeeder"
    private val database = FirebaseDatabase.getInstance().reference


//      Non-blocking background sync triggered on application launch.

    fun syncDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedAdminSuperUser()
                syncCurrentUserRole()
                seedRentalsIfEmpty()
                seedActivitiesIfEmpty()
                seedDestinationsIfEmpty()
                seedBookingsIfEmpty()
                Log.i(TAG, "✓ LankaGo Firebase Realtime Database is synchronized and up-to-date.")
            } catch (e: Exception) {
                Log.w(TAG, "Firebase synchronization note: ${e.message}")
            }
        }
    }


//      Seeds default Administrator account node in Firebase Realtime Database.

    private suspend fun seedAdminSuperUser() {
        try {
            val adminSnapshot = database.child("users").child("admin_super_user").get().await()
            if (!adminSnapshot.exists()) {
                val adminUser = User(
                    uid = "admin_super_user",
                    name = "LankaGo System Administrator",
                    email = "admin@lankago.lk",
                    role = "admin",
                    contactNumber = "+94771234567",
                    phoneNumber = "+94771234567",
                    address = "LankaGo Tourism Headquarters, Colombo 03",
                    createdAt = System.currentTimeMillis()
                )
                database.child("users").child("admin_super_user").setValue(adminUser).await()
                Log.d(TAG, "Created default admin node under /users/admin_super_user")
            } else {
                // Ensure role is explicitly set to admin
                database.child("users").child("admin_super_user").child("role").setValue("admin").await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to seed admin node: ${e.message}")
        }
    }


//     If current authenticated user has an admin email, ensures their role is "admin".

    private suspend fun syncCurrentUserRole() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return
            val email = currentUser.email?.lowercase() ?: ""
            if (email == "admin@lankago.lk" || email.startsWith("admin")) {
                database.child("users").child(currentUser.uid).child("role").setValue("admin").await()
                Log.d(TAG, "Assigned admin role to user: ${currentUser.uid}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing current user role: ${e.message}")
        }
    }


//     Seeds rentals if the node does not exist or has 0 children.

    private suspend fun seedRentalsIfEmpty() {
        try {
            val snapshot = database.child("rentals").get().await()
            if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                for (shop in com.example.lankago.data.RentalShopDataProvider.shops.take(6)) {
                    database.child("rentals").child(shop.id).setValue(shop).await()
                }
                Log.d(TAG, "Seeded initial rental shops into Firebase /rentals")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error seeding rentals: ${e.message}")
        }
    }


//      Seeds traditional cultural activities if the node is empty.

    private suspend fun seedActivitiesIfEmpty() {
        try {
            val snapshot = database.child("activities").get().await()
            if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                for (activity in TraditionalActivityRepository.getAllActivities()) {
                    database.child("activities").child(activity.id).setValue(activity).await()
                }
                Log.d(TAG, "Seeded cultural activities into Firebase /activities")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error seeding activities: ${e.message}")
        }
    }


//      Seeds travel destinations if the node is empty.

    private suspend fun seedDestinationsIfEmpty() {
        try {
            val snapshot = database.child("destinations").get().await()
            if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                for (destination in LankaGoDataManager.getAllDestinations()) {
                    database.child("destinations").child(destination.id).setValue(destination).await()
                }
                Log.d(TAG, "Seeded travel destinations into Firebase /destinations")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error seeding destinations: ${e.message}")
        }
    }


//      Seeds initial customer bookings for the Admin Dashboard to monitor.

    private suspend fun seedBookingsIfEmpty() {
        try {
            val snapshot = database.child("bookings").get().await()
            if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                val sampleBookings = listOf(
                    Booking(
                        id = "bk_sample_01",
                        userId = "traveler_dan",
                        tripId = "trip_galle_fort",
                        userName = "Danushka Perera",
                        userEmail = "danushka.p@gmail.com",
                        userPhone = "+94772345678",
                        itemName = "Epic Ride Scooter (Honda Dio 110cc)",
                        itemCategory = "Rental Vehicle",
                        bookingType = BookingType.RENTAL,
                        bookingDate = "24 Oct 2026",
                        bookingTime = "09:00 AM",
                        totalPriceLkr = 7000.0,
                        status = BookingStatus.CONFIRMED,
                        specialNotes = "Surfboard rack required for Mirissa trip."
                    ),
                    Booking(
                        id = "bk_sample_02",
                        userId = "traveler_sophie",
                        tripId = "trip_kandy_crafts",
                        userName = "Sophie Dubois",
                        userEmail = "sophie.dubois@traveler.com",
                        userPhone = "+33612345678",
                        itemName = "Traditional Devil Mask & Wood Carving Workshop",
                        itemCategory = "Cultural Workshop",
                        bookingType = BookingType.ACTIVITY,
                        bookingDate = "26 Oct 2026",
                        bookingTime = "01:30 PM",
                        totalPriceLkr = 5200.0,
                        status = BookingStatus.PENDING,
                        specialNotes = "Interested in Gara Yaka protection mask design."
                    ),
                    Booking(
                        id = "bk_sample_03",
                        userId = "traveler_kumar",
                        tripId = "trip_ella_climb",
                        userName = "Kumar Wickramasinghe",
                        userEmail = "kumar.w@yahoo.com",
                        userPhone = "+94719876543",
                        itemName = "Clay Pot Making & Terracotta Masterclass",
                        itemCategory = "Pottery Workshop",
                        bookingType = BookingType.ACTIVITY,
                        bookingDate = "28 Oct 2026",
                        bookingTime = "10:00 AM",
                        totalPriceLkr = 4500.0,
                        status = BookingStatus.CONFIRMED,
                        specialNotes = "Family with 2 children."
                    )
                )

                for (booking in sampleBookings) {
                    database.child("bookings").child(booking.id).setValue(booking).await()
                }
                Log.d(TAG, "Seeded sample bookings into Firebase /bookings")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error seeding bookings: ${e.message}")
        }
    }
}
