package com.example.lankago.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.adapters.ReviewAdapter
import com.example.lankago.data.ActivityItem
import com.example.lankago.utils.TripManager
import com.example.lankago.viewmodels.ActivityBookingViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.lankago.data.Booking
import com.example.lankago.data.BookingStatus
import com.example.lankago.data.BookingType
import com.example.lankago.repository.AdminRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityBookingFragment : Fragment() {

    private val viewModel: ActivityBookingViewModel by viewModels()
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activity_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityId = arguments?.getString("activityId") ?: "act_sigiriya_hike"

        initViews(view)
        observeData(view)
        viewModel.loadActivityDetails(activityId)
    }

    private fun initViews(view: View) {
        view.findViewById<ImageView>(R.id.btn_back_activity)?.setOnClickListener {
            findNavController().navigateUp()
        }

        val rvReviews = view.findViewById<RecyclerView>(R.id.rv_activity_reviews)
        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
    }

    private fun observeData(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activity.collectLatest { item ->
                if (item != null) {
                    bindActivityData(view, item)
                }
            }
        }
    }

    private fun bindActivityData(view: View, item: ActivityItem) {
        // Hero Cover Image
        val ivHero = view.findViewById<ImageView>(R.id.iv_activity_hero)
            ?: view.findViewById<ImageView>(R.id.ivCoverImage)
        if (ivHero != null) {
            if (!item.imageUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.pot_making)
                    .error(R.drawable.pot_making)
                    .centerCrop()
                    .into(ivHero)
            } else {
                ivHero.setImageResource(R.drawable.pot_making)
            }
        }

        view.findViewById<TextView>(R.id.tv_appbar_activity_title)?.text = item.title
        view.findViewById<TextView>(R.id.tv_activity_title)?.text = item.title
        view.findViewById<TextView>(R.id.tv_activity_category_badge)?.text = item.category
        view.findViewById<TextView>(R.id.tv_activity_rating_badge)?.text = "${item.rating} (${item.reviewCount})"
        view.findViewById<TextView>(R.id.tv_activity_address)?.text = item.address
        view.findViewById<TextView>(R.id.tv_activity_duration)?.text = "⏱ ${item.durationHours} Hours Duration"
        view.findViewById<TextView>(R.id.tv_activity_description)?.text = item.description

        // Inclusions
        val inclusionsText = item.includedItems.joinToString("\n") { "• $it" }
        view.findViewById<TextView>(R.id.tv_activity_inclusions)?.text = inclusionsText

        // Pricing
        val format = NumberFormat.getNumberInstance(Locale.US)
        val basePrice = format.format(item.basePriceLkr.toInt())
        val serviceFee = format.format(item.serviceFeeLkr.toInt())
        val totalPrice = format.format((item.basePriceLkr + item.serviceFeeLkr).toInt())

        view.findViewById<TextView>(R.id.tv_activity_base_price)?.text = "Rs. $basePrice"
        view.findViewById<TextView>(R.id.tv_activity_service_fee)?.text = "Rs. $serviceFee"
        view.findViewById<TextView>(R.id.tv_activity_total_price)?.text = "Rs. $totalPrice"
        view.findViewById<TextView>(R.id.tv_activity_bottom_price)?.text = "Rs. $totalPrice / person"

        // Booking Action
        view.findViewById<Button>(R.id.btn_book_activity)?.setOnClickListener {
            showActivityBookingDialog(item)
        }

        // Reviews
        reviewAdapter.updateData(item.reviews)
    }

    private fun showActivityBookingDialog(item: ActivityItem) {
        val trips = TripManager.getAllTrips(requireContext())
        val passedTripId = arguments?.getString("activeTripId") ?: ""
        val targetTrip = if (passedTripId.isNotBlank()) trips.find { it.id == passedTripId } ?: trips.firstOrNull() else trips.firstOrNull()

        val format = NumberFormat.getNumberInstance(Locale.US)
        val totalPrice = format.format((item.basePriceLkr + item.serviceFeeLkr).toInt())

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Activity Booking")
            .setMessage(
                "Activity: ${item.title}\n" +
                "Duration: ${item.durationHours} Hours\n" +
                "Price per Person: Rs. $totalPrice\n\n" +
                if (targetTrip != null) "This activity will be linked to your itinerary: \"${targetTrip.tripName}\"."
                else "This reservation will be saved locally in your trip manager."
            )
            .setPositiveButton("Confirm Booking") { _, _ ->
                val tripId = targetTrip?.id ?: ""
                TripManager.addActivityToTrip(requireContext(), tripId, item)

                // Save to Firebase Realtime Database /bookings node
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val authUser = FirebaseAuth.getInstance().currentUser
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        val booking = Booking(
                            userName = authUser?.displayName ?: "Traveler",
                            userEmail = authUser?.email ?: "traveler@lankago.lk",
                            userPhone = "",
                            itemName = item.title,
                            itemCategory = item.category,
                            bookingType = BookingType.ACTIVITY,
                            bookingDate = todayStr,
                            bookingTime = "09:00 AM",
                            totalPriceLkr = item.basePriceLkr + item.serviceFeeLkr,
                            status = BookingStatus.CONFIRMED,
                            specialNotes = "Location: ${item.address}. Duration: ${item.durationHours} hrs."
                        )
                        AdminRepository().addBooking(booking)
                    } catch (_: Exception) {
                        // TripManager has already recorded reservation
                    }
                }

                val currentView = view ?: return@setPositiveButton
                Snackbar.make(
                    currentView,
                    "✓ \"${item.title}\" confirmed & added to your Itinerary!",
                    Snackbar.LENGTH_SHORT
                ).show()

                currentView.postDelayed({
                    if (isAdded) {
                        try {
                            findNavController().navigate(R.id.action_activityBookingFragment_to_tripPlannerFragment)
                        } catch (_: Exception) {
                            findNavController().popBackStack()
                        }
                    }
                }, 800L)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
