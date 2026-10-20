package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class RentalsTransportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.rentals_and_transport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Navigation
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Navigate to Rental Details Screen for featured cards
        fun navigateToRentalDetails(rentalId: String) {
            val bundle = Bundle().apply {
                putString("rentalId", rentalId)
            }
            findNavController().navigate(
                R.id.action_rentalsTransportFragment_to_rentalDetailsFragment,
                bundle
            )
        }

        view.findViewById<CardView>(R.id.card_ella_bike)?.setOnClickListener {
            navigateToRentalDetails("rental_scooter_ella")
        }

        view.findViewById<CardView>(R.id.card_colombo_scooter)?.setOnClickListener {
            navigateToRentalDetails("rental_scooter_ella")
        }

        view.findViewById<CardView>(R.id.card_negombo_tuktuk)?.setOnClickListener {
            navigateToRentalDetails("rental_tuktuk_mirissa")
        }

        view.findViewById<CardView>(R.id.card_galle_tuktuk)?.setOnClickListener {
            navigateToRentalDetails("rental_tuktuk_mirissa")
        }

        view.findViewById<CardView>(R.id.card_kandy_bike)?.setOnClickListener {
            navigateToRentalDetails("rental_enfield_kandy")
        }

        view.findViewById<CardView>(R.id.card_malkey_car)?.setOnClickListener {
            navigateToRentalDetails("rental_prado_yala")
        }

        // Bottom Navigation Bar
        view.findViewById<View>(R.id.nav_home)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<View>(R.id.nav_products)?.setOnClickListener {
            findNavController().navigate(R.id.localProductsFragment)
        }

        view.findViewById<View>(R.id.nav_maps)?.setOnClickListener {
            findNavController().navigate(R.id.mapRouteFragment)
        }

        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }
}
