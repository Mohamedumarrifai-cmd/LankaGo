package com.example.lankago.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
import com.google.firebase.auth.FirebaseAuth
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

        val rentalId = arguments?.getString("rentalId") ?: "rental_scooter_ella"

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
            view.findViewById<androidx.core.widget.NestedScrollView>(R.id.btn_back_rental)?.scrollTo(0, 0)
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

        if (rental.isAvailable) {
            tvAvailability?.text = "✓ Available Now"
            tvAvailability?.setBackgroundColor(Color.parseColor("#15803D"))
            tvAvailability?.setTextColor(Color.WHITE)

            btnBook?.isEnabled = true
            btnBook?.text = "Book This Rental"
            btnBook?.backgroundTintList = null

            btnBook?.setOnClickListener {
                showRentalBookingConfirmation(rental)
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
        val targetTrip = trips.firstOrNull()

        val format = NumberFormat.getNumberInstance(Locale.US)
        val totalDue = format.format((rental.dailyRateLkr + rental.depositLkr).toInt())

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Rental Booking")
            .setMessage(
                "Vehicle: ${rental.name}\n" +
                "Vendor: ${rental.vendorName}\n" +
                "Estimated Total Due on Pickup: Rs. $totalDue\n\n" +
                if (targetTrip != null) "This rental will be automatically linked to your trip: \"${targetTrip.tripName}\"."
                else "This rental reservation will be saved locally in your trip manager."
            )
            .setPositiveButton("Confirm & Add to Trip") { _, _ ->
                val tripId = targetTrip?.id ?: ""
                TripManager.addRentalToTrip(requireContext(), tripId, rental)

                // Save to Firebase Realtime Database /bookings node
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val authUser = FirebaseAuth.getInstance().currentUser
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        val booking = Booking(
                            userName = authUser?.displayName ?: "Traveler",
                            userEmail = authUser?.email ?: "traveler@lankago.lk",
                            userPhone = "",
                            itemName = "${rental.name} (${rental.vendorName})",
                            itemCategory = rental.type,
                            bookingType = BookingType.RENTAL,
                            bookingDate = todayStr,
                            bookingTime = "10:00 AM",
                            totalPriceLkr = rental.dailyRateLkr + rental.depositLkr,
                            status = BookingStatus.CONFIRMED,
                            specialNotes = "Pickup location: ${rental.location}. Transmission: ${rental.transmission}"
                        )
                        AdminRepository().addBooking(booking)
                    } catch (_: Exception) {
                        // TripManager already persisted locally
                    }
                }

                Snackbar.make(
                    requireView(),
                    "✓ ${rental.name} reserved & added to Itinerary!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
