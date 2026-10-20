package com.example.lankago.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.TripAdapter
import com.example.lankago.data.Trip
import com.example.lankago.utils.TripManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class TripPlannerFragment : Fragment() {

    private lateinit var tripAdapter: TripAdapter
    private lateinit var rvTrips: RecyclerView
    private lateinit var layoutEmptyTrips: View
    private lateinit var fabCreateTrip: ExtendedFloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trip_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupFab()
        loadTrips()
    }

    override fun onResume() {
        super.onResume()
        loadTrips()
    }

    private fun initViews(view: View) {
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        rvTrips = view.findViewById(R.id.rvTrips)
        layoutEmptyTrips = view.findViewById(R.id.layout_empty_trips)
        fabCreateTrip = view.findViewById(R.id.fab_create_trip)
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            trips = mutableListOf(),
            onTripClick = { trip ->
                showTripItineraryDialog(trip)
            },
            onDeleteClick = { trip ->
                confirmDeleteTrip(trip)
            }
        )

        rvTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tripAdapter
        }
    }

    private fun setupFab() {
        fabCreateTrip.setOnClickListener {
            showCreateTripDialog()
        }
    }

    private fun loadTrips() {
        val trips = TripManager.getAllTrips(requireContext())
        tripAdapter.updateData(trips)

        if (trips.isEmpty()) {
            layoutEmptyTrips.visibility = View.VISIBLE
            rvTrips.visibility = View.GONE
        } else {
            layoutEmptyTrips.visibility = View.GONE
            rvTrips.visibility = View.VISIBLE
        }
    }

    private fun showCreateTripDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.create_trip, null)
        val tilDestination = dialogView.findViewById<TextInputLayout>(R.id.til_destination)
        val etDestination = dialogView.findViewById<EditText>(R.id.et_destination)
        val tilDates = dialogView.findViewById<TextInputLayout>(R.id.til_trip_dates)
        val etDates = dialogView.findViewById<EditText>(R.id.et_trip_dates)
        val etTravelers = dialogView.findViewById<EditText>(R.id.et_travelers_count)
        val cbAddRental = dialogView.findViewById<CheckBox>(R.id.cb_add_rental)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_trip)
        val btnCreate = dialogView.findViewById<Button>(R.id.btn_confirm_create_trip)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnCreate.setOnClickListener {
            val destination = etDestination.text?.toString()?.trim() ?: ""
            val dates = etDates.text?.toString()?.trim() ?: ""
            val travelers = etTravelers.text?.toString()?.trim() ?: "2"

            // UI Validation
            if (destination.isEmpty()) {
                tilDestination.error = "Please enter a destination (e.g. Ella, Sigiriya)"
                etDestination.requestFocus()
                return@setOnClickListener
            }
            tilDestination.error = null

            if (dates.isEmpty()) {
                tilDates.error = "Please specify travel dates"
                etDates.requestFocus()
                return@setOnClickListener
            }
            tilDates.error = null

            // Construct new trip
            val newTrip = Trip(
                id = "trip_${System.currentTimeMillis()}",
                tripName = "$destination Journey ($travelers Travelers)",
                date = dates,
                travelerType = "Local",
                idImageUri = null,
                places = mutableListOf(),
                bookedRentals = mutableListOf(),
                bookedActivities = mutableListOf()
            )

            // Save persistently
            TripManager.saveTrip(requireContext(), newTrip)
            dialog.dismiss()
            loadTrips()

            Snackbar.make(
                requireView(),
                "Trip \"${newTrip.tripName}\" Created Successfully!",
                Snackbar.LENGTH_SHORT
            ).show()

            // SafeArgs navigation if user checked "Include Vehicle / Bike Rental"
            if (cbAddRental.isChecked) {
                val bundle = Bundle().apply {
                    putString("rentalId", "rental_scooter_ella")
                }
                findNavController().navigate(
                    R.id.action_tripPlannerFragment_to_rentalDetailsFragment,
                    bundle
                )
            }
        }

        dialog.show()
    }

    private fun showTripItineraryDialog(trip: Trip) {
        val items = mutableListOf<String>()

        items.add("📌 Destination & Dates: ${trip.tripName} (${trip.date})")

        // Display Destinations
        if (trip.places.isNotEmpty()) {
            trip.places.forEachIndexed { index, place ->
                items.add("📍 Stop ${index + 1}: ${place.name} (${place.location})")
            }
        } else {
            items.add("📍 No destination checkpoints added yet.")
        }

        // Display Booked Rentals
        if (trip.bookedRentals.isNotEmpty()) {
            trip.bookedRentals.forEach { rental ->
                items.add("🛵 Booked Vehicle: ${rental.name} (${rental.vendorName})")
            }
        }

        // Display Booked Activities
        if (trip.bookedActivities.isNotEmpty()) {
            trip.bookedActivities.forEach { act ->
                items.add("🎟 Booked Activity: ${act.title}")
            }
        }

        // Action Options
        items.add("➔ Explore & Book Vehicles / Bikes")
        items.add("➔ Explore & Book Activities / Tours")

        AlertDialog.Builder(requireContext())
            .setTitle(trip.tripName)
            .setItems(items.toTypedArray()) { _, which ->
                val selectedOption = items[which]
                when {
                    selectedOption.contains("Book Vehicles") -> {
                        val bundle = Bundle().apply {
                            putString("rentalId", "rental_scooter_ella")
                        }
                        findNavController().navigate(
                            R.id.action_tripPlannerFragment_to_rentalDetailsFragment,
                            bundle
                        )
                    }
                    selectedOption.contains("Book Activities") -> {
                        val bundle = Bundle().apply {
                            putString("activityId", "act_sigiriya_hike")
                        }
                        findNavController().navigate(
                            R.id.action_tripPlannerFragment_to_activityBookingFragment,
                            bundle
                        )
                    }
                }
            }
            .setPositiveButton("Close", null)
            .show()
    }

    private fun confirmDeleteTrip(trip: Trip) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete \"${trip.tripName}\"?")
            .setPositiveButton("Delete") { _, _ ->
                TripManager.deleteTrip(requireContext(), trip.id)
                Snackbar.make(
                    requireView(),
                    "Trip Deleted Successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
                loadTrips()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
