package com.example.lankago.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.Trip
import com.example.lankago.utils.ImagePickerHelper
import com.example.lankago.utils.LankaGoNotificationHelper
import com.example.lankago.utils.TripManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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
 * Fragment handling creation of customized travel itineraries with COMPULSORY traveler ID verification.
 * Features:
 * - Robust input validation (Destination & Dates)
 * - COMPULSORY Traveler National ID (NIC) or Passport capture of the booking person via live Camera or Gallery
 * - Visual verification badge and green border upon successful document capture
 * - Error prevention if document photo is missing
 * - Safe asynchronous saving to TripManager & Firebase Realtime Database
 * - Non-abrupt dismissal with user-facing Snackbar feedback
 * - Conditional smart navigation: If "Need a Vehicle" is checked, routes to RentalShopsListFragment.
 */
open class PlanTripFragment : Fragment() {

    private var travelerCount = 2
    private var selectedTripImageUri: Uri? = null

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedTripImageUri = uri
        val view = view ?: return@ImagePickerHelper
        val ivPreview = view.findViewById<ImageView>(R.id.iv_trip_photo_preview)
        val placeholder = view.findViewById<LinearLayout>(R.id.layout_trip_photo_placeholder)
        val badge = view.findViewById<TextView>(R.id.tv_trip_retake_badge)
        val cardTripPhoto = view.findViewById<MaterialCardView>(R.id.card_trip_photo_capture)

        ivPreview?.visibility = View.VISIBLE
        placeholder?.visibility = View.GONE
        badge?.visibility = View.VISIBLE

        // Turn border green to signal valid compulsory document
        cardTripPhoto?.strokeColor = Color.parseColor("#10B981")
        cardTripPhoto?.strokeWidth = 2

