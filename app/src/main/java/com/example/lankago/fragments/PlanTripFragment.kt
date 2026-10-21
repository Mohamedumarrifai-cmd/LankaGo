package com.example.lankago.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.Trip
import com.example.lankago.utils.TripManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Fragment handling creation of customized travel itineraries.
 * Features:
 * - Robust input validation (Destination & Dates)
 * - Safe asynchronous saving to TripManager & Firebase Realtime Database
 * - Non-abrupt dismissal with user-facing Snackbar feedback
 * - Conditional smart navigation: If "Need a Vehicle" is checked, routes to RentalShopsListFragment.
 */
open class PlanTripFragment : Fragment() {

    private var travelerCount = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plan_trip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
    }

    private fun initViews(view: View) {
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back_plan_trip)
        val tilDestination = view.findViewById<TextInputLayout>(R.id.til_plan_destination)
        val etDestination = view.findViewById<TextInputEditText>(R.id.et_plan_destination)
        val tilDates = view.findViewById<TextInputLayout>(R.id.til_plan_dates)
        val etDates = view.findViewById<TextInputEditText>(R.id.et_plan_dates)
        val btnMinus = view.findViewById<ImageButton>(R.id.btn_plan_minus_traveler)
        val btnPlus = view.findViewById<ImageButton>(R.id.btn_plan_plus_traveler)
        val tvTravelerCount = view.findViewById<TextView>(R.id.tv_plan_traveler_count)
        val switchRental = view.findViewById<MaterialSwitch>(R.id.switch_need_rental)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btn_confirm_plan_trip)

        // Back button navigation
        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Default Date Setup & DatePickerDialog
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        val startDateFormat = SimpleDateFormat("dd MMM", Locale.US)
        val startDateStr = startDateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 5)
        val endDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val endDateStr = endDateFormat.format(calendar.time)
        etDates?.setText("$startDateStr – $endDateStr")

        etDates?.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val chosen = Calendar.getInstance()
                    chosen.set(year, month, dayOfMonth)
                    val s = SimpleDateFormat("dd MMM", Locale.US).format(chosen.time)
                    chosen.add(Calendar.DAY_OF_YEAR, 5)
                    val e = SimpleDateFormat("dd MMM yyyy", Locale.US).format(chosen.time)
                    etDates.setText("$s – $e")
                    tilDates?.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        // Travelers Stepper
        btnMinus?.setOnClickListener {
            if (travelerCount > 1) {
                travelerCount--
                tvTravelerCount?.text = travelerCount.toString()
            }
        }

        btnPlus?.setOnClickListener {
            if (travelerCount < 12) {
                travelerCount++
                tvTravelerCount?.text = travelerCount.toString()
            }
        }

        // -------------------------------------------------------------
        // ROBUST CREATE TRIP CONFIRMATION & NAVIGATION LOGIC
        // -------------------------------------------------------------
        btnConfirm?.setOnClickListener {
            val destination = etDestination?.text?.toString()?.trim().orEmpty()
            val dates = etDates?.text?.toString()?.trim().orEmpty()

            // 1. Rigorous Input Validation
            if (destination.isBlank()) {
                tilDestination?.error = "Please specify your destination (e.g., Ella, Mirissa, Sigiriya)"
                etDestination?.requestFocus()
                return@setOnClickListener
            }
            tilDestination?.error = null

            if (dates.isBlank()) {
                tilDates?.error = "Please select travel dates"
                etDates?.requestFocus()
                return@setOnClickListener
            }
            tilDates?.error = null

            // 2. Prevent duplicate clicks during async operation
            btnConfirm.isEnabled = false
            btnConfirm.text = "Creating Trip..."

            // 3. Save Trip Asynchronously via Coroutine
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val tripId = "trip_${System.currentTimeMillis()}"
                    val tripTitle = "$destination Adventure ($travelerCount Travelers)"

                    val newTrip = Trip(
                        id = tripId,
                        tripName = tripTitle,
                        date = dates,
                        travelerType = "Custom",
                        idImageUri = null,
                        places = mutableListOf(),
                        bookedRentals = mutableListOf(),
                        bookedActivities = mutableListOf()
                    )

                    // 3a. Save locally via TripManager
                    TripManager.saveTrip(requireContext(), newTrip)

                    // 3b. Sync to Firebase Realtime Database
                    try {
                        val auth = FirebaseAuth.getInstance()
                        val uid = auth.currentUser?.uid ?: "guest_traveler"
                        val rtdb = FirebaseDatabase.getInstance().reference
                        rtdb.child("trips").child(uid).child(newTrip.id).setValue(newTrip).await()
                    } catch (e: Exception) {
                        Log.w("CreateTrip", "Firebase RTDB sync non-fatal warning: ${e.message}")
                    }

                    // 4. Show Success Feedback Snackbar
                    Snackbar.make(
                        view,
                        "Trip \"$tripTitle\" created successfully!",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Safe delay allowing user to perceive the success feedback
                    delay(800)

                    if (!isAdded) return@launch

                    // 5. CONDITIONAL ROUTING:
                    // If "Need a Vehicle" is checked -> navigate to RentalShopsListFragment
                    // If unchecked -> navigate to TripPlannerFragment to view itinerary
                    val needVehicle = switchRental?.isChecked == true

                    if (needVehicle) {
                        val bundle = bundleOf(
                            "destinationLocation" to destination,
                            "tripId" to newTrip.id
                        )
                        findNavController().navigate(
                            R.id.rentalShopsListFragment,
                            bundle
                        )
                    } else {
                        findNavController().navigate(
                            R.id.tripPlannerFragment
                        )
                    }

                } catch (e: Exception) {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Next / Create Trip"
                    Snackbar.make(
                        view,
                        "Error creating trip: ${e.localizedMessage ?: "Please try again"}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
