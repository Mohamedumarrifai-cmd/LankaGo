package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.PlaceAdapter
import com.example.lankago.utils.FavoritesManager
import com.example.lankago.utils.LankaGoDataManager

class FavoritesFragment : Fragment() {

    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var rvFavorites: RecyclerView
    private lateinit var layoutEmptyState: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        // Refresh list when returning from DestinationDetail
        loadFavorites()
    }

    private fun initViews(view: View) {
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.btn_explore)?.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        rvFavorites = view.findViewById(R.id.rv_favorites)
        layoutEmptyState = view.findViewById(R.id.layout_empty_favorites)
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceAdapter(
            places = mutableListOf(),
            onPlaceClick = { place ->
                val bundle = bundleOf(
                    "place_id" to place.id,
                    "place_name" to place.name,
                    "place_category" to place.category,
                    "place_location" to place.location,
                    "place_image_uri" to place.imageUri,
                    "place_description" to place.description,
                    "place_rating" to place.rating,
                    "place_entrance_fee" to place.entranceFee
                )
                findNavController().navigate(R.id.destinationDetailFragment, bundle)
            },
            onFavoriteToggled = { _, isFav ->
                if (!isFav) {
                    // Item was un-favorited, refresh the wishlist
                    loadFavorites()
                }
            }
        )

        rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = placeAdapter
        }
    }

    private fun loadFavorites() {
        val favoriteIds = FavoritesManager.getAllFavorites(requireContext())
        val favoritePlaces = LankaGoDataManager.placesList.filter { place ->
            favoriteIds.contains(place.id)
        }

        placeAdapter.updateData(favoritePlaces)

        if (favoritePlaces.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
        }
    }
}
