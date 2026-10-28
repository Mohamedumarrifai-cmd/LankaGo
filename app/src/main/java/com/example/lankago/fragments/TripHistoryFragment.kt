package com.example.lankago.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.TripHistoryAdapter
import com.example.lankago.data.Trip
import com.example.lankago.utils.TripManager
import com.example.lankago.utils.safeNavigate
import com.example.lankago.utils.setOnSafeClickListener
import com.example.lankago.utils.showContentState
import com.example.lankago.utils.showEmptyState
import com.example.lankago.utils.showLoadingState
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


 // Production-ready Trip History screen for LankaGo travelers.

class TripHistoryFragment : Fragment() {

    private lateinit var rvTrips: RecyclerView
    private lateinit var layoutEmptyState: View
    private lateinit var btnBack: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnAddTrip: MaterialButton

    private lateinit var adapter: TripHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupListeners()
        loadTripHistory()
    }

    private fun initViews(view: View) {
        rvTrips = view.findViewById(R.id.rv_trip_history)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        btnBack = view.findViewById(R.id.btn_back_trip_history)
        btnRefresh = view.findViewById(R.id.btn_refresh_trip_history)
        btnAddTrip = view.findViewById(R.id.btn_add_trip_history)
    }

    private fun setupRecyclerView() {
        adapter = TripHistoryAdapter(
            onTripClick = { selectedTrip: Trip ->
                showTripDetailsDialog(selectedTrip)
            }
        )

        rvTrips.layoutManager = LinearLayoutManager(requireContext())
        rvTrips.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnSafeClickListener {
            findNavController().navigateUp()
        }

        btnRefresh.setOnSafeClickListener {
            loadTripHistory()
        }

        btnAddTrip.setOnSafeClickListener {
            try {
                findNavController().safeNavigate(R.id.createTripFragment)
            } catch (_: Exception) {
                findNavController().safeNavigate(R.id.planTripFragment)
            }
        }
    }

    /**
     * Loads trip history from Firebase Realtime Database nodes:
     * - `/users/{uid}/tripHistory`
     * - `/trips/{uid}`
     * - `/bookings` (matching userId or userEmail)
     * And merges with [TripManager] offline storage so no past or current bookings are ever lost.
     */
    private fun loadTripHistory() {
        view?.showLoadingState(contentView = rvTrips, emptyStateLayout = layoutEmptyState)

        viewLifecycleOwner.lifecycleScope.launch {
            val authUser = FirebaseAuth.getInstance().currentUser
            val uid = authUser?.uid ?: "guest_traveler"
            val userEmail = authUser?.email ?: ""
            val rtdb = FirebaseDatabase.getInstance().reference

            try {
                val tripMap = mutableMapOf<String, Trip>()

                withContext(Dispatchers.IO) {
                    // 1. Fetch primary /users/{uid}/tripHistory
                    try {
                        val userTripsSnap = rtdb.child("users").child(uid).child("tripHistory").get().await()
                        if (userTripsSnap.exists()) {
                            for (child in userTripsSnap.children) {
                                try {
                                    val trip = child.getValue(Trip::class.java)
                                    if (trip != null && trip.id.isNotBlank()) {
                                        tripMap[trip.id] = trip
                                    }
                                } catch (e: Exception) {
                                    Log.w("TripHistoryFragment", "Error parsing /users/$uid/tripHistory child: ${e.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("TripHistoryFragment", "Fetch /users/$uid/tripHistory failed: ${e.message}")
                    }

                    // 2. Fetch cross-referenced /trips/{uid}
                    try {
                        val tripsSnap = rtdb.child("trips").child(uid).get().await()
                        if (tripsSnap.exists()) {
                            for (child in tripsSnap.children) {
                                try {
                                    val trip = child.getValue(Trip::class.java)
                                    if (trip != null && trip.id.isNotBlank() && !tripMap.containsKey(trip.id)) {
                                        tripMap[trip.id] = trip
                                    }
                                } catch (e: Exception) {
                                    Log.w("TripHistoryFragment", "Error parsing /trips/$uid child: ${e.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("TripHistoryFragment", "Fetch /trips/$uid failed: ${e.message}")
                    }

                    // 3. Fetch from /bookings node (all past rental & activity bookings made by this account)
                    try {
                        val bookingsSnap = rtdb.child("bookings").get().await()
                        if (bookingsSnap.exists()) {
                            for (child in bookingsSnap.children) {
                                try {
                                    val booking = child.getValue(com.example.lankago.data.Booking::class.java)
                                    if (booking != null) {
                                        val isOwner = (booking.userId.isNotBlank() && booking.userId == uid) ||
                                                (userEmail.isNotBlank() && booking.userEmail.equals(userEmail, ignoreCase = true))

                                        if (isOwner) {
                                            // Check if this booking is already attached to any trip
                                            val alreadyIncluded = tripMap.values.any { t ->
                                                t.id == booking.tripId || t.id == booking.id ||
                                                        t.bookedRentals.any { r -> r.name.contains(booking.itemName, ignoreCase = true) } ||
                                                        t.bookedActivities.any { a -> a.title.contains(booking.itemName, ignoreCase = true) }
                                            }

                                            if (!alreadyIncluded) {
                                                // Synthesize a Trip entry for this booking so it is prominently visible in history
                                                val bookingTripId = if (booking.tripId.isNotBlank()) booking.tripId else booking.id.ifBlank { "book_${System.currentTimeMillis()}" }
                                                val rentalList = mutableListOf<com.example.lankago.data.RentalVehicle>()
                                                val activityList = mutableListOf<com.example.lankago.data.ActivityItem>()

                                                if (booking.bookingType == com.example.lankago.data.BookingType.RENTAL) {
                                                    rentalList.add(
                                                        com.example.lankago.data.RentalVehicle(
                                                            id = booking.id,
                                                            name = booking.itemName,
                                                            type = booking.itemCategory.ifBlank { "Vehicle / Bike Rental" },
                                                            dailyRateLkr = booking.totalPriceLkr,
                                                            location = "Sri Lanka",
                                                            isAvailable = true
                                                        )
                                                    )
                                                } else {
                                                    activityList.add(
                                                        com.example.lankago.data.ActivityItem(
                                                            id = booking.id,
                                                            title = booking.itemName,
                                                            category = booking.itemCategory.ifBlank { "Activity" },
                                                            basePriceLkr = booking.totalPriceLkr
                                                        )
                                                    )
                                                }

                                                val synthesizedTrip = Trip(
                                                    id = bookingTripId,
                                                    tripName = "${booking.itemName} Reservation",
                                                    date = booking.bookingDate.ifBlank { "Confirmed Booking" },
                                                    travelerType = "Verified Traveler",
                                                    places = mutableListOf(),
                                                    bookedRentals = rentalList,
                                                    bookedActivities = activityList,
                                                    destination = booking.specialNotes.ifBlank { "Sri Lanka" },
                                                    status = booking.status.name
                                                )
                                                tripMap[bookingTripId] = synthesizedTrip
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w("TripHistoryFragment", "Error parsing /bookings child: ${e.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("TripHistoryFragment", "Fetch /bookings failed: ${e.message}")
                    }

                    // 4. Merge with offline local storage from TripManager
                    val localTrips = TripManager.getAllTrips(requireContext())
                    for (localTrip in localTrips) {
                        if (!tripMap.containsKey(localTrip.id)) {
                            tripMap[localTrip.id] = localTrip
                            // Auto sync local trip to Firebase cloud if user is logged in
                            if (uid != "guest_traveler") {
                                try {
                                    rtdb.child("users").child(uid).child("tripHistory").child(localTrip.id).setValue(localTrip)
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }

                val finalTripList = tripMap.values.toMutableList()
                finalTripList.sortByDescending { it.timestamp }

                if (!isAdded) return@launch

                if (finalTripList.isNotEmpty()) {
                    adapter.submitList(finalTripList)
                    view?.showContentState(contentView = rvTrips, emptyStateLayout = layoutEmptyState)
                } else {
                    view?.showEmptyState(
                        contentView = rvTrips,
                        emptyStateLayout = layoutEmptyState,
                        title = "No Trips or Bookings Yet",
                        message = "You haven't scheduled any journeys or vehicle rentals yet. Book your rental or plan an itinerary today!",
                        iconRes = R.drawable.ic_home_trip_plan,
                        actionButtonText = "Plan a New Trip",
                        onActionClick = {
                            try {
                                findNavController().safeNavigate(R.id.createTripFragment)
                            } catch (_: Exception) {
                                findNavController().safeNavigate(R.id.planTripFragment)
                            }
                        }
                    )
                }

            } catch (e: Exception) {
                if (!isAdded) return@launch
                Log.e("TripHistoryFragment", "Error loading trip history", e)

                val fallbackTrips = TripManager.getAllTrips(requireContext())
                if (fallbackTrips.isNotEmpty()) {
                    adapter.submitList(fallbackTrips)
                    view?.showContentState(contentView = rvTrips, emptyStateLayout = layoutEmptyState)
                } else {
                    view?.showEmptyState(
                        contentView = rvTrips,
                        emptyStateLayout = layoutEmptyState,
                        title = "No Trips Available",
                        message = "Unable to fetch trip history. Tap below to create a new trip.",
                        iconRes = R.drawable.ic_home_trip_plan,
                        actionButtonText = "Plan a New Trip",
                        onActionClick = {
                            findNavController().safeNavigate(R.id.createTripFragment)
                        }
                    )
                }
            }
        }
    }

    /**
     * Shows a rich Material dialog displaying full details of the selected past trip.
     * Provides an option to navigate to the Trip Planner with this trip active.
     */
    private fun showTripDetailsDialog(trip: Trip) {
        val message = StringBuilder()
        message.append("• Destination: ${if (trip.destination.isNotBlank()) trip.destination else trip.tripName}\n")
        message.append("• Scheduled Dates: ${trip.date}\n")
        message.append("• Traveler Status: ${trip.travelerType}\n")
        message.append("• Status: ${trip.status}\n\n")

        message.append("--- ITINERARY DETAILS ---\n")
        if (trip.bookedRentals.isNotEmpty()) {
            message.append("Rentals (${trip.bookedRentals.size}):\n")
            trip.bookedRentals.forEach { rental ->
                message.append("  - ${rental.name} (${rental.location}) - Rs. ${rental.dailyRateLkr.toInt()}/day\n")
            }
            message.append("\n")
        } else {
            message.append("• Rentals: None booked\n")
        }

        if (trip.bookedActivities.isNotEmpty()) {
            message.append("Activities (${trip.bookedActivities.size}):\n")
            trip.bookedActivities.forEach { act ->
                message.append("  - ${act.title} (${act.durationHours} hrs)\n")
            }
            message.append("\n")
        } else {
            message.append("• Activities: None booked\n")
        }

        if (trip.places.isNotEmpty()) {
            message.append("Places to Visit (${trip.places.size}):\n")
            trip.places.forEach { place ->
                message.append("  - ${place.name} (${place.location})\n")
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(trip.tripName)
            .setMessage(message.toString().trim())
            .setPositiveButton("Open in Planner") { _, _ ->
                val bundle = bundleOf("activeTripId" to trip.id)
                findNavController().safeNavigate(R.id.tripPlannerFragment, bundle)
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
