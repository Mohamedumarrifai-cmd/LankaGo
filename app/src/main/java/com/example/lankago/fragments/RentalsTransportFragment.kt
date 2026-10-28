package com.example.lankago.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.adapters.RentalTransportAdapter
import com.example.lankago.data.RentalShop
import com.example.lankago.repository.AdminRepository
import com.example.lankago.utils.safeNavigate
import kotlinx.coroutines.launch
import java.util.Locale


 // Rentals & Transport Fragment:

class RentalsTransportFragment : Fragment() {

    private val adminRepository = AdminRepository()

    private lateinit var chipAll: TextView
    private lateinit var chipScooters: TextView
    private lateinit var chipBikes: TextView
    private lateinit var chipCars: TextView
    private lateinit var chipVans: TextView
    private lateinit var chipTuktuk: TextView
    private lateinit var chipBuses: TextView

    private var currentFilterTag: String = "all"
    private var containerAdminRentals: LinearLayout? = null
    private val adminRentalViews = mutableListOf<Pair<View, String>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.rentals_and_transport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        containerAdminRentals = view.findViewById(R.id.container_admin_rentals)

        setupTopBar(view)
        setupFilterChips(view)
        setupRentalCards(view)
        observeLiveAdminRentals(view)
    }

    private fun setupTopBar(view: View) {
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
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
            filterCardsByTag(view, filterTag)
        }

        chipAll.setOnClickListener { selectChip(chipAll, "all") }
        chipScooters.setOnClickListener { selectChip(chipScooters, "scooter") }
        chipBikes.setOnClickListener { selectChip(chipBikes, "bike") }
        chipCars.setOnClickListener { selectChip(chipCars, "car") }
        chipVans.setOnClickListener { selectChip(chipVans, "van") }
        chipTuktuk.setOnClickListener { selectChip(chipTuktuk, "tuktuk") }
        chipBuses.setOnClickListener { selectChip(chipBuses, "bus") }
    }

    private fun filterCardsByTag(rootView: View, tag: String) {
        val cardIds = listOf(
            R.id.card_epic_ride,
            R.id.card_colombo_scooter,
            R.id.card_unawatuna,
            R.id.card_dambulla_scooter,
            R.id.card_hikkaduwa_scooter,
            R.id.card_kandy_bike,
            R.id.card_mirissa_bike,
            R.id.card_ella_bike,
            R.id.card_arugam_bike,
            R.id.card_malkey_car,
            R.id.card_casons_car,
            R.id.card_kings_car,
            R.id.card_euro_car,
            R.id.card_casons_van,
            R.id.card_malkey_van,
            R.id.card_kandy_van,
            R.id.card_negombo_tuktuk,
            R.id.card_galle_tuktuk,
            R.id.card_colombo_tuktuk,
            R.id.card_lanka_bus
        )

        for (cardId in cardIds) {
            val card = rootView.findViewById<CardView>(cardId) ?: continue
            val cardTag = card.tag?.toString() ?: ""
            if (tag == "all" || cardTag.equals(tag, ignoreCase = true) ||
                (tag == "bike" && (cardTag == "scooter" || cardTag == "bike"))
            ) {
                card.visibility = View.VISIBLE
            } else {
                card.visibility = View.GONE
            }
        }

        // Also filter dynamically added admin cards
        for ((cardView, adminTag) in adminRentalViews) {
            if (tag == "all" || adminTag.equals(tag, ignoreCase = true) ||
                (tag == "bike" && (adminTag == "scooter" || adminTag == "bike"))
            ) {
                cardView.visibility = View.VISIBLE
            } else {
                cardView.visibility = View.GONE
            }
        }
    }

    private fun openShopFleet(shopId: String, shopName: String, shopCategory: String, location: String) {
        val activeTripId = arguments?.getString("activeTripId") ?: ""
        val isSelectMode = arguments?.getBoolean("isSelectMode") == true ||
                arguments?.getString("mode") == "select_rental"

        val bundle = Bundle().apply {
            putString("shopId", shopId)
            putString("shopName", shopName)
            putString("shopCategory", shopCategory)
            putString("destinationLocation", location)
            putBoolean("isSelectMode", isSelectMode)
            if (activeTripId.isNotBlank()) {
                putString("activeTripId", activeTripId)
            }
        }
        try {
            findNavController().navigate(
                R.id.action_rentalsTransportFragment_to_rentalListFragment,
                bundle
            )
        } catch (_: Exception) {
            findNavController().navigate(
                R.id.action_rentalsTransportFragment_to_rentalDetailsFragment,
                bundle
            )
        }
    }

    private fun setupRentalCards(view: View) {
        // 1. Epic Ride Scooter Rental (Galle Fort) - Bike & Scooter Fleet
        view.findViewById<CardView>(R.id.card_epic_ride)?.setOnClickListener {
            openShopFleet("shop_epic_ride", "Epic Ride Scooter Rental", "bike", "Galle Fort")
        }

        // 2. Colombo Scooter Rentals (Colombo 03) - City Scooters
        view.findViewById<CardView>(R.id.card_colombo_scooter)?.setOnClickListener {
            openShopFleet("shop_colombo_scooter", "Colombo Scooter Rentals", "scooter", "Colombo 03")
        }

        // 3. Unawatuna Scooter Rental (Unawatuna)
        view.findViewById<CardView>(R.id.card_unawatuna)?.setOnClickListener {
            openShopFleet("shop_unawatuna", "Unawatuna Scooter Rental", "scooter", "Unawatuna")
        }

        // 4. Dambulla Scooter Rentals (Dambulla)
        view.findViewById<CardView>(R.id.card_dambulla_scooter)?.setOnClickListener {
            openShopFleet("shop_dambulla_scooter", "Dambulla Scooter Rentals", "scooter", "Dambulla")
        }

        // 5. Hikkaduwa Scooter Rental (Hikkaduwa)
        view.findViewById<CardView>(R.id.card_hikkaduwa_scooter)?.setOnClickListener {
            openShopFleet("shop_hikkaduwa", "Hikkaduwa Scooter Rental", "scooter", "Hikkaduwa")
        }

        // 6. Kandy Bike Rental (Kandy) - Mountain & Touring Bikes
        view.findViewById<CardView>(R.id.card_kandy_bike)?.setOnClickListener {
            openShopFleet("shop_kandy_bike", "Kandy Bike Rental", "bike", "Kandy")
        }

        // 7. Mirissa Bike Rental (Mirissa) - Bikes & Tuk-Tuks (Both)
        view.findViewById<CardView>(R.id.card_mirissa_bike)?.setOnClickListener {
            openShopFleet("shop_mirissa_bike", "Mirissa Bike & Tuk-Tuk Rental", "both", "Mirissa")
        }

        // 8. Ella Bike Rental (Ella) - Mountain Adventures
        view.findViewById<CardView>(R.id.card_ella_bike)?.setOnClickListener {
            openShopFleet("shop_ella_bike", "Ella Bike Rental", "bike", "Ella")
        }

        // 9. Arugam Bay Bike Rental (Arugam Bay)
        view.findViewById<CardView>(R.id.card_arugam_bike)?.setOnClickListener {
            openShopFleet("shop_arugam_bike", "Arugam Bay Bike Rental", "bike", "Arugam Bay")
        }

        // 10. Malkey Rent A Car (Colombo) - Full Car Fleet
        view.findViewById<CardView>(R.id.card_malkey_car)?.setOnClickListener {
            openShopFleet("shop_malkey_car", "Malkey Rent A Car", "car", "Colombo & Islandwide")
        }

        // 11. Casons Rent A Car (Airport & City) - Full Car Fleet
        view.findViewById<CardView>(R.id.card_casons_car)?.setOnClickListener {
            openShopFleet("shop_casons_car", "Casons Rent A Car", "car", "Airport & Islandwide")
        }

        // 12. Kings Rent A Car (Colombo 07) - Luxury Cars
        view.findViewById<CardView>(R.id.card_kings_car)?.setOnClickListener {
            openShopFleet("shop_kings_car", "Kings Rent A Car", "car", "Colombo 07")
        }

        // 13. Europcar Sri Lanka - Premium Cars & 4x4
        view.findViewById<CardView>(R.id.card_euro_car)?.setOnClickListener {
            openShopFleet("shop_euro_car", "Europcar Sri Lanka", "car", "Colombo")
        }

        // 14. Casons Vans (Islandwide) - Passenger Vans & KDH
        view.findViewById<CardView>(R.id.card_casons_van)?.setOnClickListener {
            openShopFleet("shop_casons_van", "Casons Vans & Passenger Fleet", "van", "Islandwide")
        }

        // 15. Malkey Vans (Islandwide) - Vans & Safari 4x4
        view.findViewById<CardView>(R.id.card_malkey_van)?.setOnClickListener {
            openShopFleet("shop_malkey_van", "Malkey Vans & Safari Fleet", "van", "Islandwide")
        }

        // 16. Kandy Tours Vans (Kandy) - Hill Country Vans & 4x4
        view.findViewById<CardView>(R.id.card_kandy_van)?.setOnClickListener {
            openShopFleet("shop_kandy_van", "Kandy Tours Vans", "van", "Kandy")
        }

        // 17. Negombo Tuk Tuk Tours (Negombo)
        view.findViewById<CardView>(R.id.card_negombo_tuktuk)?.setOnClickListener {
            openShopFleet("shop_negombo_tuktuk", "Negombo Tuk Tuk Tours", "tuktuk", "Negombo")
        }

        // 18. Galle Tuk Tuk Rental (Galle Fort)
        view.findViewById<CardView>(R.id.card_galle_tuktuk)?.setOnClickListener {
            openShopFleet("shop_galle_tuktuk", "Galle Tuk Tuk Rental", "tuktuk", "Galle Fort")
        }

        // 19. Colombo Tuk Tuk Tours (Colombo) - Tuk-Tuks & City Cars (Both)
        view.findViewById<CardView>(R.id.card_colombo_tuktuk)?.setOnClickListener {
            openShopFleet("shop_colombo_tuktuk", "Colombo Tuk Tuk Tours", "both", "Colombo")
        }

        // 20. Lanka Coach Tours (Colombo) - Coaches & Vans
        view.findViewById<CardView>(R.id.card_lanka_bus)?.setOnClickListener {
            openShopFleet("shop_lanka_bus", "Lanka Coach Tours", "van", "Colombo")
        }
    }

    private fun observeLiveAdminRentals(rootView: View) {
        val container = containerAdminRentals ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminRepository.observeRentals().collect { firebaseShops ->
                    container.removeAllViews()
                    adminRentalViews.clear()

                    val defaultIds = setOf(
                        "shop_epic_ride", "shop_colombo_scooter", "shop_unawatuna", "shop_dambulla_scooter",
                        "shop_hikkaduwa", "shop_kandy_bike", "shop_galle_bike", "shop_ella_moto",
                        "shop_colombo_rentals", "shop_kandy_rides", "shop_mirissa_tuktuk", "shop_arugam_bike",
                        "shop_malkey_car", "shop_casons_car", "shop_kings_car", "shop_euro_car",
                        "shop_casons_van", "shop_malkey_van", "shop_kandy_van", "shop_negombo_tuktuk",
                        "shop_galle_tuktuk", "shop_colombo_tuktuk", "shop_lanka_bus"
                    )

                    val newAdminShops = firebaseShops.filter { !defaultIds.contains(it.id) }
                    for (shop in newAdminShops) {
                        val cardView = layoutInflater.inflate(R.layout.item_rental_transport_card, container, false)
                        val ivThumb = cardView.findViewById<ImageView>(R.id.img_rental_thumb)
                        val tvTitle = cardView.findViewById<TextView>(R.id.tv_rental_title)
                        val tvSubtitle = cardView.findViewById<TextView>(R.id.tv_rental_subtitle)
                        val tvRating = cardView.findViewById<TextView>(R.id.tv_rental_rating)

                        tvTitle.text = shop.name
                        val categoryTag = resolveCategoryTag(shop)
                        val price = shop.priceStartingFrom.ifBlank { "LKR 3,500/day" }
                        val city = shop.city.ifBlank { "Sri Lanka" }
                        tvSubtitle.text = "${categoryTag.replaceFirstChar { it.uppercase() }} · $price · $city"
                        tvRating.text = "★ ${String.format(Locale.US, "%.1f", if (shop.rating > 0f) shop.rating else 4.9f)}"

                        val fallbackRes = RentalTransportAdapter.resolveShopDrawable(shop)
                        if (shop.imageUrl.isNotBlank() && (shop.imageUrl.startsWith("data:image") || shop.imageUrl.startsWith("http"))) {
                            Glide.with(this@RentalsTransportFragment)
                                .load(shop.imageUrl)
                                .placeholder(fallbackRes)
                                .error(fallbackRes)
                                .fitCenter()
                                .into(ivThumb)
                        } else {
                            ivThumb.setImageResource(fallbackRes)
                        }

                        cardView.setOnClickListener {
                            openShopFleet(shop.id, shop.name, categoryTag, shop.city)
                        }

                        adminRentalViews.add(cardView to categoryTag)
                        container.addView(cardView)
                    }

                    filterCardsByTag(rootView, currentFilterTag)
                }
            }
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
