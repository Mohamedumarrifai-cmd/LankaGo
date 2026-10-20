package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.utils.FavoritesManager
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place

class DestinationDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.destination_detail, container, false)

        // Retrieve placeId passed via Bundle
        val placeId = arguments?.getString("place_id") ?: "place_1"
        val place = LankaGoDataManager.getPlaceById(requireContext(), placeId)

        val ivHero = root.findViewById<ImageView>(R.id.iv_detail_hero)
        val tvTitle = root.findViewById<TextView>(R.id.tv_detail_title)
        val tvLocation = root.findViewById<TextView>(R.id.tv_detail_location)
        val tvRating = root.findViewById<TextView>(R.id.tv_detail_rating)
        val tvCategory = root.findViewById<TextView>(R.id.tv_detail_category)
        val tvDescription = root.findViewById<TextView>(R.id.tv_detail_description)
        val btnBack = root.findViewById<ImageView>(R.id.btn_back)
        val btnFavorite = root.findViewById<ImageView>(R.id.btn_favorite)

        // Bind data
        place?.let {
            tvTitle?.text = it.name
            tvLocation?.text = it.location
            tvRating?.text = "★ ${it.rating}"
            tvCategory?.text = it.category
            tvDescription?.text = it.description
        }

        // Back button navigation
        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Favorite Toggle
        var isFav = FavoritesManager.isFavorite(requireContext(), placeId)
        updateFavoriteIcon(btnFavorite, isFav)

        btnFavorite?.setOnClickListener {
            isFav = !isFav
            if (isFav) {
                FavoritesManager.addFavorite(requireContext(), placeId)
            } else {
                FavoritesManager.removeFavorite(requireContext(), placeId)
            }
            updateFavoriteIcon(btnFavorite, isFav)
        }

        // Add to Trip Navigation
        root.findViewById<View>(R.id.btn_add_to_trip)?.setOnClickListener {
            findNavController().navigate(R.id.action_destinationDetailFragment_to_planTripFragment)
        }

        // View Route on Map Navigation
        root.findViewById<View>(R.id.btn_view_map)?.setOnClickListener {
            findNavController().navigate(R.id.action_destinationDetailFragment_to_mapRouteFragment)
        }

        return root
    }

    private fun updateFavoriteIcon(imageView: ImageView?, isFavorite: Boolean) {
        if (isFavorite) {
            imageView?.setImageResource(R.drawable.ic_heart_filled)
        } else {
            imageView?.setImageResource(R.drawable.ic_heart_outline)
        }
    }
}
