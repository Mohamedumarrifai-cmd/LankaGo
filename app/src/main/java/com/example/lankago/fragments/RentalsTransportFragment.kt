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

/**
 * Rentals & Transport Fragment:
 * - Restores the exact, complete, high-fidelity XML layout with authentic rental shops.
 * - Interactive category chips ("All", "Scooters", "Bikes", "Cars", "Vans", "Tuk Tuk", "Buses").
 * - Dynamic container for real-time Firebase Admin added rentals.
 * - Navigates to ShopDetailsFragment / RentalDetailsFragment on item click.
 */
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

    private fun setupRentalCards(view: View) {
        val activeTripId = arguments?.getString("activeTripId") ?: ""
        val isSelectMode = arguments?.getBoolean("isSelectMode") == true ||
                arguments?.getString("mode") == "select_rental"

        fun navigateToDetails(shopId: String, rentalId: String, name: String, category: String, city: String) {
            val bundle = bundleOf(
                "shopId" to shopId,
                "rentalId" to rentalId,
                "shopName" to name,
                "shopCategory" to category,
                "destinationLocation" to city,
                "isSelectMode" to isSelectMode,
                "mode" to if (isSelectMode) "select_rental" else "",
                "activeTripId" to activeTripId
            )
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

        // 1. Epic Ride Scooter Rental (Galle Fort)
        view.findViewById<CardView>(R.id.card_epic_ride)?.setOnClickListener {
            navigateToDetails("shop_epic_ride", "rental_honda_pcx", "Epic Ride Scooter Rental", "scooter", "Galle Fort")
        }

        // 2. Colombo Scooter Rentals (Colombo 03)
        view.findViewById<CardView>(R.id.card_colombo_scooter)?.setOnClickListener {
            navigateToDetails("shop_colombo_scooter", "rental_honda_pcx", "Colombo Scooter Rentals", "scooter", "Colombo 03")
        }

        // 3. Unawatuna Scooter Rental (Unawatuna)
        view.findViewById<CardView>(R.id.card_unawatuna)?.setOnClickListener {
            navigateToDetails("shop_unawatuna", "rental_honda_pcx", "Unawatuna Scooter Rental", "scooter", "Unawatuna")
        }

        // 4. Dambulla Scooter Rentals (Dambulla)
        view.findViewById<CardView>(R.id.card_dambulla_scooter)?.setOnClickListener {
            navigateToDetails("shop_dambulla_scooter", "rental_honda_pcx", "Dambulla Scooter Rentals", "scooter", "Dambulla")
        }

        // 5. Hikkaduwa Scooter Rental (Hikkaduwa)
        view.findViewById<CardView>(R.id.card_hikkaduwa_scooter)?.setOnClickListener {
            navigateToDetails("shop_hikkaduwa", "rental_honda_pcx", "Hikkaduwa Scooter Rental", "scooter", "Hikkaduwa")
        }

        // 6. Kandy Bike Rental (Kandy)
        view.findViewById<CardView>(R.id.card_kandy_bike)?.setOnClickListener {
            navigateToDetails("shop_kandy_bike", "rental_enfield_kandy", "Kandy Bike Rental", "bike", "Kandy")
        }

        // 7. Mirissa Bike Rental
        view.findViewById<CardView>(R.id.card_mirissa_bike)?.setOnClickListener {
            navigateToDetails("shop_mirissa_tuktuk", "rental_tuktuk_mirissa", "Mirissa Coastline Tuk-Tuk & Bike Rentals", "tuktuk", "Mirissa")
        }

        // 8. Ella Bike Rental
        view.findViewById<CardView>(R.id.card_ella_bike)?.setOnClickListener {
            navigateToDetails("shop_ella_moto", "rental_enfield_kandy", "Ella Mountain Moto Adventures", "bike", "Ella")
        }

        // 9. Arugam Bay Bike Rental
        view.findViewById<CardView>(R.id.card_arugam_bike)?.setOnClickListener {
            navigateToDetails("shop_arugam_bike", "rental_enfield_kandy", "Arugam Bay Surf & Bike Rentals", "bike", "Arugam Bay")
        }

        // 10. Malkey Car
        view.findViewById<CardView>(R.id.card_malkey_car)?.setOnClickListener {
            navigateToDetails("shop_malkey_car", "rental_toyota_aqua", "Malkey Rent A Car", "car", "Islandwide")
        }

        // 11. Casons Car
        view.findViewById<CardView>(R.id.card_casons_car)?.setOnClickListener {
            navigateToDetails("shop_casons_car", "rental_toyota_aqua", "Casons Rent A Car", "car", "Airport & Islandwide")
        }

        // 12. Kings Car
        view.findViewById<CardView>(R.id.card_kings_car)?.setOnClickListener {
            navigateToDetails("shop_kings_car", "rental_toyota_aqua", "Kings Rent A Car", "car", "Colombo")
        }

        // 13. Euro Car
        view.findViewById<CardView>(R.id.card_euro_car)?.setOnClickListener {
            navigateToDetails("shop_euro_car", "rental_nissan_xtrail", "Europcar Sri Lanka", "car", "Colombo")
        }

        // 14. Casons Van
        view.findViewById<CardView>(R.id.card_casons_van)?.setOnClickListener {
            navigateToDetails("shop_casons_van", "rental_nissan_xtrail", "Casons Vans & Passenger Fleet", "van", "Islandwide")
        }

        // 15. Malkey Van
        view.findViewById<CardView>(R.id.card_malkey_van)?.setOnClickListener {
            navigateToDetails("shop_malkey_van", "rental_nissan_xtrail", "Malkey Vans & Safari Fleet", "van", "Islandwide")
        }

        // 16. Kandy Van
        view.findViewById<CardView>(R.id.card_kandy_van)?.setOnClickListener {
            navigateToDetails("shop_kandy_van", "rental_nissan_xtrail", "Kandy Tours & Vans", "van", "Kandy")
        }

        // 17. Negombo Tuk Tuk
        view.findViewById<CardView>(R.id.card_negombo_tuktuk)?.setOnClickListener {
            navigateToDetails("shop_negombo_tuktuk", "rental_tuktuk_mirissa", "Negombo Tuk Tuk Tours", "tuktuk", "Negombo")
        }

        // 18. Galle Tuk Tuk
        view.findViewById<CardView>(R.id.card_galle_tuktuk)?.setOnClickListener {
            navigateToDetails("shop_galle_tuktuk", "rental_tuktuk_mirissa", "Galle Tuk Tuk Rental", "tuktuk", "Galle Fort")
        }

        // 19. Colombo Tuk Tuk
        view.findViewById<CardView>(R.id.card_colombo_tuktuk)?.setOnClickListener {
            navigateToDetails("shop_colombo_tuktuk", "rental_tuktuk_mirissa", "Colombo Tuk Tuk Tours", "tuktuk", "Colombo")
        }

        // 20. Lanka Bus
        view.findViewById<CardView>(R.id.card_lanka_bus)?.setOnClickListener {
            navigateToDetails("shop_lanka_bus", "rental_nissan_xtrail", "Lanka Coach Tours", "bus", "Colombo")
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
                            val bundle = bundleOf(
                                "shopId" to shop.id,
                                "shopName" to shop.name,
                                "shopCategory" to categoryTag,
                                "destinationLocation" to shop.city
                            )
                            try {
                                findNavController().safeNavigate(R.id.action_rentalsTransportFragment_to_shopDetailsFragment, bundle)
                            } catch (_: Exception) {
                                findNavController().safeNavigate(R.id.action_rentalsTransportFragment_to_rentalDetailsFragment, bundle)
                            }
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
