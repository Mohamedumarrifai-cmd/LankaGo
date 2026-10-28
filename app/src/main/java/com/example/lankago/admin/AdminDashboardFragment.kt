package com.example.lankago.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.databinding.FragmentAdminDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 1. Manage Rentals Card
        binding.cardManageRentals.setOnClickListener {
            navigateToDestination(R.id.adminRentalListFragment, "Manage Rentals")
        }

        // 2. Manage Products Card
        binding.cardManageProducts.setOnClickListener {
            navigateToDestination(R.id.adminProductListFragment, "Manage Products")
        }

        // 3. Manage Activities Card
        binding.cardManageActivities.setOnClickListener {
            navigateToDestination(R.id.adminActivityListFragment, "Manage Activities")
        }

        // 4. View User Bookings Card
        binding.cardUserBookings.setOnClickListener {
            navigateToDestination(R.id.adminUserBookingsFragment, "User Bookings")
        }

        // 5. Logout Card
        binding.cardLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun navigateToDestination(destinationId: Int, featureName: String) {
        try {
            findNavController().navigate(destinationId)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Navigating to $featureName",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out of the Admin Panel?")
            .setPositiveButton("Logout") { _, _ ->
                Toast.makeText(
                    requireContext(),
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
