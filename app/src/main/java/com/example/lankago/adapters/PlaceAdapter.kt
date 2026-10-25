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
        val tvDescription: TextView? = itemView.findViewById(R.id.tv_place_description)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_place_category)
        val tvFee: TextView? = itemView.findViewById(R.id.tv_place_fee)
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
        holder.tvDescription?.text = place.description
        if (holder.tvFee != null) {
            holder.tvFee.text = place.entranceFee
            holder.tvFee.visibility = if (place.entranceFee.isNotBlank()) View.VISIBLE else View.GONE
        }

        // Safely load thumbnail with Glide & drawable name resolution
        try {
            val resName = place.imageUri.substringAfterLast("/")
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                com.bumptech.glide.Glide.with(context)
                    .load(resId)
                    .centerCrop()
                    .placeholder(R.drawable.heritage_sigiriya)
                    .error(R.drawable.heritage_sigiriya)
                    .into(holder.ivThumbnail)
            } else if (place.imageUri.isNotBlank()) {
                com.bumptech.glide.Glide.with(context)
                    .load(place.imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.heritage_sigiriya)
                    .error(R.drawable.heritage_sigiriya)
                    .into(holder.ivThumbnail)
            } else {
                holder.ivThumbnail.setImageResource(R.drawable.heritage_sigiriya)
            }
        } catch (e: Exception) {
            holder.ivThumbnail.setImageResource(R.drawable.heritage_sigiriya)
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
