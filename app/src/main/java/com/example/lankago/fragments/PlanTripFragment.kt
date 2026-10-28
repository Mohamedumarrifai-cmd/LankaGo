package com.example.lankago.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.Booking
import com.example.lankago.data.BookingStatus
import com.example.lankago.data.BookingType
import com.example.lankago.data.RentalVehicle
import com.example.lankago.data.Trip
import com.example.lankago.repository.AdminRepository
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


//  Fragment handling creation of customized travel itineraries with COMPULSORY traveler ID verification
//  and integrated Vehicle & Bike Rental selection with instant receipt and notifications.

open class PlanTripFragment : Fragment() {

    private var travelerCount = 2       //default travellers la gaana
    private var selectedTripImageUri: Uri? = null
    private var selectedRental: RentalVehicle? = null

    private lateinit var tilDestination: TextInputLayout
    private lateinit var etDestination: TextInputEditText
    private lateinit var tilDates: TextInputLayout
    private lateinit var etDates: TextInputEditText
    private lateinit var switchRental: MaterialSwitch
    private lateinit var layoutRentalPreview: View
    private lateinit var ivSelectedVehicleThumb: ImageView
    private lateinit var tvSelectedVehicleName: TextView
    private lateinit var tvSelectedVehicleSpecs: TextView
    private lateinit var btnChangeVehicle: View
    private lateinit var btnChooseRental: MaterialButton
    private lateinit var btnConfirm: MaterialButton

    private val imagePickerHelper = ImagePickerHelper(this) { uri ->
        selectedTripImageUri = uri
        TripManager.draftIdImageUri = uri
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

    override fun onResume() {
        super.onResume()
        // 1. Pick up vehicle if traveler selected one from rentals screen
        val chosen = TripManager.selectedRentalForPlanning
        if (chosen != null) {
            selectedRental = chosen
            updateSelectedVehicleUI()
        }

        // 2. Ensure ID/Passport photo is restored and displayed
        val idUri = TripManager.draftIdImageUri ?: selectedTripImageUri
        if (idUri != null) {
            selectedTripImageUri = idUri
            val v = view ?: return
            val ivPreview = v.findViewById<ImageView>(R.id.iv_trip_photo_preview)
            val placeholder = v.findViewById<LinearLayout>(R.id.layout_trip_photo_placeholder)
            val badge = v.findViewById<TextView>(R.id.tv_trip_retake_badge)
            val cardTripPhoto = v.findViewById<MaterialCardView>(R.id.card_trip_photo_capture)

            ivPreview?.visibility = View.VISIBLE
            placeholder?.visibility = View.GONE
            badge?.visibility = View.VISIBLE
            cardTripPhoto?.strokeColor = Color.parseColor("#10B981")
            cardTripPhoto?.strokeWidth = 2

            ivPreview?.let {
                Glide.with(this)
                    .load(idUri)
                    .centerCrop()
                    .into(it)
            }
        }
    }

    private fun initViews(view: View) {
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back_plan_trip)
        tilDestination = view.findViewById(R.id.til_plan_destination)
        etDestination = view.findViewById(R.id.et_plan_destination)
        tilDates = view.findViewById(R.id.til_plan_dates)
        etDates = view.findViewById(R.id.et_plan_dates)
        val btnMinus = view.findViewById<ImageButton>(R.id.btn_plan_minus_traveler)
        val btnPlus = view.findViewById<ImageButton>(R.id.btn_plan_plus_traveler)
        val tvTravelerCount = view.findViewById<TextView>(R.id.tv_plan_traveler_count)

        val rgTravelerType = view.findViewById<RadioGroup>(R.id.rg_plan_traveler_type)
        val rbLocalNic = view.findViewById<RadioButton>(R.id.rb_plan_local_nic)
        val cardTripPhoto = view.findViewById<MaterialCardView>(R.id.card_trip_photo_capture)
        val tvIdTitle = view.findViewById<TextView>(R.id.tv_id_capture_title)
        val tvIdSubtitle = view.findViewById<TextView>(R.id.tv_id_capture_subtitle)

        switchRental = view.findViewById(R.id.switch_need_rental)
        layoutRentalPreview = view.findViewById(R.id.layout_selected_vehicle_preview)
        ivSelectedVehicleThumb = view.findViewById(R.id.iv_selected_vehicle_thumb)
        tvSelectedVehicleName = view.findViewById(R.id.tv_selected_vehicle_name)
        tvSelectedVehicleSpecs = view.findViewById(R.id.tv_selected_vehicle_specs)
        btnChangeVehicle = view.findViewById(R.id.btn_change_selected_vehicle)
        btnChooseRental = view.findViewById(R.id.btn_choose_rental_vehicle)
        btnConfirm = view.findViewById(R.id.btn_confirm_plan_trip)

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

        etDates.setOnClickListener {
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
                    tilDates.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        // Pre-fill initial placeholder dates or restore from draft
        if (TripManager.draftDates.isNotBlank()) {
            etDates.setText(TripManager.draftDates)
        } else if (etDates.text.isNullOrBlank()) {
            val start = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 5)
            val end = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, -5)
            etDates.setText("$start - $end")
        }

