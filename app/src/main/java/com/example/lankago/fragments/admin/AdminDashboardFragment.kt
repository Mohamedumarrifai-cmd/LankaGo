package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.safeNavigate
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Modern Material Design Admin Dashboard connected to Firebase.
 * Displays management shortcuts in a responsive grid and collects live metrics from Firebase Realtime Database.
 */
class AdminDashboardFragment : Fragment() {

    private val adminRepository = AdminRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardRentals = view.findViewById<MaterialCardView>(R.id.card_manage_rentals)
        val cardProducts = view.findViewById<MaterialCardView>(R.id.card_manage_products)
        val cardActivities = view.findViewById<MaterialCardView>(R.id.card_manage_activities)
        val cardPlaces = view.findViewById<MaterialCardView>(R.id.card_manage_places)
        val cardFestivals = view.findViewById<MaterialCardView>(R.id.card_manage_festivals)
        val cardBookings = view.findViewById<MaterialCardView>(R.id.card_view_bookings)
        val cardLogout = view.findViewById<MaterialCardView>(R.id.card_admin_logout)

        val tvRentalsCount = view.findViewById<TextView>(R.id.tv_metric_rentals_count)
        val tvProductsCount = view.findViewById<TextView>(R.id.tv_metric_products_count)
        val tvPlacesCount = view.findViewById<TextView>(R.id.tv_metric_places_count)
        val tvFestivalsCount = view.findViewById<TextView>(R.id.tv_metric_festivals_count)

        // Observe Places and Festivals counts
        try {
            val places = com.example.lankago.utils.LankaGoDataManager.getPlaces(requireContext())
            tvPlacesCount?.text = places.size.toString()
        } catch (_: Exception) {}

        try {
            val festivals = com.example.lankago.repository.CulturalFestivalRepository.getFestivals(requireContext())
            tvFestivalsCount?.text = festivals.size.toString()
        } catch (_: Exception) {}

        // Reactively observe live rentals & products from Firebase
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeRentals().collect { shops ->
                        val count = if (shops.isNotEmpty()) shops.size else com.example.lankago.data.RentalShopDataProvider.shops.size
                        tvRentalsCount?.text = count.toString()
                    }
                } catch (_: Exception) {
                    tvRentalsCount?.text = com.example.lankago.data.RentalShopDataProvider.shops.size.toString()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeProducts().collect { prods ->
                        val count = if (prods.isNotEmpty()) prods.size else com.example.lankago.data.LocalItemDataProvider.items.size
                        tvProductsCount?.text = count.toString()
                    }
                } catch (_: Exception) {
                    tvProductsCount?.text = com.example.lankago.data.LocalItemDataProvider.items.size.toString()
                }
            }
        }

        // 1. Manage Rentals -> Navigate to AdminRentalListFragment
        cardRentals?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminRentalListFragment
            )
        }

        // 2. Manage Products -> Navigate to AdminProductListFragment
        cardProducts?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminProductListFragment
            )
        }

        // 3. Manage Activities -> Navigate to AdminActivityListFragment
        cardActivities?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminActivityListFragment
            )
        }

        // 4. Manage Places & Destinations -> Navigate to AdminPlaceListFragment
        cardPlaces?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminPlaceListFragment
            )
        }

        // 5. Manage Shows & Festivals -> Navigate to AdminFestivalListFragment
        cardFestivals?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminFestivalListFragment
            )
        }

        // 6. View User Bookings -> Navigate to AdminBookingsListFragment
        cardBookings?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminBookingsListFragment
            )
        }

        // 7. Switch to Traveler Home View
        val cardSwitchMode = view.findViewById<View>(R.id.card_switch_to_user_mode)
        cardSwitchMode?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_homeFragment
            )
        }

        // 6. Logout Confirmation & Navigation
        cardLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Administrator Session")
                .setMessage("Would you like to logout from administrator access or switch to Traveler mode?")
                .setPositiveButton("Logout") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().safeNavigate(
                        R.id.action_adminDashboardFragment_to_loginFragment
                    )
                }
                .setNeutralButton("Traveler Home") { _, _ ->
                    findNavController().safeNavigate(
                        R.id.action_adminDashboardFragment_to_homeFragment
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
