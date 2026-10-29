package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.RentalListAdapter
import com.example.lankago.data.Vehicle
import com.example.lankago.repository.RentalRepository
import com.google.android.material.chip.ChipGroup

class RentalListFragment : Fragment() {

    private lateinit var rentalAdapter: RentalListAdapter
    private var allVehicles: List<Vehicle> = emptyList()
    private var destinationLocation: String = "Sri Lanka"
    private var shopId: String? = null
    private var shopName: String? = null
    private var shopCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rental_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopId = arguments?.getString("shopId")
        shopName = arguments?.getString("shopName")
        shopCategory = arguments?.getString("shopCategory")
        destinationLocation = arguments?.getString("destinationCity")
            ?: arguments?.getString("destinationLocation")
            ?: "Sri Lanka"

        initViews(view)
        loadVehicles(view)
    }

    private fun initViews(view: View) {
        view.findViewById<ImageButton>(R.id.btn_back_rental_list)?.setOnClickListener {
            findNavController().navigateUp()
        }

        val tvTitle = view.findViewById<TextView>(R.id.tv_rental_list_title)
        val tvSub = view.findViewById<TextView>(R.id.tv_rental_list_destination)

        if (!shopName.isNullOrBlank()) {
            tvTitle?.text = shopName
            tvSub?.text = "Verified Fleet • $destinationLocation"
        } else {
            tvTitle?.text = "Vehicle & Bike Rentals"
            tvSub?.text = "Available verified fleet in $destinationLocation"
        }

        val rvRentals = view.findViewById<RecyclerView>(R.id.rv_rentals_list)
        val activeTripId = arguments?.getString("activeTripId") ?: ""
        val isSelectMode = arguments?.getBoolean("isSelectMode") == true ||
            arguments?.getString("mode") == "select_rental"

        rentalAdapter = RentalListAdapter(emptyList()) { selectedVehicle ->
            val bundle = bundleOf(
                "rentalId" to selectedVehicle.id,
                "vehicleId" to selectedVehicle.id,
                "isSelectMode" to isSelectMode,
                "mode" to if (isSelectMode) "select_rental" else "",
                "activeTripId" to activeTripId
            )
            findNavController().navigate(
                R.id.action_rentalListFragment_to_rentalDetailsFragment,
                bundle
            )
        }

        rvRentals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rentalAdapter
        }

        // Setup Category Filter Chips
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_rental_filters)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chip_filter_all
            filterVehicles(checkedId, view)
        }
    }

    private fun loadVehicles(view: View) {
        allVehicles = RentalRepository.getVehiclesForShop(shopId, shopCategory, destinationLocation)
        rentalAdapter.updateData(allVehicles)
        toggleEmptyState(view, allVehicles.isEmpty())
    }

    private fun filterVehicles(checkedId: Int, view: View) {
        val filtered = when (checkedId) {
            R.id.chip_filter_bikes -> allVehicles.filter {
                it.type.contains("Bike", ignoreCase = true) ||
                it.type.contains("Scooter", ignoreCase = true) ||
                it.type.contains("Motorcycle", ignoreCase = true)
            }
            R.id.chip_filter_tuktuk -> allVehicles.filter {
                it.type.contains("Tuk", ignoreCase = true)
            }
            R.id.chip_filter_cars -> allVehicles.filter {
                it.type.contains("Car", ignoreCase = true) ||
                it.type.contains("SUV", ignoreCase = true) ||
                it.type.contains("4x4", ignoreCase = true) ||
                it.type.contains("Van", ignoreCase = true) ||
                it.type.contains("Sedan", ignoreCase = true) ||
                it.type.contains("Hatchback", ignoreCase = true)
            }
            else -> allVehicles
        }

        rentalAdapter.updateData(filtered)
        toggleEmptyState(view, filtered.isEmpty())
    }

    private fun toggleEmptyState(view: View, isEmpty: Boolean) {
        val layoutEmpty = view.findViewById<View>(R.id.layout_empty_rentals)
        val rvRentals = view.findViewById<View>(R.id.rv_rentals_list)

        if (isEmpty) {
            layoutEmpty?.visibility = View.VISIBLE
            rvRentals?.visibility = View.GONE
        } else {
            layoutEmpty?.visibility = View.GONE
            rvRentals?.visibility = View.VISIBLE
        }
    }
}