        // Restore destination from draft or pre-fill if passed as safe argument
        if (TripManager.draftDestination.isNotBlank()) {
            etDestination.setText(TripManager.draftDestination)
        } else {
            arguments?.getString("destinationName")?.let { prefilled ->
                if (prefilled.isNotBlank()) {
                    etDestination.setText(prefilled)
                }
            }
        }

        // Restore traveler count and type from draft
        if (TripManager.draftTravelers > 1) {
            travelerCount = TripManager.draftTravelers
            tvTravelerCount?.text = "$travelerCount Travelers"
        }
        if (TripManager.draftIsLocalNic) {
            rbLocalNic?.isChecked = true
        } else {
            view.findViewById<RadioButton>(R.id.rb_plan_foreign_passport)?.isChecked = true
        }

        // Restore Draft ID Photo if user already took/uploaded one
        val currentIdUri = TripManager.draftIdImageUri ?: selectedTripImageUri
        if (currentIdUri != null) {
            selectedTripImageUri = currentIdUri
            val ivPreview = view.findViewById<ImageView>(R.id.iv_trip_photo_preview)
            val placeholder = view.findViewById<LinearLayout>(R.id.layout_trip_photo_placeholder)
            val badge = view.findViewById<TextView>(R.id.tv_trip_retake_badge)

            ivPreview?.visibility = View.VISIBLE
            placeholder?.visibility = View.GONE
            badge?.visibility = View.VISIBLE
            cardTripPhoto?.strokeColor = Color.parseColor("#10B981")
            cardTripPhoto?.strokeWidth = 2

            ivPreview?.let {
                Glide.with(this)
                    .load(currentIdUri)
                    .centerCrop()
                    .into(it)
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

        // Vehicle Rental Switch Toggle: When enabled, immediately open rentals screen to choose
        switchRental.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (selectedRental == null) {
                    openRentalSelectionScreen()
                } else {
                    updateSelectedVehicleUI()
                }
            } else {
                selectedRental = null
                TripManager.selectedRentalForPlanning = null
                layoutRentalPreview.visibility = View.GONE
                btnChooseRental.visibility = View.GONE
            }
        }

        btnChooseRental.setOnClickListener {
            openRentalSelectionScreen()
        }

        btnChangeVehicle.setOnClickListener {
            openRentalSelectionScreen()
        }

