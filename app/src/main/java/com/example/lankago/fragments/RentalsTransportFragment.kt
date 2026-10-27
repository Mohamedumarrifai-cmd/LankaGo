package com.example.lankago.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.RentalTransportAdapter
import com.example.lankago.data.RentalShop
import com.example.lankago.data.RentalShopDataProvider
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.safeNavigate
import kotlinx.coroutines.launch

/**
 * Dynamic Fragment rendering the Rentals & Transport showcase screen.
 * Live-synced with Firebase Realtime Database `/rentals` node:
 * - Newly added admin rental shops appear immediately at the top of the list.
 * - Exact visual style with left square photo, title, category/price subtitle, rating, and chevron.
 * - Interactive filter chips ("All", "Scooters", "Bikes", "Cars", "Vans", "Tuk Tuk", "Buses").
 * - Click navigation to ShopDetailsFragment / RentalDetailsFragment.
 */
class RentalsTransportFragment : Fragment() {

    private val adminRepository = AdminRepository()
    private lateinit var adapter: RentalTransportAdapter

    private lateinit var chipAll: TextView
    private lateinit var chipScooters: TextView
    private lateinit var chipBikes: TextView
    private lateinit var chipCars: TextView
    private lateinit var chipVans: TextView
    private lateinit var chipTuktuk: TextView
    private lateinit var chipBuses: TextView

    private lateinit var rvRentals: RecyclerView
    private lateinit var layoutEmpty: View

    private var allRentalShops: List<RentalShop> = emptyList()
    private var currentFilterTag: String = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.rentals_and_transport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvRentals = view.findViewById(R.id.rv_rentals_transport)
        layoutEmpty = view.findViewById(R.id.layout_empty_rentals)

        setupTopBar(view)
        setupRecyclerView()
        setupFilterChips(view)
        observeLiveRentals()
    }

    private fun setupTopBar(view: View) {
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        val activeTripId = arguments?.getString("activeTripId") ?: ""
        val isSelectMode = arguments?.getBoolean("isSelectMode") == true ||
                arguments?.getString("mode") == "select_rental"

        adapter = RentalTransportAdapter { shop ->
            val bundle = bundleOf(
                "shopId" to shop.id,
                "shopName" to shop.name,
                "shopCategory" to resolveCategoryTag(shop),
                "destinationLocation" to shop.city,
                "isSelectMode" to isSelectMode,
                "mode" to if (isSelectMode) "select_rental" else "",
                "activeTripId" to activeTripId
            )

            // Safely navigate to shop details
            try {
                findNavController().safeNavigate(
                    R.id.action_rentalsTransportFragment_to_shopDetailsFragment,
                    bundle
                )
            } catch (_: Exception) {
                findNavController().safeNavigate(
                    R.id.action_rentalsTransportFragment_to_rentalDetailsFragment,
                    bundle
                )
            }
        }

        rvRentals.layoutManager = LinearLayoutManager(requireContext())
        rvRentals.adapter = adapter
    }

    private fun observeLiveRentals() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminRepository.observeRentals().collect { firebaseShops ->
                    // Place all admin-added Firebase shops at the top, then default shops
                    val combined = firebaseShops.toMutableList()
                    val firebaseIds = firebaseShops.map { it.id }.toSet()
                    for (defaultShop in RentalShopDataProvider.getAllShops()) {
                        if (!firebaseIds.contains(defaultShop.id)) {
                            combined.add(defaultShop)
                        }
                    }

                    allRentalShops = combined
                    filterList(currentFilterTag)
                }
            }
        }
    }

    private fun setupFilterChips(view: View) {
        chipAll = view.findViewById(R.id.chip_all)
        chipScooters = view.findViewById(R.id.chip_scooters)
        chipBikes = view.findViewById(R.id.chip_bikes)
        chipCars = view.findViewById(R.id.chip_cars)
        chipVans = view.findViewById(R.id.chip_vans)
        chipTuktuk = view.findViewById(R.id.chip_tuktuk)
        chipBuses = view.findViewById(R.id.chip_buses)

        val chips = listOf(chipAll, chipScooters, chipBikes, chipCars, chipVans, chipTuktuk, chipBuses)

        fun selectChip(selectedChip: TextView, filterTag: String) {
            currentFilterTag = filterTag
            for (chip in chips) {
                if (chip == selectedChip) {
                    chip.setBackgroundResource(R.drawable.chip_active)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    chip.setTypeface(null, Typeface.BOLD)
                } else {
                    chip.setBackgroundResource(R.drawable.chip_inactive)
                    chip.setTextColor(0xFF1A2B4C.toInt())
                    chip.setTypeface(null, Typeface.NORMAL)
                }
            }
            filterList(filterTag)
        }

        chipAll.setOnClickListener { selectChip(chipAll, "all") }
        chipScooters.setOnClickListener { selectChip(chipScooters, "scooter") }
        chipBikes.setOnClickListener { selectChip(chipBikes, "bike") }
        chipCars.setOnClickListener { selectChip(chipCars, "car") }
        chipVans.setOnClickListener { selectChip(chipVans, "van") }
        chipTuktuk.setOnClickListener { selectChip(chipTuktuk, "tuktuk") }
        chipBuses.setOnClickListener { selectChip(chipBuses, "bus") }
    }

    private fun filterList(tag: String) {
        val filtered = if (tag == "all") {
            allRentalShops
        } else {
            allRentalShops.filter { shop ->
                val categoryTag = resolveCategoryTag(shop)
                categoryTag.equals(tag, ignoreCase = true) ||
                        (tag == "bike" && (categoryTag == "scooter" || categoryTag == "bike"))
            }
        }

        if (filtered.isEmpty()) {
            rvRentals.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvRentals.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            adapter.submitList(filtered)
        }
    }

    private fun resolveCategoryTag(shop: RentalShop): String {
        val combined = "${shop.name} ${shop.description} ${shop.fleetAvailable.joinToString(" ")}".lowercase()
        return when {
            combined.contains("scooter") -> "scooter"
            combined.contains("bike") || combined.contains("moto") -> "bike"
            combined.contains("van") || combined.contains("kdh") -> "van"
            combined.contains("tuktuk") || combined.contains("tuk tuk") || combined.contains("tuk-tuk") -> "tuktuk"
            combined.contains("bus") || combined.contains("coach") -> "bus"
            combined.contains("car") || combined.contains("suv") || combined.contains("sedan") -> "car"
            else -> "car"
        }
    }
}
