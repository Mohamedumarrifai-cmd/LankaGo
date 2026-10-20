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
        fun openShop(shopId: String) {
            val bundle = bundleOf("shopId" to shopId)
            findNavController().navigate(
                R.id.action_rentalsTransportFragment_to_shopDetailsFragment,
                bundle
            )
        }

        // 1. Epic Ride Scooter Rental (Galle Fort)
        view.findViewById<CardView>(R.id.card_epic_ride)?.setOnClickListener {
            openShop("shop_epic_ride")
        }

        // 2. Colombo Scooter Rentals (Colombo 03)
        view.findViewById<CardView>(R.id.card_colombo_scooter)?.setOnClickListener {
            openShop("shop_colombo_scooter")
        }

        // 3. Unawatuna Scooter Rental (Unawatuna)
        view.findViewById<CardView>(R.id.card_unawatuna)?.setOnClickListener {
            openShop("shop_unawatuna")
        }

        // 4. Dambulla Scooter Rentals (Dambulla)
        view.findViewById<CardView>(R.id.card_dambulla_scooter)?.setOnClickListener {
            openShop("shop_dambulla_scooter")
        }

        // 5. Hikkaduwa Scooter Rental (Hikkaduwa)
        view.findViewById<CardView>(R.id.card_hikkaduwa_scooter)?.setOnClickListener {
            openShop("shop_hikkaduwa")
        }

        // 6. Kandy Bike Rental (Kandy)
        view.findViewById<CardView>(R.id.card_kandy_bike)?.setOnClickListener {
            openShop("shop_kandy_bike")
        }

        // 7. Mirissa Bike Rental
        view.findViewById<CardView>(R.id.card_mirissa_bike)?.setOnClickListener {
            openShop("shop_mirissa_tuktuk")
        }

        // 8. Ella Bike Rental
        view.findViewById<CardView>(R.id.card_ella_bike)?.setOnClickListener {
            openShop("shop_ella_moto")
        }

        // 9. Arugam Bay Bike Rental
        view.findViewById<CardView>(R.id.card_arugam_bike)?.setOnClickListener {
            openShop("shop_galle_bike")
        }

        // 10-13. Cars
        view.findViewById<CardView>(R.id.card_malkey_car)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }
        view.findViewById<CardView>(R.id.card_casons_car)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }
        view.findViewById<CardView>(R.id.card_kings_car)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }
        view.findViewById<CardView>(R.id.card_euro_car)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }

        // 14-16. Vans
        view.findViewById<CardView>(R.id.card_casons_van)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }
        view.findViewById<CardView>(R.id.card_malkey_van)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }
        view.findViewById<CardView>(R.id.card_kandy_van)?.setOnClickListener {
            openShop("shop_kandy_rides")
        }

        // 17-19. Tuk Tuks
        view.findViewById<CardView>(R.id.card_negombo_tuktuk)?.setOnClickListener {
            openShop("shop_mirissa_tuktuk")
        }
        view.findViewById<CardView>(R.id.card_galle_tuktuk)?.setOnClickListener {
            openShop("shop_galle_bike")
        }
        view.findViewById<CardView>(R.id.card_colombo_tuktuk)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }

        // 20. Buses
        view.findViewById<CardView>(R.id.card_lanka_bus)?.setOnClickListener {
            openShop("shop_colombo_rentals")
        }
    }
}
