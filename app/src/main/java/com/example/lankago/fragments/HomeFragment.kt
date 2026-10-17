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

        // 1. Search (Search Bar input & Bottom Nav Explore)
        view.findViewById<View>(R.id.search_bar)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        view.findViewById<View>(R.id.et_search)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        view.findViewById<View>(R.id.nav_explore)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        // 2. Local Products (Local Malls)
        view.findViewById<View>(R.id.btn_local_products)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_localProductsFragment)
        }

        // 3. Traditional Activities
        view.findViewById<View>(R.id.btn_traditional_activities)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_traditionalActivitiesFragment)
        }

        // 4. Shows & Festivals
        view.findViewById<View>(R.id.btn_shows_festivals)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_showsAndFestivalsFragment)
        }

        // 5. Rentals & Transport
        view.findViewById<View>(R.id.btn_rentals_transport)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rentalsTransportFragment)
        }

        // 6. Notifications (Top Bar)
        view.findViewById<View>(R.id.btn_notifications)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment)
        }

        // 7. Profile (Bottom Nav)
        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        // 8. Trip Planner (Trips Category & Bottom Nav)
        view.findViewById<View>(R.id.btn_trip_planner)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_tripPlannerFragment)
        }
        view.findViewById<View>(R.id.nav_trips)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_tripPlannerFragment)
        }

        // 9. Favorites (Category)
        view.findViewById<View>(R.id.btn_favorites)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
        }
    }
}
