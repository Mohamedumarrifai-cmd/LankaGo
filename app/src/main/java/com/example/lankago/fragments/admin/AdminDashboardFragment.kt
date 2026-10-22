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
        val cardBookings = view.findViewById<MaterialCardView>(R.id.card_view_bookings)
        val cardLogout = view.findViewById<MaterialCardView>(R.id.card_admin_logout)
        val tvRentalsCount = view.findViewById<TextView>(R.id.tv_metric_rentals_count)

        // Reactively observe live rentals from Firebase Realtime Database
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeRentals().collect { shops ->
                        tvRentalsCount.text = shops.size.toString()
                    }
                } catch (_: Exception) {
                    // Fallback to fetch once
                    try {
                        val once = adminRepository.getAllRentalsOnce()
                        tvRentalsCount.text = once.size.toString()
                    } catch (_: Exception) {
                        tvRentalsCount.text = "0"
                    }
                }
            }
        }

        // 1. Manage Rentals -> Navigate to AdminRentalListFragment
        cardRentals.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminRentalListFragment
            )
        }

        // 2. Manage Products -> Navigate to AdminProductListFragment
        cardProducts.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminProductListFragment
            )
        }

        // 3. Manage Activities -> Navigate to AdminActivityListFragment
        cardActivities.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminActivityListFragment
            )
        }

        // 4. View User Bookings -> Navigate to AdminBookingsListFragment
        cardBookings?.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_adminDashboardFragment_to_adminBookingsListFragment
            )
        }

        // 5. Logout Confirmation & Navigation
        cardLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to end your administrator session?")
                .setPositiveButton("Logout") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    findNavController().safeNavigate(
                        R.id.action_adminDashboardFragment_to_adminLoginFragment
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
