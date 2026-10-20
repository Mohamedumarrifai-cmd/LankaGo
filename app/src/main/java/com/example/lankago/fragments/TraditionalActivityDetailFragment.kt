package com.example.lankago.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.data.ActivityItem
import com.example.lankago.data.Review
import com.example.lankago.data.TraditionalActivity
import com.example.lankago.utils.TripManager
import com.example.lankago.viewmodels.TraditionalActivityDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TraditionalActivityDetailFragment : Fragment() {

    private val viewModel: TraditionalActivityDetailViewModel by viewModels()
    private var currentActivity: TraditionalActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_traditional_activity_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityId = arguments?.getString("traditionalActivityId") ?: "act_pottery_making"

        initListeners(view)
        observeData(view)
        viewModel.loadActivity(activityId)
    }

    private fun initListeners(view: View) {
        view.findViewById<ImageButton>(R.id.btn_back_activity)?.setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<ImageButton>(R.id.btn_favorite_activity)?.setOnClickListener { btn ->
            val favBtn = btn as ImageButton
            favBtn.isSelected = !favBtn.isSelected
            if (favBtn.isSelected) {
                favBtn.setImageResource(R.drawable.ic_heart_filled)
                Snackbar.make(view, "Added to your saved activities!", Snackbar.LENGTH_SHORT).show()
            } else {
                favBtn.setImageResource(R.drawable.ic_heart_outline)
                Snackbar.make(view, "Removed from saved activities.", Snackbar.LENGTH_SHORT).show()
            }
        }

        view.findViewById<MaterialButton>(R.id.btn_book_activity)?.setOnClickListener {
            currentActivity?.let { activity ->
                showBookingBottomSheet(activity)
            }
        }
    }

    private fun observeData(rootView: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activity.collectLatest { item ->
                if (item != null) {
                    currentActivity = item
                    bindActivityDetails(rootView, item)
                }
            }
        }
    }

    private fun bindActivityDetails(view: View, item: TraditionalActivity) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        view.findViewById<ImageView>(R.id.iv_activity_hero)?.setImageResource(item.imageRes)
        view.findViewById<TextView>(R.id.tv_activity_category_badge)?.text = item.category
        view.findViewById<TextView>(R.id.tv_activity_title)?.text = item.title
        view.findViewById<TextView>(R.id.tv_activity_rating)?.text =
            "${item.rating} (${item.reviewCount} reviews)"
        view.findViewById<TextView>(R.id.tv_activity_location)?.text = item.location
        view.findViewById<TextView>(R.id.tv_spec_duration)?.text = item.duration

        view.findViewById<ImageView>(R.id.iv_instructor_avatar)?.setImageResource(item.hostAvatarRes)
        view.findViewById<TextView>(R.id.tv_instructor_name)?.text = item.hostName
        view.findViewById<TextView>(R.id.tv_instructor_title)?.text = item.hostTitle

        view.findViewById<TextView>(R.id.tv_activity_description)?.text = item.description
        view.findViewById<TextView>(R.id.tv_workshop_address)?.text = item.address

        val priceStr = "LKR ${numberFormat.format(item.priceLkr)}"
        view.findViewById<TextView>(R.id.tv_bottom_price)?.text = priceStr

        // Render Highlights
        val highlightsContainer = view.findViewById<LinearLayout>(R.id.layout_activity_highlights)
        highlightsContainer?.removeAllViews()

        item.highlights.forEach { highlightText ->
            val highlightRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            val checkIcon = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(42, 42).apply {
                    setMargins(0, 4, 16, 0)
                }
                setImageResource(R.drawable.ic_check)
            }

            val label = TextView(requireContext()).apply {
                text = highlightText
                textSize = 14f
                setTextColor(Color.parseColor("#4B5563"))
                setLineSpacing(4f, 1f)
            }

            highlightRow.addView(checkIcon)
            highlightRow.addView(label)
            highlightsContainer?.addView(highlightRow)
        }
    }

    /**
     * Material BottomSheet for confirming booking date, time slot, guest count, and total price.
     */
    private fun showBookingBottomSheet(activity: TraditionalActivity) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_book_traditional_activity, null)
        bottomSheetDialog.setContentView(dialogView)

        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Default to tomorrow
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
        var selectedDateStr = dateFormat.format(calendar.time)

        var guestCount = 1
        var selectedTimeSlot = activity.timeSlots.firstOrNull() ?: "09:00 AM - 11:30 AM"
        val serviceFee = 500.0

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_activity_title)
        val tvUnitPrice = dialogView.findViewById<TextView>(R.id.tv_dialog_unit_price)
        val cardSelectDate = dialogView.findViewById<CardView>(R.id.card_select_date)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tv_selected_date)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chip_group_time_slots)
        val btnMinus = dialogView.findViewById<ImageButton>(R.id.btn_minus_guest)
        val btnPlus = dialogView.findViewById<ImageButton>(R.id.btn_plus_guest)
        val tvGuestCount = dialogView.findViewById<TextView>(R.id.tv_guest_count)
        val tvSubtotalLabel = dialogView.findViewById<TextView>(R.id.tv_calc_subtotal_label)
        val tvSubtotalVal = dialogView.findViewById<TextView>(R.id.tv_calc_subtotal_val)
        val tvServiceFee = dialogView.findViewById<TextView>(R.id.tv_calc_service_fee)
        val tvTotalPrice = dialogView.findViewById<TextView>(R.id.tv_dialog_total_price)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel_booking)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.btn_confirm_booking)

        tvTitle?.text = activity.title
        tvUnitPrice?.text = "LKR ${numberFormat.format(activity.priceLkr)}/pp"
        tvSelectedDate?.text = selectedDateStr
        tvServiceFee?.text = "LKR ${numberFormat.format(serviceFee)}"

        fun updatePriceCalculation() {
            val subtotal = activity.priceLkr * guestCount
            val total = subtotal + serviceFee
            tvGuestCount?.text = guestCount.toString()
            tvSubtotalLabel?.text = "Activity Fee ($guestCount ${if (guestCount > 1) "guests" else "guest"})"
            tvSubtotalVal?.text = "LKR ${numberFormat.format(subtotal)}"
            tvTotalPrice?.text = "LKR ${numberFormat.format(total)}"
        }

        updatePriceCalculation()

        // 1. Date selection listener
        cardSelectDate?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateStr = dateFormat.format(calendar.time)
                    tvSelectedDate?.text = selectedDateStr
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() + 86400000L // Min tomorrow
            datePickerDialog.show()
        }

        // 2. Time slot chips population
        chipGroup?.removeAllViews()
        activity.timeSlots.forEachIndexed { index, slot ->
            val chip = Chip(requireContext()).apply {
                text = slot
                isCheckable = true
                isChecked = index == 0
                shapeAppearanceModel = com.google.android.material.shape.ShapeAppearanceModel.builder()
                    .setAllCornerSizes(24f)
                    .build()
                setChipBackgroundColorResource(android.R.color.transparent)
                chipStrokeWidth = 3f
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#5D5FEF"))
                setTextColor(ColorStateList.valueOf(Color.parseColor("#1A2B4C")))
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedTimeSlot = slot
                    }
                }
            }
            chipGroup?.addView(chip)
        }

        // 3. Guest count stepper listeners
        btnMinus?.setOnClickListener {
            if (guestCount > 1) {
                guestCount--
                updatePriceCalculation()
            }
        }

        btnPlus?.setOnClickListener {
            if (guestCount < 10) {
                guestCount++
                updatePriceCalculation()
            }
        }

        // 4. Cancel & Confirm buttons
        btnCancel?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        btnConfirm?.setOnClickListener {
            val subtotal = activity.priceLkr * guestCount
            val total = subtotal + serviceFee

            // Save to active trip via TripManager
            val bookedItem = ActivityItem(
                id = "${activity.id}_${System.currentTimeMillis()}",
                title = "${activity.title} ($selectedTimeSlot)",
                category = activity.category,
                location = activity.location,
                address = activity.address,
                durationHours = activity.durationHours,
                basePriceLkr = subtotal,
                serviceFeeLkr = serviceFee,
                rating = activity.rating,
                reviewCount = activity.reviewCount,
                isAvailable = true,
                imageUrl = "android.resource://com.example.lankago/${activity.imageRes}",
                description = "Instructor: ${activity.hostName}. Scheduled on $selectedDateStr for $guestCount guest(s).",
                includedItems = activity.highlights,
                reviews = listOf(
                    Review("rev_user", "You", null, 5.0f, selectedDateStr, "Confirmed reservation.")
                )
            )

            val trips = TripManager.getAllTrips(requireContext())
            val targetTripId = trips.firstOrNull()?.id ?: ""
            TripManager.addActivityToTrip(requireContext(), targetTripId, bookedItem)

            bottomSheetDialog.dismiss()

            // Display confirmation Alert Dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Booking Confirmed!")
                .setMessage(
                    "You are successfully booked for:\n\n" +
                            "• Activity: ${activity.title}\n" +
                            "• Instructor: ${activity.hostName}\n" +
                            "• Date: $selectedDateStr\n" +
                            "• Time Slot: $selectedTimeSlot\n" +
                            "• Guests: $guestCount\n" +
                            "• Total Price: LKR ${numberFormat.format(total)}\n\n" +
                            "A confirmation and directions have been added to your Trip Planner."
                )
                .setPositiveButton("View Trip Planner") { _, _ ->
                    findNavController().navigate(R.id.action_homeFragment_to_tripPlannerFragment)
                }
                .setNegativeButton("Done", null)
                .show()
        }

        bottomSheetDialog.show()
    }
}