        // Confirm & Create Trip Click Listener
        btnConfirm.setOnClickListener {
            val destination = etDestination.text?.toString()?.trim() ?: ""
            val dates = etDates.text?.toString()?.trim() ?: ""
            val isLocal = rbLocalNic?.isChecked ?: true
            val travelerTypeString = if (isLocal) "Local (NIC)" else "Foreign (Passport)"

            // 1. Destination Validation
            if (destination.isBlank()) {
                tilDestination.error = "Please enter a travel destination"
                etDestination.requestFocus()
                return@setOnClickListener
            }
            tilDestination.error = null

            // 2. Dates Validation
            if (dates.isBlank()) {
                tilDates.error = "Please select travel dates"
                etDates.requestFocus()
                return@setOnClickListener
            }
            tilDates.error = null

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
                    val rentalList = if (selectedRental != null) mutableListOf(selectedRental!!) else mutableListOf()
                            //create new trip object
                    val newTrip = Trip(
                        id = tripId,
                        tripName = tripTitle,
                        date = dates,
                        travelerType = travelerTypeString,
                        idImageUri = selectedTripImageUri?.toString(),
                        places = mutableListOf(),
                        bookedRentals = rentalList,
                        bookedActivities = mutableListOf(),
                        destination = destination
                    )

                    // 5a. Save locally via TripManager
                    TripManager.saveTrip(requireContext(), newTrip)

                    // 5b. Sync to Firebase Realtime Database
                    val auth = FirebaseAuth.getInstance()
                    val uid = auth.currentUser?.uid ?: "guest_traveler"
                    val userEmail = auth.currentUser?.email ?: "traveler@lankago.lk"
                    val userName = auth.currentUser?.displayName ?: "Traveler"
                    val rtdb = FirebaseDatabase.getInstance().reference

                    try {
                        // Primary Trip History node under user profile
                        rtdb.child("users").child(uid).child("tripHistory").child(newTrip.id).setValue(newTrip).await()
                        // Cross-reference under /trips node
                        rtdb.child("trips").child(uid).child(newTrip.id).setValue(newTrip).await()

                        // If rental vehicle attached, register confirmed Booking in /bookings node
                        selectedRental?.let { rental ->
                            val bookingRef = "LKG-RENT-${System.currentTimeMillis().toString().takeLast(6)}"
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            val totalRentalPrice = rental.dailyRateLkr + rental.depositLkr
                            val booking = Booking(
                                id = bookingRef,
                                userId = uid,
                                tripId = newTrip.id,
                                userName = userName,
                                userEmail = userEmail,
                                userPhone = "",
                                itemName = "${rental.name} (${rental.vendorName})",
                                itemCategory = rental.type,
                                bookingType = BookingType.RENTAL,
                                bookingDate = todayStr,
                                bookingTime = "09:00 AM",
                                totalPriceLkr = totalRentalPrice,
                                status = BookingStatus.CONFIRMED,
                                specialNotes = "Trip Booking to $destination. Pickup: ${rental.location}"
                            )
                            AdminRepository().addBooking(booking)

                            // Post rental payment / booking notification
                            LankaGoNotificationHelper.postPaymentSuccessNotification(
                                context = requireContext(),
                                bookingRef = bookingRef,
                                vehicleName = rental.name,
                                amountLkr = totalRentalPrice
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("PlanTripFragment", "Firebase RTDB sync non-fatal warning: ${e.message}")
                    }

                    // 6. Post Trip Created Notification
                    LankaGoNotificationHelper.postTripCreatedNotification(requireContext(), newTrip)

                    // Clear temporary selection and draft
                    TripManager.clearDraft()

                    if (!isAdded) return@launch

                    // 7. Show Generated Rich Confirmation Receipt Dialog
                    val bookingRef = "LKG-CONF-${newTrip.id.takeLast(6)}"
                    showBookingReceiptDialog(
                        bookingRef = bookingRef,
                        trip = newTrip,
                        rental = selectedRental
                    )

                } catch (e: Exception) {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Confirm Booking"
                    Snackbar.make(
                        view,
                        "Error confirming booking: ${e.localizedMessage ?: "Please try again"}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun openRentalSelectionScreen() {
        val destination = etDestination.text?.toString()?.trim() ?: "Sri Lanka"
        // Save current draft before opening vehicle selection so nothing is lost!
        TripManager.draftDestination = destination
        TripManager.draftDates = etDates.text?.toString()?.trim() ?: ""
        TripManager.draftTravelers = travelerCount
        val rbLocalNic = view?.findViewById<RadioButton>(R.id.rb_plan_local_nic)
        TripManager.draftIsLocalNic = rbLocalNic?.isChecked ?: true
        TripManager.draftIdImageUri = selectedTripImageUri

        val bundle = bundleOf(
            "destinationCity" to destination,
            "destinationLocation" to destination,
            "isSelectMode" to true,
            "mode" to "select_rental"
        )
        try {
            findNavController().navigate(
                R.id.action_planTripFragment_to_rentalsTransportFragment,
                bundle
            )
        } catch (_: Exception) {
            try {
                findNavController().navigate(R.id.rentalsTransportFragment, bundle)
            } catch (_: Exception) {
                findNavController().navigate(R.id.rentalListFragment, bundle)
            }
        }
    }

    private fun updateSelectedVehicleUI() {
        val rental = selectedRental ?: return
        switchRental.isChecked = true
        layoutRentalPreview.visibility = View.VISIBLE
        btnChooseRental.visibility = View.GONE

        tvSelectedVehicleName.text = rental.name
        val format = NumberFormat.getNumberInstance(Locale.US)
        tvSelectedVehicleSpecs.text = "${rental.type} • LKR ${format.format(rental.dailyRateLkr.toInt())}/day • ${rental.location}"

        val localDrawable = when {
            rental.name.contains("Tuk", ignoreCase = true) || rental.type.contains("Tuk", ignoreCase = true) -> R.drawable.tourist_tuktuk
            rental.name.contains("KDH", ignoreCase = true) || rental.name.contains("Van", ignoreCase = true) || rental.type.contains("Van", ignoreCase = true) -> R.drawable.kdh
            rental.name.contains("Montero", ignoreCase = true) -> R.drawable.montero
            rental.name.contains("X-Trail", ignoreCase = true) || rental.type.contains("SUV", ignoreCase = true) || rental.type.contains("4x4", ignoreCase = true) -> R.drawable.xtrail
            rental.name.contains("Grace", ignoreCase = true) -> R.drawable.grace
            rental.name.contains("Prius", ignoreCase = true) -> R.drawable.prius
            rental.name.contains("Aqua", ignoreCase = true) -> R.drawable.aqua
            rental.name.contains("Alto", ignoreCase = true) -> R.drawable.alto
            rental.name.contains("Wagon", ignoreCase = true) -> R.drawable.wagonr
            rental.name.contains("NS200", ignoreCase = true) || rental.name.contains("Pulsar", ignoreCase = true) -> R.drawable.ns200
            rental.name.contains("FZ", ignoreCase = true) || rental.name.contains("Yamaha", ignoreCase = true) -> R.drawable.fz
            rental.name.contains("Dio", ignoreCase = true) -> R.drawable.dio
            rental.name.contains("NMAX", ignoreCase = true) -> R.drawable.nmax
            rental.name.contains("PCX", ignoreCase = true) -> R.drawable.pcx
            rental.name.contains("Enfield", ignoreCase = true) || rental.name.contains("Himalayan", ignoreCase = true) -> R.drawable.royalenflied
            rental.type.contains("Bike", ignoreCase = true) -> R.drawable.fz
            else -> R.drawable.aqua
        }

        val heroUrl = rental.images.firstOrNull()
        if (!heroUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(heroUrl)
                .placeholder(localDrawable)
                .error(localDrawable)
                .centerCrop()
                .into(ivSelectedVehicleThumb)
        } else {
            ivSelectedVehicleThumb.setImageResource(localDrawable)
        }
    }

    private fun showBookingReceiptDialog(
        bookingRef: String,
        trip: Trip,
        rental: RentalVehicle?
    ) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val rootLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_bottom_sheet)
            setPadding(40, 40, 40, 40)
        }

        // Green success badge
        val tvBadge = TextView(requireContext()).apply {
            text = "✓ BOOKING CONFIRMED & RESERVED"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#10B981"))
            setPadding(0, 0, 0, 8)
        }

        val tvTitle = TextView(requireContext()).apply {
            text = "Booking & Payment Receipt"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1A2B4C"))
        }

        val tvSubtext = TextView(requireContext()).apply {
            text = "Your journey has been confirmed with verified ID. Saved to your Trip History."
            textSize = 13f
            setTextColor(Color.parseColor("#64748B"))
            setPadding(0, 6, 0, 20)
        }

        // Receipt Details Box
        val boxReceipt = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F8FAFC"))
            setPadding(24, 24, 24, 24)
        }

