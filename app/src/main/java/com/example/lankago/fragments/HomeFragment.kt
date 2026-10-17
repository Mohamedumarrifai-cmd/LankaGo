package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Explore (Search)
        view.findViewById<View>(R.id.nav_explore)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
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

        // Rentals & Transport
        view.findViewById<View>(R.id.btn_rentals_transport)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_rentalsTransportFragment)
        }

        // Notifications
        view.findViewById<View>(R.id.btn_notifications)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment)
        }

        // Profile
        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }
}
