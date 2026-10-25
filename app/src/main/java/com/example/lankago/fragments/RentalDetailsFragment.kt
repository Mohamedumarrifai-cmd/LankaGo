package com.example.lankago.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.NearbyRentalAdapter
import com.example.lankago.adapters.ReviewAdapter
import com.example.lankago.data.RentalVehicle
import com.example.lankago.utils.TripManager
import com.example.lankago.viewmodels.RentalDetailsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.lankago.data.Booking
import com.example.lankago.data.BookingStatus
import com.example.lankago.data.BookingType
import com.example.lankago.repository.AdminRepository
import com.example.lankago.repository.RentalRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RentalDetailsFragment : Fragment() {

    private val viewModel: RentalDetailsViewModel by viewModels()
    private lateinit var nearbyAdapter: NearbyRentalAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rental_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rentalId = arguments?.getString("rentalId")
            ?: arguments?.getString("vehicleId")
            ?: "rental_honda_dio"

        initViews(view)
        observeData(view)
        viewModel.loadRentalDetails(rentalId)
    }

    private fun initViews(view: View) {
        view.findViewById<ImageView>(R.id.btn_back_rental)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup nearby rentals horizontal RecyclerView
        val rvNearby = view.findViewById<RecyclerView>(R.id.rv_nearby_rentals)
        nearbyAdapter = NearbyRentalAdapter(emptyList()) { selectedRental ->
            viewModel.loadRentalDetails(selectedRental.id)
            view.findViewById<androidx.core.widget.NestedScrollView>(R.id.scroll_rental_details)?.scrollTo(0, 0)
        }
        rvNearby?.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = nearbyAdapter
        }

        // Setup reviews RecyclerView
        val rvReviews = view.findViewById<RecyclerView>(R.id.rv_rental_reviews)
        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }

        // Eager Booking Listener to ensure immediate responsiveness
        val rentalId = arguments?.getString("rentalId")
            ?: arguments?.getString("vehicleId")
            ?: "rental_honda_dio"
        view.findViewById<Button>(R.id.btn_book_rental)?.setOnClickListener {
            val current = viewModel.rental.value ?: RentalRepository.getRentalById(rentalId)
            if (current != null) {
                showRentalBookingConfirmation(current)
            }
        }
    }

    private fun observeData(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.rental.collectLatest { rental ->
                if (rental != null) {
                    bindRentalData(view, rental)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nearbyRentals.collectLatest { nearbyList ->
                nearbyAdapter.updateData(nearbyList)
            }
        }
    }

    private fun bindRentalData(view: View, rental: RentalVehicle) {
        // Hero Image
        val fallbackRes = when {
            rental.id.contains("galle", ignoreCase = true) || rental.name.contains("Galle", ignoreCase = true) -> R.drawable.galle_tuktuk
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
            rental.name.contains("NMAX", ignoreCase = true) -> R.drawable.nmax
            rental.name.contains("Dio", ignoreCase = true) -> R.drawable.dio
            rental.name.contains("PCX", ignoreCase = true) -> R.drawable.pcx
            rental.name.contains("FZ", ignoreCase = true) -> R.drawable.fz
            rental.name.contains("Enfield", ignoreCase = true) || rental.name.contains("Himalayan", ignoreCase = true) -> R.drawable.royalenflied
            rental.type.contains("Bike", ignoreCase = true) || rental.name.contains("Yamaha", ignoreCase = true) -> R.drawable.fz
            else -> R.drawable.aqua
        }

        val ivHero = view.findViewById<ImageView>(R.id.iv_rental_hero)
        if (ivHero != null) {
            val heroUrl = rental.images.firstOrNull()
            if (!heroUrl.isNullOrBlank()) {
                com.bumptech.glide.Glide.with(this)
                    .load(heroUrl)
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .centerCrop()
                    .into(ivHero)
            } else {
                ivHero.setImageResource(fallbackRes)
            }
        }

        // Multi-Photo Gallery Strip (> 5 Images)
        val galleryStrip = view.findViewById<LinearLayout>(R.id.layout_vehicle_gallery_strip)
        if (galleryStrip != null) {
            galleryStrip.removeAllViews()
            val allImages = if (rental.images.isNotEmpty()) rental.images else listOf("")
            val density = resources.displayMetrics.density
            val thumbWidth = (84 * density).toInt()
            val thumbHeight = (64 * density).toInt()
            val thumbMargin = (8 * density).toInt()
            val cornerRad = 10 * density

            allImages.forEachIndexed { index, imgUrl ->
                val cardView = com.google.android.material.card.MaterialCardView(requireContext()).apply {
                    radius = cornerRad
                    cardElevation = 2 * density
                    strokeWidth = if (index == 0) (2.5f * density).toInt() else (1 * density).toInt()
                    strokeColor = if (index == 0) Color.parseColor("#5D5FEF") else Color.parseColor("#CBD5E1")
                    layoutParams = LinearLayout.LayoutParams(thumbWidth, thumbHeight).apply {
                        setMargins(0, 0, thumbMargin, 0)
                    }
                }
                val ivThumb = ImageView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                if (!imgUrl.isNullOrBlank()) {
                    com.bumptech.glide.Glide.with(this)
                        .load(imgUrl)
                        .placeholder(fallbackRes)
                        .error(fallbackRes)
                        .centerCrop()
                        .into(ivThumb)
                } else {
                    ivThumb.setImageResource(fallbackRes)
                }
                cardView.addView(ivThumb)

                cardView.setOnClickListener {
                    if (ivHero != null && !imgUrl.isNullOrBlank()) {
                        com.bumptech.glide.Glide.with(this)
                            .load(imgUrl)
                            .placeholder(fallbackRes)
                            .error(fallbackRes)
                            .centerCrop()
                            .into(ivHero)
                    }
                    for (i in 0 until galleryStrip.childCount) {
                        val child = galleryStrip.getChildAt(i) as? com.google.android.material.card.MaterialCardView
                        child?.strokeWidth = if (i == index) (2.5f * density).toInt() else (1 * density).toInt()
                        child?.strokeColor = if (i == index) Color.parseColor("#5D5FEF") else Color.parseColor("#CBD5E1")
                    }
                }
                galleryStrip.addView(cardView)
            }
        }

        view.findViewById<TextView>(R.id.tv_appbar_rental_title)?.text = rental.name
        view.findViewById<TextView>(R.id.tv_rental_name)?.text = rental.name
        view.findViewById<TextView>(R.id.tv_rental_location)?.text = "${rental.type} • ${rental.location}"
        view.findViewById<TextView>(R.id.tv_rental_rating_badge)?.text = "${rental.rating} (${rental.reviewCount})"

        // Specs
        view.findViewById<TextView>(R.id.tv_spec_transmission)?.text = rental.transmission
        view.findViewById<TextView>(R.id.tv_spec_seats)?.text = "${rental.seatingCapacity} Persons"
        view.findViewById<TextView>(R.id.tv_spec_fuel)?.text = rental.fuelType

        // Vendor
        view.findViewById<TextView>(R.id.tv_vendor_name)?.text = rental.vendorName

        // Pricing
        val format = NumberFormat.getNumberInstance(Locale.US)
        val dailyStr = "Rs. ${format.format(rental.dailyRateLkr.toInt())}"
        val depositStr = "Rs. ${format.format(rental.depositLkr.toInt())}"
        val totalDueStr = "Rs. ${format.format((rental.dailyRateLkr + rental.depositLkr).toInt())}"

        view.findViewById<TextView>(R.id.tv_price_daily_rate)?.text = dailyStr
        view.findViewById<TextView>(R.id.tv_price_deposit)?.text = depositStr
        view.findViewById<TextView>(R.id.tv_price_total_due)?.text = totalDueStr
        view.findViewById<TextView>(R.id.tv_bottom_daily_price)?.text = "$dailyStr / day"

        // Direct Call Intent
        view.findViewById<View>(R.id.btn_call_vendor)?.setOnClickListener {
            try {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${rental.vendorPhone}"))
                startActivity(callIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to place phone call", Toast.LENGTH_SHORT).show()
            }
        }

        // Direct WhatsApp Button using https://wa.me/ API with robust error handling
        view.findViewById<View>(R.id.btn_whatsapp_vendor)?.setOnClickListener {
            try {
                val cleanPhone = rental.vendorWhatsapp.replace("+", "").replace(" ", "").trim()
                val message = "Hello ${rental.vendorName}, I am inquiring about renting the ${rental.name} via LankaGo."
                val waUrl = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                startActivity(waIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
            }
        }

        // Visit Website Button opening rental provider site in web browser
        view.findViewById<View>(R.id.btn_visit_website)?.setOnClickListener {
            try {
                val siteUrl = "https://www.lankago.lk/rentals/${rental.id}"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl))
                startActivity(webIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to open web browser", Toast.LENGTH_SHORT).show()
            }
        }

        // Availability Badge & Booking Action Edge-Case Handling
        val tvAvailability = view.findViewById<TextView>(R.id.tv_availability_badge)
        val btnBook = view.findViewById<Button>(R.id.btn_book_rental)

        val isSelectMode = arguments?.getBoolean("isSelectMode") == true ||
            arguments?.getString("mode") == "select_rental"

        if (rental.isAvailable) {
            tvAvailability?.text = "✓ Available Now"
            tvAvailability?.setBackgroundColor(Color.parseColor("#15803D"))
            tvAvailability?.setTextColor(Color.WHITE)

            btnBook?.isEnabled = true
            btnBook?.backgroundTintList = null

            if (isSelectMode) {
                btnBook?.text = "✓ Select Vehicle for Trip"
                btnBook?.setOnClickListener {
                    TripManager.selectedRentalForPlanning = rental
                    Toast.makeText(
                        requireContext(),
                        "✓ ${rental.name} selected for your booking!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("auto_confirm_booking", true)
                    findNavController().popBackStack()
                }
            } else {
                btnBook?.text = "Book This Rental"
                btnBook?.setOnClickListener {
                    showRentalBookingConfirmation(rental)
                }
            }
        } else {
            // Edge-Case Handling: Disabled with Sold Out badge
            tvAvailability?.text = "✕ Currently Sold Out"
            tvAvailability?.setBackgroundColor(Color.parseColor("#B91C1C"))
            tvAvailability?.setTextColor(Color.WHITE)

            btnBook?.isEnabled = false
            btnBook?.text = "Currently Unavailable"
            btnBook?.setBackgroundColor(Color.parseColor("#94A3B8"))

            Snackbar.make(
                view,
                "This vehicle is currently sold out. Check nearby alternatives below!",
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Reviews
        reviewAdapter.updateData(rental.reviews)
    }

    private fun showRentalBookingConfirmation(rental: RentalVehicle) {
        val trips = TripManager.getAllTrips(requireContext())
        val passedTripId = arguments?.getString("activeTripId") ?: ""
        val targetTrip = if (passedTripId.isNotBlank()) trips.find { it.id == passedTripId } ?: trips.firstOrNull() else trips.firstOrNull()

        val format = NumberFormat.getNumberInstance(Locale.US)
        val dailyStr = "Rs. ${format.format(rental.dailyRateLkr.toInt())}"
        val depositStr = "Rs. ${format.format(rental.depositLkr.toInt())}"
        val totalDueAmount = rental.dailyRateLkr + rental.depositLkr
        val totalDueStr = "Rs. ${format.format(totalDueAmount.toInt())}"

        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

        val scroll = android.widget.ScrollView(requireContext())
        val rootLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_bottom_sheet)
            setPadding(40, 40, 40, 40)
        }

        // Title
        val tvTitle = TextView(requireContext()).apply {
            text = "💳 Checkout & Vehicle Booking"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A2B4C"))
        }

        val tvSubtitle = TextView(requireContext()).apply {
            text = "${rental.name} • ${rental.vendorName}"
            textSize = 14f
            setTextColor(Color.parseColor("#8A8F98"))
            setPadding(0, 8, 0, 24)
        }

        // Price breakdown box
        val boxPricing = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F8FAFC"))
            setPadding(28, 28, 28, 28)
        }

        fun createPriceRow(label: String, value: String, isBold: Boolean = false): LinearLayout {
            return LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 6, 0, 6)
                addView(TextView(requireContext()).apply {
                    text = label
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setTextColor(if (isBold) Color.parseColor("#1A2B4C") else Color.parseColor("#64748B"))
                    textSize = if (isBold) 15f else 13f
                    if (isBold) setTypeface(null, android.graphics.Typeface.BOLD)
                })
                addView(TextView(requireContext()).apply {
                    text = value
                    setTextColor(if (isBold) Color.parseColor("#10B981") else Color.parseColor("#1A2B4C"))
                    textSize = if (isBold) 16f else 13f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })
            }
        }

        boxPricing.addView(createPriceRow("Daily Rental Rate", "$dailyStr / day"))
        boxPricing.addView(createPriceRow("Refundable Security Deposit", depositStr))
        boxPricing.addView(View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2).apply {
                setMargins(0, 12, 0, 12)
            }
            setBackgroundColor(Color.parseColor("#E2E8F0"))
        })
        boxPricing.addView(createPriceRow("Total Amount Payable", totalDueStr, true))

        // Linked trip banner
        val tvLinkedTrip = TextView(requireContext()).apply {
            val tripName = targetTrip?.tripName ?: "Active Trip Itinerary"
            text = "🌴 Linked to: $tripName"
            textSize = 13f
            setTextColor(Color.parseColor("#4F46E5"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 20, 0, 16)
        }

        // Payment Method label
        val tvPaymentLabel = TextView(requireContext()).apply {
            text = "Select Payment Method"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A2B4C"))
            setPadding(0, 8, 0, 12)
        }

        val rgPayment = RadioGroup(requireContext())
        val rbCard = RadioButton(requireContext()).apply {
            text = "Credit / Debit Card (Visa / Mastercard)"
            isChecked = true
            setTextColor(Color.parseColor("#1A2B4C"))
        }
        val rbLankaPay = RadioButton(requireContext()).apply {
            text = "LankaPay Online / Direct Bank"
            setTextColor(Color.parseColor("#1A2B4C"))
        }
        val rbCash = RadioButton(requireContext()).apply {
            text = "Cash on Vehicle Pickup"
            setTextColor(Color.parseColor("#1A2B4C"))
        }
        rgPayment.addView(rbCard)
        rgPayment.addView(rbLankaPay)
        rgPayment.addView(rbCash)

        // Dummy card input layout
        val layoutCardInputs = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 12)
        }

        val etCardNumber = EditText(requireContext()).apply {
            hint = "Card Number (e.g. 4532 8920 1204 8812)"
            setBackgroundResource(R.drawable.bg_input)
            setPadding(24, 24, 24, 24)
            setText("4532 •••• •••• 8891")
            textSize = 13f
            setTextColor(Color.parseColor("#1A2B4C"))
        }

        val rowCardDetails = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 12, 0, 0)
        }
        val etExpiry = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 8, 0)
            }
            hint = "MM/YY"
            setText("12/28")
            setBackgroundResource(R.drawable.bg_input)
            setPadding(24, 24, 24, 24)
            textSize = 13f
            setTextColor(Color.parseColor("#1A2B4C"))
        }
        val etCvv = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(8, 0, 0, 0)
            }
            hint = "CVV"
            setText("•••")
            setBackgroundResource(R.drawable.bg_input)
            setPadding(24, 24, 24, 24)
            textSize = 13f
            setTextColor(Color.parseColor("#1A2B4C"))
        }
        rowCardDetails.addView(etExpiry)
        rowCardDetails.addView(etCvv)
        layoutCardInputs.addView(etCardNumber)
        layoutCardInputs.addView(rowCardDetails)

        rgPayment.setOnCheckedChangeListener { _, checkedId ->
            layoutCardInputs.visibility = if (checkedId == rbCard.id) View.VISIBLE else View.GONE
        }

        // Action Buttons
        val btnConfirmPay = Button(requireContext()).apply {
            text = "Pay $totalDueStr & Confirm"
            setBackgroundResource(R.drawable.button_primary)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 24, 0, 8)
            }
        }

        val btnCancel = Button(requireContext()).apply {
            text = "Cancel"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#64748B"))
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirmPay.setOnClickListener {
            dialog.dismiss()

            val selectedMethod = when (rgPayment.checkedRadioButtonId) {
                rbLankaPay.id -> "LankaPay Online"
                rbCash.id -> "Cash on Pickup"
                else -> "Credit Card (Visa/Mastercard)"
            }

            val bookingRef = "LKG-PAY-${System.currentTimeMillis().toString().takeLast(6)}"
            val tripId = targetTrip?.id ?: passedTripId

            // 1. Add rental locally to TripManager and obtain updated Trip
            val bookedTrip = TripManager.addRentalToTripAndReturn(requireContext(), tripId, rental)

            // 2. Dispatch Payment Success Notification & In-App notification
            com.example.lankago.utils.LankaGoNotificationHelper.postPaymentSuccessNotification(
                context = requireContext(),
                bookingRef = bookingRef,
                vehicleName = rental.name,
                amountLkr = totalDueAmount
            )

            // 3. Sync booking to Firebase Realtime Database
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val authUser = FirebaseAuth.getInstance().currentUser
                    val uid = authUser?.uid ?: "guest_traveler"
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val booking = Booking(
                        id = bookingRef,
                        userId = uid,
                        tripId = bookedTrip.id,
                        userName = authUser?.displayName ?: "Traveler",
                        userEmail = authUser?.email ?: "traveler@lankago.lk",
                        userPhone = "",
                        itemName = "${rental.name} (${rental.vendorName})",
                        itemCategory = rental.type,
                        bookingType = BookingType.RENTAL,
                        bookingDate = todayStr,
                        bookingTime = "10:00 AM",
                        totalPriceLkr = totalDueAmount,
                        status = BookingStatus.CONFIRMED,
                        specialNotes = "Paid via $selectedMethod. Ref: $bookingRef. Pickup: ${rental.location}"
                    )
                    AdminRepository().addBooking(booking)

                    // Also mirror to user trip history node
                    val rtdb = FirebaseDatabase.getInstance().reference
                    rtdb.child("users")
                        .child(uid)
                        .child("tripHistory")
                        .child(bookedTrip.id)
                        .setValue(bookedTrip)

                    rtdb.child("trips")
                        .child(uid)
                        .child(bookedTrip.id)
                        .setValue(bookedTrip)
                } catch (e: Exception) {
                    Log.w("RentalDetailsFragment", "Firebase sync warning: ${e.message}")
                }
            }

            // 4. Show Generated Receipt Dialog
            showPaymentReceiptDialog(
                bookingRef = bookingRef,
                rental = rental,
                tripName = targetTrip?.tripName ?: "Sri Lanka Adventure",
                amountPaidStr = totalDueStr,
                paymentMethod = selectedMethod
            )
        }

        rootLayout.addView(tvTitle)
        rootLayout.addView(tvSubtitle)
        rootLayout.addView(boxPricing)
        rootLayout.addView(tvLinkedTrip)
        rootLayout.addView(tvPaymentLabel)
        rootLayout.addView(rgPayment)
        rootLayout.addView(layoutCardInputs)
        rootLayout.addView(btnConfirmPay)
        rootLayout.addView(btnCancel)

        scroll.addView(rootLayout)
        dialog.setContentView(scroll)
        dialog.show()
    }

    private fun showPaymentReceiptDialog(
        bookingRef: String,
        rental: RentalVehicle,
        tripName: String,
        amountPaidStr: String,
        paymentMethod: String
    ) {
        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val rootLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_bottom_sheet)
            setPadding(40, 40, 40, 40)
        }

        // Green success badge
        val tvBadge = TextView(requireContext()).apply {
            text = "✓ PAYMENT SUCCESSFUL"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#10B981"))
            setPadding(0, 0, 0, 8)
        }

        val tvTitle = TextView(requireContext()).apply {
            text = "Booking & Payment Receipt"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1A2B4C"))
        }

        val tvSubtext = TextView(requireContext()).apply {
            text = "Your rental has been successfully confirmed and added to your Trip History."
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
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })
            })
        }

        val dateStr = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.US).format(Date())
        addReceiptLine("Receipt No.", bookingRef)
        addReceiptLine("Date & Time", dateStr)
        addReceiptLine("Vehicle Reserved", rental.name)
        addReceiptLine("Provider / Shop", rental.vendorName)
        addReceiptLine("Pickup Location", rental.location)
        addReceiptLine("Linked Trip", tripName)
        addReceiptLine("Payment Mode", paymentMethod)
        addReceiptLine("Amount Paid", amountPaidStr)
        addReceiptLine("Status", "CONFIRMED & RESERVED")

        // View in Trip History Button
        val btnViewHistory = Button(requireContext()).apply {
            text = "View in Trip History"
            setBackgroundResource(R.drawable.button_primary)
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
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
                findNavController().navigate(R.id.action_rentalDetailsFragment_to_tripHistoryFragment)
            } catch (_: Exception) {
                try {
                    findNavController().navigate(R.id.tripHistoryFragment)
                } catch (_: Exception) {
                    findNavController().popBackStack()
                }
            }
        }

        rootLayout.addView(tvBadge)
        rootLayout.addView(tvTitle)
        rootLayout.addView(tvSubtext)
        rootLayout.addView(boxReceipt)
        rootLayout.addView(btnViewHistory)

        dialog.setContentView(rootLayout)
        dialog.show()
    }
}