        fun addReceiptLine(label: String, value: String) {
            boxReceipt.addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 6, 0, 6)
                addView(TextView(requireContext()).apply {
                    text = label
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setTextColor(Color.parseColor("#64748B"))
                    textSize = 13f
                })
                addView(TextView(requireContext()).apply {
                    text = value
                    setTextColor(Color.parseColor("#1A2B4C"))
                    textSize = 13f
                    setTypeface(null, Typeface.BOLD)
                })
            })
        }

        val dateStr = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.US).format(Date())
        addReceiptLine("Receipt No.", bookingRef)
        addReceiptLine("Date & Time", dateStr)
        addReceiptLine("Trip Itinerary", trip.tripName)
        addReceiptLine("Scheduled Dates", trip.date)
        addReceiptLine("Traveler Verification", "${trip.travelerType} (Verified)")

        if (rental != null) {
            val format = NumberFormat.getNumberInstance(Locale.US)
            val totalRentalLkr = rental.dailyRateLkr + rental.depositLkr
            addReceiptLine("Vehicle Reserved", rental.name)
            addReceiptLine("Rental Provider", rental.vendorName)
            addReceiptLine("Pickup Hub", rental.location)
            addReceiptLine("Rental Total", "Rs. ${format.format(totalRentalLkr.toInt())}")
        } else {
            addReceiptLine("Rental Vehicle", "Self-Arranged Transport")
        }

        addReceiptLine("Status", "CONFIRMED & ACTIVE")

        // View in Trip History Button
        val btnViewHistory = Button(requireContext()).apply {
            text = "View in Trip History"
            setBackgroundResource(R.drawable.button_primary)
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 24, 0, 8)
            }
        }

        btnViewHistory.setOnClickListener {
            dialog.dismiss()
            try {
                findNavController().navigate(R.id.action_planTripFragment_to_tripHistoryFragment)
            } catch (_: Exception) {
                try {
                    findNavController().navigate(R.id.tripHistoryFragment)
                } catch (_: Exception) {
                    findNavController().popBackStack()
                }
            }
        }

        val btnClose = Button(requireContext()).apply {
            text = "Done"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#64748B"))
        }
        btnClose.setOnClickListener {
            dialog.dismiss()
            findNavController().popBackStack()
        }

        rootLayout.addView(tvBadge)
        rootLayout.addView(tvTitle)
        rootLayout.addView(tvSubtext)
        rootLayout.addView(boxReceipt)
        rootLayout.addView(btnViewHistory)
        rootLayout.addView(btnClose)

        dialog.setContentView(rootLayout)
        dialog.show()
    }
}
