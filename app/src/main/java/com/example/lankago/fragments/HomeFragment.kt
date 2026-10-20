package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryButtons(view)
        setupSearchBar(view)
        setupHeroDestination(view)
    }

    private fun setupHeroDestination(view: View) {
        view.findViewById<View>(R.id.card_hero_destination)?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("place_id", "place_1")
            }
            findNavController().navigate(R.id.action_homeFragment_to_destinationDetailFragment, bundle)
        }
    }

    private fun setupCategoryButtons(view: View) {
        // Products Hub (from Category Grid)
        view.findViewById<View>(R.id.btn_products_hub)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_localProductsFragment)
        }

        // Local Products (Malls)
        view.findViewById<View>(R.id.btn_local_products)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_localProductsFragment)
        }

        // Traditional Activities
        view.findViewById<View>(R.id.btn_traditional_activities)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_traditionalActivitiesFragment)
        }

        // Shows & Festivals
        view.findViewById<View>(R.id.btn_shows_festivals)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_showsAndFestivalsFragment)
        }

        // Rentals & Transport Hub
        view.findViewById<View>(R.id.btn_rentals_transport)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rentalShopsListFragment)
        }

        // Trip Planner
        view.findViewById<View>(R.id.btn_trip_planner)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_tripPlannerFragment)
        }

        // Favorites
        view.findViewById<View>(R.id.btn_favorites)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
        }

        // Notifications (Top bar icon)
        view.findViewById<View>(R.id.btn_notifications)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment)
        }
    }

    private fun setupSearchBar(view: View) {
        view.findViewById<View>(R.id.search_bar)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        view.findViewById<View>(R.id.et_search)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }
}
