package com.example.lankago.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.Trip
import com.example.lankago.utils.TripManager
import com.example.lankago.utils.safeNavigate
import com.example.lankago.utils.setOnSafeClickListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Production-ready Fragment for planning and creating trips in LankaGo.
 * Responsibilities:
 * 1. Collects and validates trip destination, dates, and traveler count.
 * 2. Persists planned trips locally in [TripManager] for offline resiliency.
 * 3. Pushes trip record to Firebase Realtime Database node: `/users/{uid}/tripHistory/{tripId}`.
 * 4. Navigates smoothly to Trip History or Rental selection without UI freezes or navigation crashes.
 */
class CreateTripFragment : Fragment() {

    private lateinit var tilDestination: TextInputLayout
    private lateinit var etDestination: EditText
    private lateinit var tilDates: TextInputLayout
    private lateinit var etDates: EditText
    private lateinit var etTravelers: EditText
    private lateinit var cbAddRental: CheckBox
    private lateinit var btnCancel: AppCompatButton
    private lateinit var btnConfirm: AppCompatButton

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_trip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupDatePickers()
        setupListeners(view)
    }

    private fun initViews(view: View) {
        tilDestination = view.findViewById(R.id.til_destination)
        etDestination = view.findViewById(R.id.et_destination)
        tilDates = view.findViewById(R.id.til_trip_dates)
        etDates = view.findViewById(R.id.et_trip_dates)
        etTravelers = view.findViewById(R.id.et_travelers_count)
        cbAddRental = view.findViewById(R.id.cb_add_rental)
        btnCancel = view.findViewById(R.id.btn_cancel_trip)
        btnConfirm = view.findViewById(R.id.btn_confirm_create_trip)
    }

    private fun setupDatePickers() {
        etDates.isFocusable = false
        etDates.isClickable = true
        etDates.setOnClickListener {
            val startYear = calendar.get(Calendar.YEAR)
            val startMonth = calendar.get(Calendar.MONTH)
            val startDay = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val startCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val startDateStr = SimpleDateFormat("MMM dd", Locale.US).format(startCal.time)

                // Prompt for Return/End Date (default + 4 days)
                val endCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth + 4)
                }
                DatePickerDialog(requireContext(), { _, endY, endM, endD ->
                    endCal.set(endY, endM, endD)
                    val endDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(endCal.time)
                    etDates.setText("$startDateStr - $endDateStr")
                    tilDates.error = null
                }, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH)).apply {
                    datePicker.minDate = startCal.timeInMillis
                    setTitle("Select End Date")
                }.show()

            }, startYear, startMonth, startDay).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                setTitle("Select Departure Date")
            }.show()
        }
    }

    private fun setupListeners(view: View) {
        btnCancel.setOnSafeClickListener {
            findNavController().navigateUp()
        }

        btnConfirm.setOnSafeClickListener {
            val destination = etDestination.text?.toString()?.trim() ?: ""
            val dates = etDates.text?.toString()?.trim() ?: ""
            val travelersStr = etTravelers.text?.toString()?.trim() ?: "2"
            val travelersCount = travelersStr.toIntOrNull() ?: 2

            // 1. Validation
            if (destination.isBlank()) {
                tilDestination.error = "Please enter a destination (e.g. Ella, Sigiriya, Mirissa)"
                etDestination.requestFocus()
                return@setOnSafeClickListener
            }
            tilDestination.error = null

            if (dates.isBlank()) {
                tilDates.error = "Please select your travel dates"
                etDates.requestFocus()
                return@setOnSafeClickListener
            }
            tilDates.error = null

            val tripId = "trip_${System.currentTimeMillis()}"
            val tripName = "$destination Adventure ($travelersCount Travelers)"
            val newTrip = Trip(
                id = tripId,
                tripName = tripName,
                date = dates,
                travelerType = "Local",
                destination = destination,
                timestamp = System.currentTimeMillis(),
                status = "Upcoming",
                places = mutableListOf(),
                bookedRentals = mutableListOf(),
                bookedActivities = mutableListOf()
            )

            // Prevent duplicate clicks
            btnConfirm.isEnabled = false
            btnConfirm.text = "Saving Trip..."

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // 2. Persist locally first for zero-latency offline availability
                    TripManager.saveTrip(requireContext(), newTrip)

                    // 3. Push to Firebase Realtime Database: /users/{uid}/tripHistory/{tripId}
                    val authUser = FirebaseAuth.getInstance().currentUser
                    val uid = authUser?.uid ?: "guest_traveler"
                    val databaseRef = FirebaseDatabase.getInstance().reference

                    withContext(Dispatchers.IO) {
                        try {
                            // Primary requirement: /users/{uid}/tripHistory/{tripId}
                            databaseRef.child("users")
                                .child(uid)
                                .child("tripHistory")
                                .child(tripId)
                                .setValue(newTrip)
                                .await()

                            // Also mirror under global /trips node for cross-component queries
                            databaseRef.child("trips")
                                .child(uid)
                                .child(tripId)
                                .setValue(newTrip)
                                .await()
                        } catch (firebaseEx: Exception) {
                            Log.w("CreateTripFragment", "Firebase RTDB write error: ${firebaseEx.message}")
                        }
                    }

                    if (!isAdded) return@launch

                    Snackbar.make(
                        view,
                        "✓ Trip to $destination created & saved to your Trip History!",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    delay(600L)
                    if (!isAdded) return@launch

                    // 4. Conditional Navigation
                    if (cbAddRental.isChecked) {
                        val bundle = Bundle().apply {
                            putString("activeTripId", tripId)
                            putString("destinationLocation", destination)
                            putString("rentalId", "rental_scooter_ella")
                        }
                        findNavController().safeNavigate(
                            R.id.action_tripPlannerFragment_to_rentalDetailsFragment,
                            bundle
                        )
                    } else {
                        // Navigate to Trip History or pop back
                        try {
                            findNavController().safeNavigate(R.id.tripHistoryFragment)
                        } catch (_: Exception) {
                            findNavController().navigateUp()
                        }
                    }

                } catch (e: Exception) {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Create Trip"
                    Snackbar.make(
                        view,
                        "Error saving trip: ${e.localizedMessage ?: "Please try again."}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