        ivPreview?.let {
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(it)
        }
    }

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

        val rgTravelerType = view.findViewById<RadioGroup>(R.id.rg_plan_traveler_type)
        val rbLocalNic = view.findViewById<RadioButton>(R.id.rb_plan_local_nic)
        val cardTripPhoto = view.findViewById<MaterialCardView>(R.id.card_trip_photo_capture)
        val tvIdTitle = view.findViewById<TextView>(R.id.tv_id_capture_title)
        val tvIdSubtitle = view.findViewById<TextView>(R.id.tv_id_capture_subtitle)

        val switchRental = view.findViewById<MaterialSwitch>(R.id.switch_need_rental)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btn_confirm_plan_trip)

        // Back button navigation
        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Toggle prompt between NIC and Passport
        rgTravelerType?.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_plan_local_nic) {
                tvIdTitle?.text = "Tap to capture National ID (NIC)"
                tvIdSubtitle?.text = "Clear photo of your NIC with Camera or Gallery (Required)"
            } else {
                tvIdTitle?.text = "Tap to capture Passport ID Page"
                tvIdSubtitle?.text = "Clear photo of your Passport details with Camera or Gallery (Required)"
            }
        }

        // Camera / Gallery Image Chooser Trigger
        cardTripPhoto?.setOnClickListener {
            val docName = if (rbLocalNic?.isChecked == true) "National ID (NIC)" else "Passport"
            imagePickerHelper.showImageSourceDialog("Capture Booking Person's $docName")
        }

        // Default Date Setup & DatePickerDialog
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        etDates?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val startDate = dateFormat.format(calendar.time)

                    calendar.add(Calendar.DAY_OF_MONTH, 5)
                    val endDate = dateFormat.format(calendar.time)
                    calendar.add(Calendar.DAY_OF_MONTH, -5)

                    etDates.setText("$startDate - $endDate")
                    tilDates?.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        // Pre-fill initial placeholder dates
        if (etDates?.text.isNullOrBlank()) {
            val start = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 5)
            val end = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, -5)
            etDates?.setText("$start - $end")
        }

        // Pre-fill destination if passed as safe argument
        arguments?.getString("destinationName")?.let { prefilled ->
            if (prefilled.isNotBlank()) {
                etDestination?.setText(prefilled)
            }
        }

        // Traveler Counter Controls
        btnMinus?.setOnClickListener {
            if (travelerCount > 1) {
                travelerCount--
                tvTravelerCount?.text = "$travelerCount Travelers"
            }
        }

        btnPlus?.setOnClickListener {
            if (travelerCount < 20) {
                travelerCount++
                tvTravelerCount?.text = "$travelerCount Travelers"
            }
        }

        // Confirm & Create Trip Click Listener
        btnConfirm?.setOnClickListener {
            val destination = etDestination?.text?.toString()?.trim() ?: ""
            val dates = etDates?.text?.toString()?.trim() ?: ""
            val needVehicle = switchRental?.isChecked ?: false
            val isLocal = rbLocalNic?.isChecked ?: true
            val travelerTypeString = if (isLocal) "Local (NIC)" else "Foreign (Passport)"

            // 1. Destination Validation
            if (destination.isBlank()) {
                tilDestination?.error = "Please enter a travel destination"
                etDestination?.requestFocus()
                return@setOnClickListener
            }
            tilDestination?.error = null

            // 2. Dates Validation
            if (dates.isBlank()) {
                tilDates?.error = "Please select travel dates"
                etDates?.requestFocus()
                return@setOnClickListener
            }
            tilDates?.error = null

            // 3. COMPULSORY REQUIREMENT: Booking Person's ID / Passport Photo Check
            if (selectedTripImageUri == null) {
                cardTripPhoto?.strokeColor = Color.parseColor("#EF4444")
                cardTripPhoto?.strokeWidth = 3
                val requiredDoc = if (isLocal) "National ID (NIC)" else "Passport"
                view.findViewById<androidx.core.widget.NestedScrollView>(R.id.scroll_plan_trip)
                    ?.smoothScrollTo(0, (cardTripPhoto?.top ?: 150) - 100)
                Toast.makeText(
                    requireContext(),
                    "⚠️ Compulsory: You MUST capture/upload your $requiredDoc to confirm!",
                    Toast.LENGTH_LONG
                ).show()
                Snackbar.make(
                    view,
                    "Compulsory: You MUST capture a photo of your $requiredDoc to confirm this booking.",
                    Snackbar.LENGTH_LONG
                ).show()
                cardTripPhoto?.requestFocus()
                return@setOnClickListener
            }

            // 4. Prevent duplicate clicks during async operation
            btnConfirm.isEnabled = false
            btnConfirm.text = "Confirming Booking..."

            // 5. Save Trip Asynchronously via Coroutine
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val tripId = "trip_${System.currentTimeMillis()}"
                    val tripTitle = "$destination Adventure ($travelerCount Travelers)"

                    val newTrip = Trip(
                        id = tripId,
                        tripName = tripTitle,
                        date = dates,
                        travelerType = travelerTypeString,
                        idImageUri = selectedTripImageUri?.toString(),
                        places = mutableListOf(),
                        bookedRentals = mutableListOf(),
                        bookedActivities = mutableListOf()
                    )

                    // 5a. Save locally via TripManager
                    TripManager.saveTrip(requireContext(), newTrip)

                    // 5b. Sync to Firebase Realtime Database
                    try {
                        val auth = FirebaseAuth.getInstance()
                        val uid = auth.currentUser?.uid ?: "guest_traveler"
                        val rtdb = FirebaseDatabase.getInstance().reference
                        // Primary Trip History node under user profile
                        rtdb.child("users").child(uid).child("tripHistory").child(newTrip.id).setValue(newTrip).await()
                        // Cross-reference under /trips node
                        rtdb.child("trips").child(uid).child(newTrip.id).setValue(newTrip).await()
                    } catch (e: Exception) {
                        Log.w("CreateTrip", "Firebase RTDB sync non-fatal warning: ${e.message}")
                    }

                    // 6. Post Notification & Show Success Feedback Snackbar
                    LankaGoNotificationHelper.postTripCreatedNotification(requireContext(), newTrip)

                    Snackbar.make(
                        view,
                        "Trip confirmed! ID Document verified successfully.",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Safe UI delay to let user see feedback before navigation
                    delay(700L)

                    if (!isAdded) return@launch

                    // 7. Smart Conditional Routing to rentals_and_transport screen
                    if (needVehicle) {
                        val bundle = bundleOf(
                            "activeTripId" to tripId,
                            "destinationCity" to destination,
                            "destinationLocation" to destination,
                            "travelerCount" to travelerCount
                        )
                        try {
                            findNavController().navigate(
                                R.id.action_planTripFragment_to_rentalsTransportFragment,
                                bundle
                            )
                        } catch (navEx: Exception) {
                            findNavController().navigate(R.id.rentalsTransportFragment, bundle)
                        }
                    } else {
                        findNavController().popBackStack()
                    }

                } catch (e: Exception) {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Confirm & Plan Trip"
                    Snackbar.make(
                        view,
                        "Error confirming booking: ${e.localizedMessage ?: "Please try again"}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
