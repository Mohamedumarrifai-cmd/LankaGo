package com.example.lankago.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lankago.R
import com.example.lankago.data.RentalShop
import java.util.Locale

/**
 * ListAdapter for rendering rental cards in RentalsTransportFragment
 * matching the user's exact UI design style.
 */
class RentalTransportAdapter(
    private val onShopClick: (RentalShop) -> Unit
) : ListAdapter<RentalShop, RentalTransportAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rental_transport_card, parent, false)
        return ViewHolder(view, onShopClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onShopClick: (RentalShop) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivThumb: ImageView = itemView.findViewById(R.id.img_rental_thumb)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_rental_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_rental_subtitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rental_rating)

        fun bind(shop: RentalShop) {
            val context = itemView.context

            tvTitle.text = shop.name

            // Detect category display
            val category = resolveCategoryName(shop)
            val price = shop.priceStartingFrom.ifBlank { "LKR 3,500/day" }
            val city = shop.city.ifBlank { "Sri Lanka" }
            tvSubtitle.text = "$category · $price · $city"

            val ratingValue = if (shop.rating > 0f) shop.rating else 4.9f
            tvRating.text = "★ ${String.format(Locale.US, "%.1f", ratingValue)}"

            // Load Image with Glide (handles URL, Base64 data URI, and local drawables)
            val imageSource: Any = when {
                shop.imageUrl.isNotBlank() -> shop.imageUrl
                shop.imageRes != 0 -> shop.imageRes
                else -> R.drawable.epic_ride
            }

            Glide.with(context)
                .load(imageSource)
                .placeholder(R.drawable.epic_ride)
                .error(R.drawable.epic_ride)
                .centerCrop()
                .into(ivThumb)

            itemView.setOnClickListener { onShopClick(shop) }
        }

        private fun resolveCategoryName(shop: RentalShop): String {
            val combinedText = "${shop.name} ${shop.description} ${shop.fleetAvailable.joinToString(" ")}".lowercase()
            return when {
                combinedText.contains("scooter") -> "Scooter"
                combinedText.contains("bike") || combinedText.contains("moto") -> "Bike"
                combinedText.contains("van") || combinedText.contains("kdh") -> "Van"
                combinedText.contains("tuktuk") || combinedText.contains("tuk tuk") || combinedText.contains("tuk-tuk") -> "Tuk Tuk"
                combinedText.contains("bus") || combinedText.contains("coach") -> "Bus"
                combinedText.contains("car") || combinedText.contains("suv") || combinedText.contains("sedan") -> "Car"
                else -> "Vehicle"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RentalShop>() {
        override fun areItemsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RentalShop, newItem: RentalShop): Boolean =
            oldItem == newItem
    }
}
