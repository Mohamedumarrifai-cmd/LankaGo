package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        // Bottom Navigation Bar
        view.findViewById<View>(R.id.nav_home)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<View>(R.id.nav_explore)?.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        view.findViewById<View>(R.id.nav_trips)?.setOnClickListener {
            findNavController().navigate(R.id.tripPlannerFragment)
        }
    }
}
