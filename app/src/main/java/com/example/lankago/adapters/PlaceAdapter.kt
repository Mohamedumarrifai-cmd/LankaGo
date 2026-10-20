package com.example.lankago.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.lankago.R
import com.example.lankago.utils.FavoritesManager
import com.example.lankago.utils.Place

class PlaceAdapter(
    private var places: MutableList<Place>,
    private val onPlaceClick: (Place) -> Unit,
    private val onFavoriteToggled: ((Place, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_place_thumbnail)
        val tvName: TextView = itemView.findViewById(R.id.tv_place_name)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_place_location)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_place_category)
        val tvRating: TextView = itemView.findViewById(R.id.tv_place_rating)
        val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        val context = holder.itemView.context

        holder.tvName.text = place.name
        holder.tvLocation.text = place.location
        holder.tvCategory.text = place.category
        holder.tvRating.text = "★ ${place.rating}"

        // Safely load thumbnail
        try {
            if (place.imageUri.startsWith("android.resource://") ||
                place.imageUri.startsWith("content://") ||
                place.imageUri.startsWith("file://")
            ) {
                holder.ivThumbnail.setImageURI(Uri.parse(place.imageUri))
            } else {
                holder.ivThumbnail.setImageResource(R.drawable.sigiriya)
            }
            if (holder.ivThumbnail.drawable == null) {
                holder.ivThumbnail.setImageResource(R.drawable.sigiriya)
            }
        } catch (e: Exception) {
            holder.ivThumbnail.setImageResource(R.drawable.sigiriya)
        }

        // Set favorite state from FavoritesManager
        val isFav = FavoritesManager.isFavorite(context, place.id)
        updateFavoriteIcon(holder.ivFavorite, isFav)

        // Heart icon click listener
        holder.ivFavorite.setOnClickListener {
            val isNowFav = FavoritesManager.toggleFavorite(context, place.id)
            updateFavoriteIcon(holder.ivFavorite, isNowFav)
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos)
            }

            val message = if (isNowFav) {
                "Added to Favorites"
            } else {
                "Removed from Favorites"
            }
            Toast.makeText(context, "${place.name} $message", Toast.LENGTH_SHORT).show()

            onFavoriteToggled?.invoke(place, isNowFav)
        }

        // Row click -> view destination detail
        holder.itemView.setOnClickListener {
            onPlaceClick(place)
        }
    }

    private fun updateFavoriteIcon(imageView: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            imageView.setImageResource(R.drawable.ic_heart_filled)
        } else {
            imageView.setImageResource(R.drawable.ic_heart_outline)
        }
    }

    override fun getItemCount(): Int = places.size

    fun updateData(newPlaces: List<Place>) {
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
    }
}
