package com.example.lankago.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lankago.R
import com.example.lankago.utils.FavoritesManager
import com.example.lankago.utils.LankaGoDataManager

class DestinationDetailFragment : Fragment() {

    private var currentPlaceId: String = "place_1"
    private var currentPlaceName: String = "Sigiriya Rock Fortress"

    private lateinit var btnFavorite: ImageView
    private lateinit var ivHero: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvDescription: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.destination_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        extractArgumentsAndBindData()
        setupFavoriteButton()
    }

    private fun initViews(view: View) {
        view.findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            findNavController().navigateUp()
        }

        btnFavorite = view.findViewById(R.id.btn_favorite)
        ivHero = view.findViewById(R.id.iv_detail_hero)
        tvTitle = view.findViewById(R.id.tv_detail_title)
        tvLocation = view.findViewById(R.id.tv_detail_location)
        tvCategory = view.findViewById(R.id.tv_detail_category)
        tvRating = view.findViewById(R.id.tv_detail_rating)
        tvDescription = view.findViewById(R.id.tv_detail_description)
    }

    private fun extractArgumentsAndBindData() {
        val args = arguments
        val placeId = args?.getString("place_id")

        val place = if (placeId != null) {
            LankaGoDataManager.placesList.find { it.id == placeId }
        } else {
            LankaGoDataManager.placesList.firstOrNull()
        }

        if (place != null) {
            currentPlaceId = place.id
            currentPlaceName = place.name

            tvTitle.text = place.name
            tvLocation.text = place.location
            tvCategory.text = place.category
            tvRating.text = "★ ${place.rating}"
            tvDescription.text = place.description

            try {
                if (place.imageUri.startsWith("android.resource://") ||
                    place.imageUri.startsWith("content://") ||
                    place.imageUri.startsWith("file://")
                ) {
                    ivHero.setImageURI(Uri.parse(place.imageUri))
                } else {
                    ivHero.setImageResource(R.drawable.sigiriya)
                }
                if (ivHero.drawable == null) {
                    ivHero.setImageResource(R.drawable.sigiriya)
                }
            } catch (e: Exception) {
                ivHero.setImageResource(R.drawable.sigiriya)
            }
        }
    }

    private fun setupFavoriteButton() {
        val context = requireContext()

        // Check if current place is in favorites and set initial state
        val isFav = FavoritesManager.isFavorite(context, currentPlaceId)
        updateFavoriteIcon(isFav)

        // Toggle favorite on click
        btnFavorite.setOnClickListener {
            val isNowFav = FavoritesManager.toggleFavorite(context, currentPlaceId)
            updateFavoriteIcon(isNowFav)

            val message = if (isNowFav) {
                "Added to Favorites"
            } else {
                "Removed from Favorites"
            }
            Toast.makeText(context, "$currentPlaceName $message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled)
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline)
        }
    }
}
