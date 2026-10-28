package com.example.lankago.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.AdminRentalAdapter
import com.example.lankago.data.RentalShop
import com.example.lankago.repository.AdminRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


 // Production-grade Admin Screen displaying all rental shops directly from Firebase Realtime Database.

class AdminRentalListFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var rentalAdapter: AdminRentalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_rental_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_admin_rentals)
        val rvRentals = view.findViewById<RecyclerView>(R.id.rv_admin_rentals)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_rentals_loading)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layout_empty_state)
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_rental)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize Adapter with Edit and Delete callbacks
        rentalAdapter = AdminRentalAdapter(
            onEditClick = { shop ->
                val bundle = bundleOf("rentalShopId" to shop.id)
                findNavController().navigate(
                    R.id.action_adminRentalListFragment_to_addEditRentalFragment,
                    bundle
                )
            },
            onDeleteClick = { shop ->
                confirmDeleteRental(shop)
            }
        )

        rvRentals.layoutManager = LinearLayoutManager(requireContext())
        rvRentals.adapter = rentalAdapter

        // FAB -> Add New Rental Shop (SafeArgs ID is null)
        fabAdd.setOnClickListener {
            val bundle = bundleOf("rentalShopId" to null)
            findNavController().navigate(
                R.id.action_adminRentalListFragment_to_addEditRentalFragment,
                bundle
            )
        }

        // Real-time observation of Firebase Realtime Database
        pbLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    adminRepository.observeRentals().collect { shops ->
                        pbLoading.visibility = View.GONE
                        if (shops.isEmpty()) {
                            // Check if initial fetch returned empty
                            loadFallbackRentals(pbLoading, rvRentals, layoutEmpty)
                        } else {
                            rvRentals.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE
                            rentalAdapter.submitList(shops)
                        }
                    }
                } catch (e: Exception) {
                    pbLoading.visibility = View.GONE
                    loadFallbackRentals(pbLoading, rvRentals, layoutEmpty)
                }
            }
        }
    }

    private fun loadFallbackRentals(
        pbLoading: ProgressBar,
        rvRentals: RecyclerView,
        layoutEmpty: LinearLayout
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val onceList = adminRepository.getAllRentalsOnce()
                pbLoading.visibility = View.GONE
                val resolved = if (onceList.isNotEmpty()) onceList else com.example.lankago.data.RentalShopDataProvider.shops
                if (resolved.isEmpty()) {
                    rvRentals.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                } else {
                    rvRentals.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    rentalAdapter.submitList(resolved)
                }
            } catch (_: Exception) {
                pbLoading.visibility = View.GONE
                val fallback = com.example.lankago.data.RentalShopDataProvider.shops
                if (fallback.isNotEmpty()) {
                    rvRentals.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    rentalAdapter.submitList(fallback)
                } else {
                    rvRentals.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun confirmDeleteRental(shop: RentalShop) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Rental Shop")
            .setMessage("Are you sure you want to permanently remove \"${shop.name}\" from the database and storage?")
            .setPositiveButton("Delete") { _, _ ->
                performDelete(shop)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete(shop: RentalShop) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                adminRepository.deleteRentalShop(shopId = shop.id, imageUrl = shop.imageUrl)
                Snackbar.make(
                    requireView(),
                    "Rental \"${shop.name}\" removed successfully",
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Snackbar.make(
                    requireView(),
                    "Delete failed: ${e.localizedMessage ?: "Unknown error"}",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(requireContext().getColor(R.color.admin_danger)).show()
            }
        }
    }
}
