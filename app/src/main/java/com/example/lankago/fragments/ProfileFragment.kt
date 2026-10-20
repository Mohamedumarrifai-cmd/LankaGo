package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Favorite Places
        view.findViewById<View>(R.id.layout_profile_favorites)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }

        // Vehicle & Bike Rentals
        view.findViewById<View>(R.id.layout_profile_rentals)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_rentalListFragment)
        }

        // Offline Saved
        view.findViewById<View>(R.id.layout_profile_offline)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_offlineSavedFragment)
        }

        // Community Reviews
        view.findViewById<View>(R.id.layout_profile_reviews)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_reviewsFragment)
        }

        // Notifications
        view.findViewById<View>(R.id.layout_profile_notifications)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_notificationsFragment)
        }

        // Settings
        view.findViewById<View>(R.id.layout_profile_settings)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        // Logout
        view.findViewById<View>(R.id.btn_profile_logout)?.setOnClickListener {
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}
