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

/**
 * Production-ready Trip History screen for LankaGo travelers.
 * Fetches and displays past & scheduled itineraries from Firebase Realtime Database (/users/{uid}/tripHistory).
 * Features:
 * - Realtime cloud syncing with offline local fallbacks via [TripManager].
 * - Standard loading spinners and empty state handling via [showLoadingState] and [showEmptyState].
 * - Fail-safe debounced navigation to trip creation and trip details.
 */
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
     * Loads trip history from Firebase Realtime Database node: `/users/{uid}/tripHistory`
     * Falls back to [TripManager] offline storage if disconnected.
     */
    private fun loadTripHistory() {
        view?.showLoadingState(contentView = rvTrips, emptyStateLayout = layoutEmptyState)

        viewLifecycleOwner.lifecycleScope.launch {
            val authUser = FirebaseAuth.getInstance().currentUser
            val uid = authUser?.uid ?: "guest_traveler"
            val rtdb = FirebaseDatabase.getInstance().reference

            try {
                val snapshot = withContext(Dispatchers.IO) {
                    try {
                        rtdb.child("users")
                            .child(uid)
                            .child("tripHistory")
                            .get()
                            .await()
                    } catch (e: Exception) {
                        Log.w("TripHistoryFragment", "Remote fetch failed, checking fallback: ${e.message}")
                        null
                    }
                }

                val tripList = mutableListOf<Trip>()

                if (snapshot != null && snapshot.exists()) {
                    for (child in snapshot.children) {
                        try {
                            val trip = child.getValue(Trip::class.java)
                            if (trip != null) {
                                tripList.add(trip)
                            }
                        } catch (parseEx: Exception) {
                            Log.w("TripHistoryFragment", "Error parsing trip: ${parseEx.message}")
                        }
                    }
                }

                // If remote is empty, check local offline store
                if (tripList.isEmpty()) {
                    val localTrips = TripManager.getAllTrips(requireContext())
                    tripList.addAll(localTrips)
                }

                // Sort descending by timestamp / creation
                tripList.sortByDescending { it.timestamp }

                if (!isAdded) return@launch

                if (tripList.isNotEmpty()) {
                    adapter.submitList(tripList)
                    view?.showContentState(contentView = rvTrips, emptyStateLayout = layoutEmptyState)
                } else {
                    view?.showEmptyState(
                        contentView = rvTrips,
                        emptyStateLayout = layoutEmptyState,
                        title = "No Trips Planned Yet",
                        message = "You haven't scheduled any journeys in Sri Lanka yet. Plan your dream itinerary today!",
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

                // Safe fallback to local trips
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
