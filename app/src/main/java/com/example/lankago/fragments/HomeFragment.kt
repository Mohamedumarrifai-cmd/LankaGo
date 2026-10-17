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

        setupBottomNavigation(view)
        setupCategoryButtons(view)
        setupSearchBar(view)
    }

    private fun setupBottomNavigation(view: View) {
        // 1. Home Button (Already on Home)
        view.findViewById<View>(R.id.nav_home)?.setOnClickListener {
            // Already on home screen
        }

        // 2. Products Button -> Navigate to LocalProductsFragment
        view.findViewById<View>(R.id.nav_products)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_localProductsFragment)
        }

        // 3. Maps Button -> Navigate to MapRouteFragment
        view.findViewById<View>(R.id.nav_maps)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_mapRouteFragment)
        }

        // 4. Profile Button -> Navigate to ProfileFragment
        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun setupCategoryButtons(view: View) {
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

        // Rentals & Transport
        view.findViewById<View>(R.id.btn_rentals_transport)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rentalsTransportFragment)
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
