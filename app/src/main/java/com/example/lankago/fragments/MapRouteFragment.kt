package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R

class MapRouteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Top Back Button
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        setupBottomNavigation(view)
    }

    private fun setupBottomNavigation(view: View) {
        // 1. Home
        view.findViewById<View>(R.id.nav_home)?.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        // 2. Products
        view.findViewById<View>(R.id.nav_products)?.setOnClickListener {
            findNavController().navigate(R.id.localProductsFragment)
        }

        // 3. Maps (Already on this screen)
        view.findViewById<View>(R.id.nav_maps)?.setOnClickListener {
            // Already on MapRouteFragment
        }

        // 4. Profile
        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }
}
