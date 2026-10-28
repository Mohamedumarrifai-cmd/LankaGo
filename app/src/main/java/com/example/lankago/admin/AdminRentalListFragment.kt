package com.example.lankago.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lankago.R
import com.example.lankago.databinding.FragmentAdminRentalListBinding
import com.example.lankago.model.Rental
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AdminRentalListFragment : Fragment() {

    private var _binding: FragmentAdminRentalListBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel scoped to Activity lifecycle
    private val viewModel: AdminSharedViewModel by activityViewModels()

    private lateinit var rentalAdapter: AdminRentalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminRentalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        rentalAdapter = AdminRentalAdapter(
            onEditClick = { rental ->
                onEditRentalClicked(rental)
            },
            onDeleteClick = { rental ->
                onDeleteRentalClicked(rental)
            }
        )

        binding.rvRentals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rentalAdapter
        }
    }

    private fun setupListeners() {
        // Back Button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // FAB Click -> Add Mode (Pass null or empty string ID)
        binding.fabAddRental.setOnClickListener {
            navigateToAddEditRental(rentalId = "")
        }
    }

    private fun observeViewModel() {
        // Collect StateFlow safely with repeatOnLifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rentals.collect { rentals ->
                    updateUiState(rentals)
                }
            }
        }
    }

    private fun updateUiState(rentals: List<Rental>) {
        rentalAdapter.submitList(rentals)
        if (rentals.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvRentals.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvRentals.visibility = View.VISIBLE
        }
    }

    // Edit Icon Click -> Edit Mode (Pass specific rentalId)
    private fun onEditRentalClicked(rental: Rental) {
        navigateToAddEditRental(rentalId = rental.id)
    }

    // Delete Icon Click -> Confirm with Material AlertDialog
    private fun onDeleteRentalClicked(rental: Rental) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Rental Shop")
            .setMessage("Are you sure you want to delete this shop?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRental(rental.id)
                Toast.makeText(
                    requireContext(),
                    "'${rental.shopName}' deleted successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToAddEditRental(rentalId: String?) {
        val bundle = Bundle().apply {
            putString("rentalId", rentalId ?: "")
        }
        try {
            findNavController().navigate(
                R.id.action_adminRentalListFragment_to_addEditRentalFragment,
                bundle
            )
        } catch (e: Exception) {
            try {
                findNavController().navigate(R.id.addEditRentalFragment, bundle)
            } catch (ex: Exception) {
                Toast.makeText(
                    requireContext(),
                    if (rentalId.isNullOrEmpty()) "Opening Add Rental" else "Opening Edit Rental",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
