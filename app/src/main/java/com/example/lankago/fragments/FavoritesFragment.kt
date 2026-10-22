package com.example.lankago.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.adapters.PlaceAdapter
import com.example.lankago.utils.FavoritesManager
import com.example.lankago.utils.LankaGoDataManager
import com.example.lankago.utils.Place
import com.example.lankago.utils.safeNavigate

class FavoritesFragment : Fragment() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var btnBack: ImageView
    private lateinit var btnExplore: AppCompatButton
    private lateinit var adapter: PlaceAdapter
    private val favoritePlaces = mutableListOf<Place>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFavorites = view.findViewById(R.id.rv_favorites)
        layoutEmpty = view.findViewById(R.id.layout_empty_favorites)
        btnBack = view.findViewById(R.id.btn_back)
        btnExplore = view.findViewById(R.id.btn_explore)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnExplore.setOnClickListener {
            try {
                findNavController().safeNavigate(R.id.action_favoritesFragment_to_discoverFragment)
            } catch (_: Exception) {
                findNavController().safeNavigate(R.id.discoverFragment)
            }
        }

        adapter = PlaceAdapter(
            places = favoritePlaces,
            onPlaceClick = { place ->
                val bundle = Bundle().apply {
                    putString("place_id", place.id)
                }
                findNavController().safeNavigate(
                    R.id.action_favoritesFragment_to_destinationDetailFragment,
                    bundle
                )
            },
            onFavoriteToggled = { place, isFavorite ->
                if (!isFavorite) {
                    favoritePlaces.removeAll { it.id == place.id }
                    adapter.notifyDataSetChanged()
                    updateVisibility()
                }
            }
        )

        rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        rvFavorites.adapter = adapter

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        val context = context ?: return
        val favoriteIds = FavoritesManager.getAllFavorites(context)
        val allPlaces = LankaGoDataManager.getPlaces(context)

        val filtered = allPlaces.filter { favoriteIds.contains(it.id) }

        favoritePlaces.clear()
        favoritePlaces.addAll(filtered)
        adapter.notifyDataSetChanged()

        updateVisibility()
    }

    private fun updateVisibility() {
        if (favoritePlaces.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
        }
    }
}
