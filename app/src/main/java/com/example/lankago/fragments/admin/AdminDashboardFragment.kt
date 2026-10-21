package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.viewmodels.AdminViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Modern Material Design Admin Dashboard.
 * Displays management shortcuts in a responsive grid and collects mock metrics from [AdminViewModel].
 */
class AdminDashboardFragment : Fragment() {

    private val adminViewModel: AdminViewModel by activityViewModels()

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
        val cardLogout = view.findViewById<MaterialCardView>(R.id.card_admin_logout)
        val tvRentalsCount = view.findViewById<TextView>(R.id.tv_metric_rentals_count)

        // Reactively observe StateFlow of rental shops to update count badge
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.rentalShops.collect { shops ->
                    tvRentalsCount.text = shops.size.toString()
                }
            }
        }

        // 1. Manage Rentals -> Navigate to AdminRentalListFragment
        cardRentals.setOnClickListener {
            findNavController().navigate(
                R.id.action_adminDashboardFragment_to_adminRentalListFragment
            )
        }

        // 2. Manage Products
        cardProducts.setOnClickListener {
            Snackbar.make(
                view,
                "Local Products Management module is scheduled for future release.",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // 3. Manage Activities
        cardActivities.setOnClickListener {
            Snackbar.make(
                view,
                "Cultural Activities & Tours Management is scheduled for future release.",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // 4. Logout Confirmation & Navigation
        cardLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to end your administrator session?")
                .setPositiveButton("Logout") { _, _ ->
                    adminViewModel.logout()
                    findNavController().navigate(
                        R.id.action_adminDashboardFragment_to_adminLoginFragment
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
