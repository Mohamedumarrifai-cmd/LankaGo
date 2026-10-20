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
import com.example.lankago.R
import com.example.lankago.adapters.ReviewAdapter
import com.example.lankago.data.ActivityItem
import com.example.lankago.utils.TripManager
import com.example.lankago.viewmodels.ActivityBookingViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
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
        val targetTrip = trips.firstOrNull()

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

                Snackbar.make(
                    requireView(),
                    "✓ \"${item.title}\" Added to your Itinerary!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
