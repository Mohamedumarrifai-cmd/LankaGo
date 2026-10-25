package com.example.lankago.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

/**
 * Fragment rendering the Rentals & Transport showcase screen matching rentals_and_transport.xml.
 * Features:
 * - Back button to return to Home
 * - Category filter chips ("All", "Scooters", "Bikes", "Cars", "Vans", "Tuk Tuk", "Buses")
 * - Direct click navigation from every rental shop card to ShopDetailsFragment with full details
 */
class RentalsTransportFragment : Fragment() {

    private lateinit var chipAll: TextView
    private lateinit var chipScooters: TextView
    private lateinit var chipBikes: TextView
    private lateinit var chipCars: TextView
    private lateinit var chipVans: TextView
    private lateinit var chipTuktuk: TextView
    private lateinit var chipBuses: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.rentals_and_transport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopBar(view)
        setupFilterChips(view)
        setupRentalCards(view)
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
            if (tag == "all" || cardTag.equals(tag, ignoreCase = true)) {
                card.visibility = View.VISIBLE
            } else {
                card.visibility = View.GONE
            }
        }
    }

    private fun setupRentalCards(view: View) {
        val activeTripId = arguments?.getString("activeTripId") ?: ""
        val isSelectMode = arguments?.getBoolean("isSelectMode") == true ||
            arguments?.getString("mode") == "select_rental"

        fun selectVehicle(rentalId: String, shopId: String) {
            val bundle = Bundle().apply {
                putString("rentalId", rentalId)
                putString("shopId", shopId)
                putBoolean("isSelectMode", isSelectMode)
                if (activeTripId.isNotBlank()) {
                    putString("activeTripId", activeTripId)
                }
            }
            try {
                findNavController().navigate(
                    R.id.action_rentalsTransportFragment_to_rentalDetailsFragment,
                    bundle
                )
            } catch (_: Exception) {
                findNavController().navigate(
                    R.id.action_rentalsTransportFragment_to_shopDetailsFragment,
                    bundle
                )
            }
        }

        // 0. Browse All Fleet Vehicles Banner
        view.findViewById<View>(R.id.card_view_all_vehicles)?.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_rentalsTransportFragment_to_rentalListFragment)
            } catch (_: Exception) {
                // Resilient fallback
            }
        }

        // 1. Honda Dio 110cc (Galle Fort)
        view.findViewById<CardView>(R.id.card_epic_ride)?.setOnClickListener {
            selectVehicle("rental_honda_dio", "shop_epic_ride")
        }

        // 2. Honda PCX 150 (Colombo 03)
        view.findViewById<CardView>(R.id.card_colombo_scooter)?.setOnClickListener {
            selectVehicle("rental_honda_pcx", "shop_colombo_scooter")
        }

        // 3. Yamaha NMAX 155cc (Unawatuna)
        view.findViewById<CardView>(R.id.card_unawatuna)?.setOnClickListener {
            selectVehicle("rental_yamaha_nmax", "shop_unawatuna")
        }

        // 4. Yamaha FZ-S 150cc (Dambulla)
        view.findViewById<CardView>(R.id.card_dambulla_scooter)?.setOnClickListener {
            selectVehicle("rental_yamaha_fzs", "shop_dambulla_scooter")
        }

        // 5. Bajaj Pulsar NS200 (Hikkaduwa)
        view.findViewById<CardView>(R.id.card_hikkaduwa_scooter)?.setOnClickListener {
            selectVehicle("rental_pulsar_ns200", "shop_hikkaduwa")
        }

        // 6. Royal Enfield Himalayan 411cc (Kandy)
        view.findViewById<CardView>(R.id.card_kandy_bike)?.setOnClickListener {
            selectVehicle("rental_enfield_kandy", "shop_kandy_bike")
        }

        // 7. Sri Lankan Tourist Tuk-Tuk (Mirissa)
        view.findViewById<CardView>(R.id.card_mirissa_bike)?.setOnClickListener {
            selectVehicle("rental_tuktuk_mirissa", "shop_mirissa_tuktuk")
        }

        // 8. Royal Enfield Mountain Adventure (Ella)
        view.findViewById<CardView>(R.id.card_ella_bike)?.setOnClickListener {
            selectVehicle("rental_enfield_kandy", "shop_ella_moto")
        }

        // 9. Yamaha FZ Beach Cruiser (Arugam Bay)
        view.findViewById<CardView>(R.id.card_arugam_bike)?.setOnClickListener {
            selectVehicle("rental_yamaha_fzs", "shop_galle_bike")
        }

        // 10. Suzuki Alto 800 Economy (Colombo)
        view.findViewById<CardView>(R.id.card_malkey_car)?.setOnClickListener {
            selectVehicle("rental_suzuki_alto", "shop_colombo_rentals")
        }

        // 11. Suzuki Wagon R FX Hybrid (Airport & City)
        view.findViewById<CardView>(R.id.card_casons_car)?.setOnClickListener {
            selectVehicle("rental_suzuki_wagonr", "shop_colombo_rentals")
        }

        // 12. Toyota Aqua Hybrid (Colombo 07)
        view.findViewById<CardView>(R.id.card_kings_car)?.setOnClickListener {
            selectVehicle("rental_toyota_aqua", "shop_colombo_rentals")
        }

        // 13. Toyota Prius Luxury Hybrid (Colombo Hub)
        view.findViewById<CardView>(R.id.card_euro_car)?.setOnClickListener {
            selectVehicle("rental_toyota_prius", "shop_colombo_rentals")
        }

        // 14. Toyota KDH Commuter High-Roof (Colombo)
        view.findViewById<CardView>(R.id.card_casons_van)?.setOnClickListener {
            selectVehicle("rental_toyota_kdh", "shop_colombo_rentals")
        }

        // 15. Nissan X-Trail 4WD Safari Edition (Colombo)
        view.findViewById<CardView>(R.id.card_malkey_van)?.setOnClickListener {
            selectVehicle("rental_nissan_xtrail", "shop_colombo_rentals")
        }

        // 16. Mitsubishi Montero 4x4 Expedition (Kandy)
        view.findViewById<CardView>(R.id.card_kandy_van)?.setOnClickListener {
            selectVehicle("rental_mitsubishi_montero", "shop_kandy_rides")
        }

        // 17. Negombo Tourist Tuk-Tuk
        view.findViewById<CardView>(R.id.card_negombo_tuktuk)?.setOnClickListener {
            selectVehicle("rental_tuktuk_mirissa", "shop_mirissa_tuktuk")
        }

        // 18. Galle Heritage Coastal Tuk-Tuk
        view.findViewById<CardView>(R.id.card_galle_tuktuk)?.setOnClickListener {
            selectVehicle("rental_tuktuk_galle", "shop_galle_bike")
        }

        // 19. Honda Grace Hybrid Sedan
        view.findViewById<CardView>(R.id.card_colombo_tuktuk)?.setOnClickListener {
            selectVehicle("rental_honda_grace", "shop_colombo_rentals")
        }

        // 20. Toyota KDH VIP Van
        view.findViewById<CardView>(R.id.card_lanka_bus)?.setOnClickListener {
            selectVehicle("rental_toyota_kdh", "shop_colombo_rentals")
        }
    }
}
